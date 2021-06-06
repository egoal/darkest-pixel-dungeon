/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon

import android.util.Log

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Resident
import com.egoal.darkestpixeldungeon.actors.buffs.Amok
import com.egoal.darkestpixeldungeon.actors.buffs.Awareness
import com.egoal.darkestpixeldungeon.actors.buffs.MindVision
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.*
import com.egoal.darkestpixeldungeon.items.Catalog
import com.egoal.darkestpixeldungeon.items.unclassified.Ankh
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.levels.*
import com.egoal.darkestpixeldungeon.levels.PrisonBossLevel
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.HeroCreateScene
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.windows.WndResurrect
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.utils.BArray
import com.watabou.noosa.Game
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import com.watabou.utils.SparseArray

import java.io.IOException
import java.util.ArrayList
import java.util.HashSet

object Dungeon {

    var initialDepth_ = -1
    const val VERSION_STRING = "0.6.1-0.1"

    lateinit var hero: Hero
    lateinit var level: Level

    var quickslot = QuickSlot()

    var depth: Int = 0
    var gold: Int = 0
    var torch: Float = 0f

    lateinit var chapters: HashSet<Int>

    // Hero's fov
    lateinit var visible: BooleanArray

    lateinit var droppedItems: SparseArray<ArrayList<Item>?>

    var version: Int = 0

    //we store this to avoid having to re-allocate the array with each pathfind
    private lateinit var passable: BooleanArray

    //todo: this is REALLY a bad usage
    fun nullHero() {
        nullField("hero")
    }

    fun nullLevel() {
        nullField("level")
    }

    val isHeroNull: Boolean get() = !::hero.isInitialized
    val isLevelNull: Boolean get() = !::level.isInitialized

    private fun nullField(fieldName: String) {
        with(javaClass.getDeclaredField(fieldName)) {
            isAccessible = true
            set(this, null)
        }
    }

    // save
    private val RG_GAME_FILE = "game.dat"
    private val RG_DEPTH_FILE = "depth%d.dat"

    private val WR_GAME_FILE = "warrior.dat"
    private val WR_DEPTH_FILE = "warrior%d.dat"

    private val MG_GAME_FILE = "mage.dat"
    private val MG_DEPTH_FILE = "mage%d.dat"

    private val RN_GAME_FILE = "ranger.dat"
    private val RN_DEPTH_FILE = "ranger%d.dat"

    private val SC_GAME_FILE = "sorceress.dat"
    private val SC_DEPTH_FILE = "sorceress%d.data"

    private val EL_GAME_FILE = "exile.dat"
    private val EL_DEPTH_FILE = "exile%d.data"

    private val VERSION = "version"
    private val CHALLENGES = "challenges"
    private val HERO = "hero"
    private val GOLD = "gold"
    private val DEPTH = "depth"
    private val TORCH = "torch"
    private val DROPPED = "dropped%d"
    private val LEVEL = "level"
    private val LIMDROPS = "limiteddrops"
    private val DV = "dewVial"
    private val CHAPTERS = "chapters"
    private val QUESTS = "quests"
    private val BADGES = "badges"

    //enum of items which have limited spawns, records how many have spawned
    //could all be their own separate numbers, but this allows iterating, much
    // nicer for bundling/initializing.
    //TODO: this is fairly brittle when it comes to bundling, should look into
    // a more flexible solution.
    enum class limitedDrops {
        //limited world drops
        strengthPotions,
        upgradeScrolls,
        lullabyScrolls,
        arcaneStyli,
        wine,

        laboratories,
        archDemons,
        magicWells,

        //all unlimited health potion sources (except guards, which are at the
        // bottom.
        swarmHP,
        guardHP,
        batHP,
        warlockHP,
        scorpioHP,
        cookingHP,
        madManHumanity,
        //blandfruit, which can technically be an unlimited health potion source
        blandfruitSeed,

        //doesn't use Generator, so we have to enforce one armband drop here
        armband,
        chaliceOfBlood, // only the statuary drop this now
        goddessRadiance,
        demonicSkull,
        handOfElder,
        boethiahsBlade,

        ceremonialDaggerUsed,
        ceremonialDagger,

        //containers
        dewVial,
        seedBag,
        scrollBag,
        potionBag,
        wandBag;

        var count = 0

        //for items which can only be dropped once, should directly access count
        // otherwise.
        fun dropped(): Boolean {
            return count != 0
        }

        fun drop() {
            count = 1
        }
    }

    fun init() {
        version = Game.versionCode

        Actor.clear()
        Actor.resetNextID()

        Scroll.initLabels()
        Potion.initColors()
        Ring.initGems()

        Statistics.reset()
        Journal.reset()

        quickslot.reset()
        QuickSlotButton.reset()

        depth = initialDepth_
        gold = 0
        torch = 0f

        droppedItems = SparseArray()

        for (a in limitedDrops.values())
            a.count = 0

        chapters = HashSet()

        // quest init
        Ghost.Quest.reset()
        Wandmaker.Quest.reset()
        Blacksmith.Quest.reset()
        Imp.Quest.reset()

        Alchemist.Quest.reset()
        Statuary.Reset()
        Jessica.Quest.reset()
        Yvette.Quest.Reset()

        Badges.reset()
        Catalog.Load()

        // hero init
        hero = Hero()
        hero.live()

        hero.userName = HeroCreateScene.UserName
        HeroCreateScene.CurrentClass.initHero(hero)
        HeroCreateScene.BornPrize.collect(hero)
    }

    fun IsChallenged(): Boolean = ::hero.isInitialized && hero.challenge != null

    fun newLevel(): Level {
        nullLevel()

        Actor.clear()

        depth++
        if (depth > Statistics.DeepestFloor) {
            Statistics.DeepestFloor = depth

            Statistics.CompletedWithNoKilling = depth > 1 && Statistics.QualifiedForNoKilling
        }

        val level: Level
        when (depth) {
            0 -> level = VillageLevel()
            1, 2, 3, 4 -> level = SewerLevel()
            5 -> level = SewerBossLevel()
            6, 7, 8, 9 -> level = PrisonLevel()
            10 -> level = PrisonBossLevel()
            11, 12, 13, 14 -> level = CavesLevel()
            15 -> level = CavesBossLevel()
            16, 17, 18, 19 -> level = CityLevel()
            20 -> level = CityBossLevel()
            21 -> level = LastShopLevel()
            22, 23, 24 -> level = HallsLevel()
            25 -> level = HallsBossLevel()
            26 -> level = LastLevel()
            else -> {
                level = DeadEndLevel()
                Statistics.DeepestFloor -= 1
            }
        }

        visible = BooleanArray(level.length())
        level.create()

        Statistics.QualifiedForNoKilling = !bossLevel()

        return level
    }

    fun resetLevel() {

        Actor.clear()

        level!!.reset()
        switchLevel(level!!, level!!.entrance)
    }

    fun shopOnLevel(): Boolean = depth in listOf(6, 11, 16)

    fun bossLevel(depth: Int = Dungeon.depth): Boolean = depth in listOf(5, 10, 15, 20, 25)

    fun switchLevel(level: Level, spawnPos: Int) {
        Dungeon.level = level

        val pos = if (spawnPos < 0 || spawnPos >= level.length()) level.exit else spawnPos

        level.onSwitchedIn()

        PathFinder.setMapSize(level.width(), level.height())

        // add into level.mobs, then into actor.
        hero.restoreFollowers(level, pos)
        Actor.init()

        visible = BooleanArray(level.length())

        val respawner = level.respawner()
        if (respawner != null) {
            Actor.add(respawner)
        }
        Actor.add(Resident())

        hero.pos = pos

        observe()
        try {
            saveAll()
        } catch (e: IOException) {
            DarkestPixelDungeon.reportException(e)
            /*This only catches IO errors. Yes, this means things can go wrong, and
      they can go wrong catastrophically.
			But when they do the user will get a nice 'report this issue' dialogue,
			and I can fix the bug.*/
        }

    }

    // drop items to the next level
    fun dropToChasm(item: Item) {
        val depth = depth + 1
        var dropped = droppedItems.get(depth)
        if (dropped == null) {
            dropped = ArrayList()
            droppedItems.put(depth, dropped)
        }

        dropped.add(item)
    }

    // quotas
    private fun quotaRequiredOnEachSet(quota: Int, current: Int, set: Int = 5): Boolean {
        val leftThisSet = quota - (current - depth / set * quota)
        if (leftThisSet <= 0) return false
        val floorThisSet = set - depth % set
        return Random.Int(floorThisSet) < leftThisSet
    }

    fun posNeeded(): Boolean {
        //2 POS each floor set
        val posLeftThisSet = 2 - (limitedDrops.strengthPotions.count - depth / 5 * 2)
        if (posLeftThisSet <= 0) return false

        val floorThisSet = depth % 5

        //pos drops every two floors, (numbers 1-2, and 3-4) with a 50% chance
        // for the earlier one each time.
        var targetPOSLeft = 2 - floorThisSet / 2
        if (floorThisSet % 2 == 1 && Random.Int(2) == 0) targetPOSLeft--

        return targetPOSLeft < posLeftThisSet
    }

    fun souNeeded(): Boolean = quotaRequiredOnEachSet(2, limitedDrops.upgradeScrolls.count)

    fun asNeeded(): Boolean = quotaRequiredOnEachSet(1, limitedDrops.arcaneStyli.count)

    // extra wine every 10 floor
    fun wineNeeded(): Boolean = quotaRequiredOnEachSet(1, limitedDrops.wine.count, 10)

    fun daggerNeeded(): Boolean = quotaRequiredOnEachSet(1, limitedDrops.ceremonialDagger.count)

    fun scrollOfLullabyNeed(): Boolean = quotaRequiredOnEachSet(1, limitedDrops.lullabyScrolls.count, 10)

    fun labNeed(): Boolean = quotaRequiredOnEachSet(1, limitedDrops.laboratories.count, 10)

    fun demonNeed(): Boolean {
        // from 12, 1 per 7 floors
        if (depth <= 12) return false

        val demonLeft = (depth - 12) / 6 + 1 - limitedDrops.archDemons.count
        return demonLeft > 0 && Random.Int(6 - (depth - 12) % 6) < demonLeft
    }

    fun wellNeed(): Boolean {
        // 1 per 10 floors, after 5
        if (depth <= 5) return false

        val wellLeft = (depth - 5) / 10 + 1 - limitedDrops.magicWells.count

        return wellLeft > 0 && Random.Int(10 - (depth - 5) % 10) < wellLeft
        // Random.RandomLW(0f, 1f) < ((float) wellLeft / Random.Int(10 - (depth - 5) % 10));
    }

    fun gameFile(cl: HeroClass): String = when (cl) {
        HeroClass.WARRIOR -> WR_GAME_FILE
        HeroClass.MAGE -> MG_GAME_FILE
        HeroClass.ROGUE -> RG_GAME_FILE
        HeroClass.HUNTRESS -> RN_GAME_FILE
        HeroClass.SORCERESS -> SC_GAME_FILE
        HeroClass.EXILE -> EL_GAME_FILE
    }

    fun backupGameFile(cl: HeroClass): String {
        return "backup_game_" + gameFile(cl)
    }

    fun backupLevelFile(cl: HeroClass): String {
        return "backup_level_" + gameFile(cl)
    }

    private fun depthFile(cl: HeroClass): String {
        when (cl) {
            HeroClass.WARRIOR -> return WR_DEPTH_FILE
            HeroClass.MAGE -> return MG_DEPTH_FILE
            HeroClass.HUNTRESS -> return RN_DEPTH_FILE
            HeroClass.SORCERESS -> return SC_DEPTH_FILE
            HeroClass.EXILE -> return EL_DEPTH_FILE
            else -> return RG_DEPTH_FILE
        }
    }

    @Throws(IOException::class)
    fun saveAll(doBackup: Boolean = false) {
        if (doBackup)
            Log.d("dpd", "saving with backup.")

        if (hero.isAlive) {
            Actor.fixTime()
            saveGame(GamesInProgress.curGameFile, if (doBackup) GamesInProgress.curBackupGameFile else null)
            saveLevel(GamesInProgress.curDepthFile(depth), if (doBackup) GamesInProgress.curBackupDepthFile else null)

            // GamesInProgress[hero.heroClass, depth, hero.lvl] = hero.challenge

            GamesInProgress[GamesInProgress.curSlot] = GamesInProgress.Info().apply {
                name = hero.userName
                heroClass = hero.heroClass
                subClass = hero.subClass
                depth = this@Dungeon.depth
                level = hero.lvl
                armorTier = hero.tier()
                challenge = hero.challenge
            }

        } else if (WndResurrect.instance != null) {
            WndResurrect.instance.hide()
            Hero.ReallyDie(WndResurrect.causeOfDeath)
        }
    }

    @Throws(IOException::class)
    private fun saveGame(fileName: String, backupFile: String?) {
        try {
            val bundle = Bundle()

            version = Game.versionCode
            bundle.put(VERSION, version)
            bundle.put(HERO, hero)
            bundle.put(GOLD, gold)
            bundle.put(DEPTH, depth)
            bundle.put(TORCH, torch)

            for (d in droppedItems.keyArray()) {
                bundle.put(Messages.format(DROPPED, d), droppedItems.get(d))
            }

            quickslot.storePlaceholders(bundle)

            val dropValues = IntArray(limitedDrops.values().size)
            for (value in limitedDrops.values())
                dropValues[value.ordinal] = value.count
            bundle.put(LIMDROPS, dropValues)

            var count = 0
            val ids = IntArray(chapters.size)
            for (id in chapters) {
                ids[count++] = id
            }
            bundle.put(CHAPTERS, ids)

            // quests
            val quests = Bundle()
            Ghost.Quest.storeInBundle(quests)
            Wandmaker.Quest.storeInBundle(quests)
            Blacksmith.Quest.storeInBundle(quests)
            Imp.Quest.storeInBundle(quests)
            Alchemist.Quest.storeInBundle(quests)
            Jessica.Quest.storeInBundle(quests)
            Yvette.Quest.StoreInBundle(quests)
            Statuary.Save(quests)

            bundle.put(QUESTS, quests)

            Statistics.storeInBundle(bundle)
            Journal.storeInBundle(bundle)
            Generator.storeInBundle(bundle)

            Scroll.save(bundle)
            Potion.save(bundle)
            Ring.save(bundle)

            Catalog.Save()

            Actor.storeNextID(bundle)

            val badges = Bundle()
            Badges.saveLocal(badges)
            bundle.put(BADGES, badges)

            val output = Game.instance.openFileOutput(fileName, Game
                    .MODE_PRIVATE)
            Bundle.write(bundle, output)
            output.close()

            if (backupFile != null) {
                val os = Game.instance.openFileOutput(backupFile, Game
                        .MODE_PRIVATE)
                Bundle.write(bundle, os)
                os.close()
            }

        } catch (e: IOException) {
            GamesInProgress.setUnknown(hero!!.heroClass)
            DarkestPixelDungeon.reportException(e)
        }

    }

    @Throws(IOException::class)
    private fun saveLevel(depthFile: String, backupFile: String?) {
        val bundle = Bundle()
        bundle.put(LEVEL, level)

        val output = Game.instance.openFileOutput(depthFile, Game.MODE_PRIVATE)
        Bundle.write(bundle, output)
        output.close()

        if (backupFile != null) {
            val os = Game.instance.openFileOutput(backupFile, Game.MODE_PRIVATE)
            Bundle.write(bundle, os)
            os.close()
        }
    }

    @Throws(IOException::class)
    fun loadGame() {
        loadGame(GamesInProgress.curGameFile, true)
    }

    @Throws(IOException::class)
    fun loadBackupGame() {
        loadGame(GamesInProgress.curBackupGameFile, true)
    }

    @Throws(IOException::class)
    private fun loadGame(fileName: String, fullLoad: Boolean) {
        val bundle = gameBundle(fileName)

        version = bundle.getInt(VERSION)

        Generator.reset()

        Actor.restoreNextID(bundle)

        quickslot.reset()
        QuickSlotButton.reset()

        nullLevel()
        depth = -1

        Scroll.restore(bundle)
        Potion.restore(bundle)
        Ring.restore(bundle)

        quickslot.restorePlaceholders(bundle)

        if (fullLoad) {
            val dropValues = bundle.getIntArray(LIMDROPS)
            for (value in limitedDrops.values())
                value.count = if (value.ordinal < dropValues.size)
                    dropValues[value.ordinal]
                else
                    0

            chapters = HashSet()
            val ids = bundle.getIntArray(CHAPTERS)
            if (ids != null) {
                for (id in ids) {
                    chapters.add(id)
                }
            }

            val quests = bundle.getBundle(QUESTS)
            if (!quests.isNull) {
                Ghost.Quest.restoreFromBundle(quests)
                Wandmaker.Quest.restoreFromBundle(quests)
                Blacksmith.Quest.restoreFromBundle(quests)
                Imp.Quest.restoreFromBundle(quests)

                // dpd, restore quests
                Alchemist.Quest.restoreFromBundle(quests)
                Jessica.Quest.restoreFromBundle(quests)
                Statuary.Load(quests)
                Yvette.Quest.RestoreFromBundle(quests)
            } else {
                Ghost.Quest.reset()
                Wandmaker.Quest.reset()
                Blacksmith.Quest.reset()
                Imp.Quest.reset()

                // dpd
                Alchemist.Quest.reset()
                Jessica.Quest.reset()
                Yvette.Quest.Reset()
            }
        }

        val badges = bundle.getBundle(BADGES)
        if (!badges.isNull) {
            Badges.loadLocal(badges)
        } else {
            Badges.reset()
        }

        hero = bundle.get(HERO) as Hero

        gold = bundle.getInt(GOLD)
        depth = bundle.getInt(DEPTH)
        torch = bundle.getFloat(TORCH)

        Statistics.restoreFromBundle(bundle)
        Journal.restoreFromBundle(bundle)
        Generator.restoreFromBundle(bundle)

        Catalog.Load()

        droppedItems = SparseArray()
        for (i in 2..Statistics.DeepestFloor + 1) {
            val dropped = ArrayList<Item>()
            for (b in bundle.getCollection(Messages.format(DROPPED, i))) {
                dropped.add(b as Item)
            }
            if (dropped.isNotEmpty()) {
                droppedItems.put(i, dropped)
            }
        }
    }

    @Throws(IOException::class)
    fun loadBackupLevel(): Level {
        return loadLevelFromFile(GamesInProgress.curBackupDepthFile)
    }

    @Throws(IOException::class)
    fun loadLevel(): Level {
        return loadLevelFromFile(GamesInProgress.curDepthFile(depth))
    }

    @Throws(IOException::class)
    private fun loadLevelFromFile(filename: String): Level {
        nullLevel()
        Actor.clear()

        val fin = Game.instance.openFileInput(filename)
        val bundle = Bundle.read(fin)
        fin.close()

        return bundle.get("level") as Level
    }

    fun deleteGame(deleteLevels: Boolean, deleteBackup: Boolean) {
        GamesInProgress.delete(GamesInProgress.curSlot, deleteLevels, deleteBackup)
    }

    @Throws(IOException::class)
    fun gameBundle(fileName: String): Bundle {
        val input = Game.instance.openFileInput(fileName)
        val bundle = Bundle.read(input)
        input.close()

        return bundle
    }

    fun preview(info: GamesInProgress.Info, bundle: Bundle) {
        info.depth = bundle.getInt(DEPTH)
        if (info.depth == -1) {
            info.depth = bundle.getInt("maxDepth")  // FIXME
        }
        Hero.Preview(info, bundle.getBundle(HERO))
    }

    fun fail(cause: Class<*>?) {
        if (hero.belongings.getItem(Ankh::class.java) == null) {
            Rankings.Submit(false, cause)
        }
    }

    fun win(cause: Class<*>) {
        hero.belongings.identify()

        if (IsChallenged()) Badges.validateChampion()

        Rankings.Submit(true, cause)
    }

    fun observe() {
        if (isHeroNull) return
        observe(hero.seeDistance() + 1)
    }

    fun observe(dist: Int) {
        if (isLevelNull) return

        level!!.updateFieldOfView(hero!!, visible)

        val cx = hero!!.pos % level!!.width()
        val cy = hero!!.pos / level!!.width()

        val ax = Math.max(0, cx - dist)
        val bx = Math.min(cx + dist, level!!.width() - 1)
        val ay = Math.max(0, cy - dist)
        val by = Math.min(cy + dist, level!!.height() - 1)

        val len = bx - ax + 1
        var pos = ax + ay * level!!.width()
        var y = ay
        while (y <= by) {
            BArray.or(level!!.visited, visible, pos, len, level!!.visited)
            y++
            pos += level!!.width()
        }

        if (hero!!.buff(MindVision::class.java) != null || hero!!.buff(Awareness::class.java) != null)
            GameScene.updateFog()
        else
            GameScene.updateFog(ax, ay, len, by - ay)

        for(mob in hero.mindVisionEnemies) GameScene.updateFog(mob.pos, 2)

        GameScene.afterObserve()
    }

    private fun setupPassable() {
        if (!::passable.isInitialized || passable.size != level!!.length())
            passable = BooleanArray(level!!.length())
        else
            BArray.setFalse(passable)
    }

    fun findPath(ch: Char, from: Int, to: Int, pass: BooleanArray, visible: BooleanArray): PathFinder.Path? {

        setupPassable()
        if (ch.flying || ch.buff(Amok::class.java) != null) {
            BArray.or(pass, Level.avoid, passable)
        } else {
            System.arraycopy(pass, 0, passable!!, 0, level!!.length())
        }

        for (c in Actor.chars()) {
            if (visible[c.pos]) {
                passable[c.pos] = false
            }
        }

        return PathFinder.find(from, to, passable)

    }

    fun findStep(ch: Char, from: Int, to: Int, pass: BooleanArray,
                 visible: BooleanArray): Int {

        if (level!!.adjacent(from, to)) {
            return if (Actor.findChar(to) == null && (pass[to] || Level
                            .avoid[to]))
                to
            else
                -1
        }

        setupPassable()
        if (ch.flying || ch.buff(Amok::class.java) != null) {
            BArray.or(pass, Level.avoid, passable)
        } else {
            System.arraycopy(pass, 0, passable, 0, level!!.length())
        }

        for (c in Actor.chars()) {
            if (visible[c.pos]) {
                passable[c.pos] = false
            }
        }

        return PathFinder.getStep(from, to, passable)

    }

    fun flee(ch: Char, cur: Int, from: Int, pass: BooleanArray,
             visible: BooleanArray): Int {

        setupPassable()
        if (ch.flying) {
            BArray.or(pass, Level.avoid, passable)
        } else {
            System.arraycopy(pass, 0, passable, 0, level!!.length())
        }

        for (c in Actor.chars()) {
            if (visible[c.pos]) {
                passable[c.pos] = false
            }
        }
        passable[cur] = true

        return PathFinder.getStepBack(cur, from, passable)
    }
}
