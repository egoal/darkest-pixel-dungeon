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
package com.egoal.darkestpixeldungeon.levels

import android.util.Log

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.buffs.Shadows
import com.egoal.darkestpixeldungeon.actors.buffs.ViewMark
import com.egoal.darkestpixeldungeon.actors.hero.perks.Telepath
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.food.BrownAle
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.items.rings.RingOfWealth
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfLullaby
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.features.HighGrass
import com.egoal.darkestpixeldungeon.levels.features.Luminary
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.WellWater
import com.egoal.darkestpixeldungeon.actors.buffs.Awareness
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.buffs.MindVision
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.particles.FlowParticle
import com.egoal.darkestpixeldungeon.effects.particles.WindParticle
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.unclassified.Stylus
import com.egoal.darkestpixeldungeon.items.unclassified.Torch
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.levels.features.Chasm
import com.egoal.darkestpixeldungeon.levels.features.Door
import com.egoal.darkestpixeldungeon.levels.traps.Trap
import com.egoal.darkestpixeldungeon.mechanics.ShadowCaster
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.utils.BArray
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Game
import com.watabou.noosa.Group
import com.watabou.noosa.audio.Sample
import com.watabou.utils.*

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet

abstract class Level : Bundlable {

    protected var width: Int = 0
    protected var height: Int = 0
    protected var length: Int = 0

    var version: Int = 0
    lateinit var map: IntArray
    lateinit var visited: BooleanArray
    lateinit var mapped: BooleanArray

    var feeling = Feeling.NONE

    var entrance: Int = 0
    var exit: Int = 0

    //when a boss level has become locked.
    var locked = false

    var mobs = HashSet<Mob>()
    var heaps = SparseArray<Heap>()
    var blobs = HashMap<Class<out Blob>, Blob>()
    var plants = SparseArray<Plant>()
    var traps = SparseArray<Trap>()
    var customTiles = HashSet<CustomTileVisual>()
    var luminaries = HashSet<Luminary>()

    protected var itemsToSpawn = ArrayList<Item>()

    // visuals is added each time the scene is created,
    // so, no need to keep track on them in the bundle
    protected lateinit var visuals: Group // ? = null

    var color1 = 0x004400
    var color2 = 0x88CC44

    enum class Feeling {
        NONE,
        CHASM,
        WATER,
        GRASS,
        DARK
    }

    open fun create() {
        setupSize()
        PathFinder.setMapSize(width, height)
        Luminary.SetMapSize(width, height)

        // allocation
        fieldOfView = BooleanArray(length())
        passable = BooleanArray(length())
        losBlocking = BooleanArray(length())
        flamable = BooleanArray(length())
        secret = BooleanArray(length())
        solid = BooleanArray(length())
        avoid = BooleanArray(length())
        water = BooleanArray(length())
        pit = BooleanArray(length())
        lighted = BooleanArray(length())

        map = IntArray(length())
        visited = BooleanArray(length())
        Arrays.fill(visited, false)
        mapped = BooleanArray(length())
        Arrays.fill(mapped, false)

        // feeling
        if (Dungeon.depth != 0 && Dungeon.depth != 21 && !Dungeon.bossLevel()) {
            if (Dungeon.depth > 1) {
                val p = Random.Float()
                if (p < 0.2)
                    feeling = Feeling.DARK
                else if (p < 0.275)
                    feeling = Feeling.WATER
                else if (p < 0.35)
                    feeling = Feeling.GRASS
            }
        }

        val stationaryItems = createStationaryItems()

        // now these two variables only set one once, so move outside
        pitRoomNeeded = Dungeon.depth > 1 && weakFloorCreated
        weakFloorCreated = false

        // some status may be modified during building
        Generator.stash()
        val hasDropped = ArrayList<Item>()
        val dropped = Dungeon.droppedItems.get(Dungeon.depth + 1)
        if (dropped != null) hasDropped.addAll(dropped)

        var i = 0
        while (true) {
            itemsToSpawn.clear()
            itemsToSpawn.addAll(stationaryItems)

            // no chasm feeling
            Arrays.fill(map, Terrain.WALL)

            mobs.clear()
            heaps.clear()
            blobs.clear()
            plants.clear()
            traps.clear()
            customTiles.clear()
            luminaries.clear()

            Generator.ResetCategoryProbs()
            if (build(i)) {
                Log.d("dpd", String.format("level build okay after %d trails.", i))
                break
            }

            Generator.recover()
            ++i
        }

        if (dropped != null) {
            dropped.clear()
            dropped.addAll(hasDropped)
        }

        decorate()

        buildFlagMaps()
        cleanWalls()

        createMobs()
        createItems()
    }

    protected open fun setupSize() {
        if (width == 0 || height == 0) {
            height = 36
            width = height
        }
        length = width * height
    }

    fun reset() {

        for (mob in mobs.toTypedArray()) {
            if (!mob.reset()) {
                mobs.remove(mob)
            }
        }
        createMobs()
    }

    // background music
    open fun trackMusic(): String {
        return Assets.TRACK_CHAPTER_1
    }

    override fun restoreFromBundle(bundle: Bundle) {

        version = bundle.getInt(VERSION)

        if (bundle.contains("width") && bundle.contains("height")) {
            width = bundle.getInt("width")
            height = bundle.getInt("height")
        } else {
            height = 36
            width = height
        } //default sizes
        length = width * height
        PathFinder.setMapSize(width(), height())
        Luminary.SetMapSize(width, height)

        mobs = HashSet()
        heaps = SparseArray()
        blobs = HashMap()
        plants = SparseArray()
        traps = SparseArray()
        customTiles = HashSet()

        map = bundle.getIntArray(MAP)

        visited = bundle.getBooleanArray(VISITED)
        mapped = bundle.getBooleanArray(MAPPED)

        entrance = bundle.getInt(ENTRANCE)
        exit = bundle.getInt(EXIT)

        locked = bundle.getBoolean(LOCKED)

        weakFloorCreated = false

        var collection = bundle.getCollection(HEAPS)
        for (h in collection) {
            val heap = h as Heap
            if (!heap.empty())
                heaps.put(heap.pos, heap)
        }

        collection = bundle.getCollection(PLANTS)
        for (p in collection) {
            val plant = p as Plant
            plants.put(plant.pos, plant)
        }

        collection = bundle.getCollection(TRAPS)
        for (p in collection) {
            val trap = p as Trap
            traps.put(trap.pos, trap)
        }

        collection = bundle.getCollection(CUSTOM_TILES)
        for (p in collection) {
            val vis = p as CustomTileVisual
            customTiles.add(vis)
        }


        mobs.addAll(bundle.getCollection(MOBS).mapNotNull { it as Mob? })

        collection = bundle.getCollection(BLOBS)
        for (b in collection) {
            val blob = b as Blob
            blobs[blob.javaClass] = blob
        }

        feeling = bundle.getEnum(FEELING, Feeling::class.java)

        buildFlagMaps()
        cleanWalls()
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(VERSION, Game.versionCode)
        bundle.put("width", width)
        bundle.put("height", height)
        bundle.put(MAP, map)
        bundle.put(VISITED, visited)
        bundle.put(MAPPED, mapped)
        bundle.put(ENTRANCE, entrance)
        bundle.put(EXIT, exit)
        bundle.put(LOCKED, locked)
        bundle.put(HEAPS, heaps.values())
        bundle.put(PLANTS, plants.values())
        bundle.put(TRAPS, traps.values())
        bundle.put(CUSTOM_TILES, customTiles)
        bundle.put(MOBS, mobs)
        bundle.put(BLOBS, blobs.values)
        bundle.put(FEELING, feeling)
    }

    fun tunnelTile(): Int {
        return if (feeling == Feeling.CHASM) Terrain.EMPTY_SP else Terrain.EMPTY
    }

    fun width(): Int {
        if (width == 0)
            setupSize()
        return width
    }

    fun height(): Int {
        if (height == 0)
            setupSize()
        return height
    }

    fun length(): Int {
        if (length == 0)
            setupSize()
        return length
    }

    abstract fun tilesTex(): String

    abstract fun waterTex(): String

    protected abstract fun build(iteration: Int): Boolean

    protected abstract fun decorate()

    protected abstract fun createMobs()

    protected abstract fun createItems()

    fun loadMapDataFromFile(mapfile: String): Boolean {
        val br = Game.instance.assets.open(mapfile).bufferedReader()
        val header = br.readLine()
        val wh = header.split(' ')
        val w = wh[0].toInt()
        val h = wh[1].toInt()

        assert(w == width && h == height)

        for (r in 0 until h) {
            val data = br.readLine().split(' ').map { it.toInt() }
            for (c in 0 until w) {
                val pos = r * width() + c
                map[pos] = data[c]

                // assign necessary values
                when (data[c]) {
                    Terrain.ENTRANCE -> entrance = pos
                    Terrain.LOCKED_EXIT, Terrain.UNLOCKED_EXIT, Terrain.EXIT -> exit = pos
                }
            }
        }

        return true
    }

    open fun seal() {
        if (!locked) {
            locked = true
            Buff.affect(Dungeon.hero, LockedFloor::class.java)
        }
    }

    open fun unseal() {
        if (locked) locked = false
    }

    open fun addVisuals(): Group {
        if (!::visuals.isInitialized || visuals.parent == null) {
            visuals = Group()
        } else visuals.clear()

        for (i in 0 until length()) {
            if (pit[i]) {
                visuals.add(WindParticle.Wind(i))
                if (i >= width() && water[i - width()]) {
                    visuals.add(FlowParticle.Flow(i - width()))
                }
            }
        }

        // add luminaries' visuals
        for (lum in luminaries) {
            lum.visual()?.let { visuals.add(it) }
        }

        return visuals
    }

    private fun addSceneLuminaries() {
        // luminaries from map generation
        for (i in 0 until length()) {
            val flags = Terrain.flags[map[i]]
            if (flags and Terrain.LUMINARY != 0)
                luminaries.add(createSceneLuminaryAt(i))
        }
    }

    fun addLuminary(lum: Luminary) {
        luminaries.add(lum)
    }

    fun removeLuminary(lum: Luminary) {
        luminaries.remove(lum)
        val lv = lum.visual()
        if (lv != null) visuals.remove(lv)
    }

    fun removeLuminaryAt(pos: Int) {
        for (lum in luminaries)
            if (lum.pos == pos) {
                removeLuminary(lum)
                break
            }
    }

    protected open fun createSceneLuminaryAt(pos: Int): Luminary {
        return Luminary(pos)
    }

    // called before actor init, after restored.
    open fun onSwitchedIn() {}

    open fun nMobs(): Int {
        return 0
    }

    fun findMobAt(pos: Int): Mob? = findMob { it.pos == pos }

    fun findMob(filter: (Mob) -> Boolean): Mob? = mobs.find(filter)

    private fun respawnTime(): Float = when (Statistics.Clock.state) {
        Statistics.ClockTime.State.Day -> 50f
        Statistics.ClockTime.State.Night -> 40f
        Statistics.ClockTime.State.MidNight -> 35f
    }

    open fun respawner(): Actor? = object : Actor() {
        init {
            actPriority = 1 //as if it were a buff.
        }

        override fun act(): Boolean {
            if (mobs.size < nMobs()) {

                val mob = Bestiary.mutable(Dungeon.depth)
                mob!!.state = mob.WANDERING
                mob.pos = randomRespawnCell()
                if (Dungeon.hero.isAlive && mob.pos != -1 && distance(Dungeon.hero.pos, mob.pos) >= 4) {
                    GameScene.add(mob)
                    if (Statistics.AmuletObtained) mob.beckon(Dungeon.hero.pos)
                }
            }
            spend(respawnTime())
            return true
        }
    }

    open fun randomRespawnCell(): Int {
        var cell: Int
        do {
            cell = Random.Int(length())
        } while (!passable[cell] || Dungeon.visible[cell] || Actor.findChar(cell) != null)
        return cell
    }

    open fun randomDestination(): Int {
        var cell: Int
        do {
            cell = Random.Int(length())
        } while (!passable[cell])
        return cell
    }

    fun addItemToSpawn(item: Item?) {
        if (item != null) {
            itemsToSpawn.add(item)
        }
    }

    @JvmOverloads
    fun findPrizeItem(match: Class<out Item>? = null): Item? {
        if (itemsToSpawn.size == 0)
            return null

        if (match == null) {
            val item = Random.element(itemsToSpawn)
            itemsToSpawn.remove(item)
            return item
        }

        for (item in itemsToSpawn) {
            if (match.isInstance(item)) {
                itemsToSpawn.remove(item)
                return item
            }
        }

        return null
    }

    // call this each time luminary is modified
    fun updateLightMap() {
        BArray.setFalse(lighted)
        for (lum in luminaries.toTypedArray()) lum.light(this)
    }

    protected fun buildFlagMaps() {
        //todo: allocation again
        fieldOfView = BooleanArray(length())
        passable = BooleanArray(length())
        losBlocking = BooleanArray(length())
        flamable = BooleanArray(length())
        secret = BooleanArray(length())
        solid = BooleanArray(length())
        avoid = BooleanArray(length())
        water = BooleanArray(length())
        pit = BooleanArray(length())
        lighted = BooleanArray(length())

        for (i in 0 until length()) {
            val flags = Terrain.flags[map[i]]
            passable[i] = flags and Terrain.PASSABLE != 0
            losBlocking[i] = flags and Terrain.LOS_BLOCKING != 0
            flamable[i] = flags and Terrain.FLAMABLE != 0
            secret[i] = flags and Terrain.SECRET != 0
            solid[i] = flags and Terrain.SOLID != 0
            avoid[i] = flags and Terrain.AVOID != 0
            water[i] = flags and Terrain.LIQUID != 0
            pit[i] = flags and Terrain.PIT != 0
        }

        addSceneLuminaries()
        updateLightMap()

        val lastRow = length() - width()
        for (i in 0 until width()) {
            avoid[i] = false
            passable[i] = avoid[i]
            avoid[lastRow + i] = false
            passable[lastRow + i] = avoid[lastRow + i]
        }
        run {
            var i = width()
            while (i < lastRow) {
                avoid[i] = false
                passable[i] = avoid[i]
                avoid[i + width() - 1] = false
                passable[i + width() - 1] = avoid[i + width() - 1]
                i += width()
            }
        }

        for (i in width() until length() - width()) {

            if (water[i]) {
                map[i] = getWaterTile(i)
            }

            if (pit[i]) {
                if (!pit[i - width()]) {
                    val c = map[i - width()]
                    if (c == Terrain.EMPTY_SP || c == Terrain.STATUE_SP) {
                        map[i] = Terrain.CHASM_FLOOR_SP
                    } else if (water[i - width()]) {
                        map[i] = Terrain.CHASM_WATER
                    } else if (Terrain.flags[c] and Terrain.UNSTITCHABLE != 0) {
                        map[i] = Terrain.CHASM_WALL
                    } else {
                        map[i] = Terrain.CHASM_FLOOR
                    }
                }
            }
        }
    }

    private fun getWaterTile(pos: Int): Int {
        var t = Terrain.WATER_TILES
        for (j in PathFinder.NEIGHBOURS4.indices) {
            if (Terrain.flags[map[pos + PathFinder.NEIGHBOURS4[N4Indicies[j]]]] and Terrain.UNSTITCHABLE != 0) {
                t += 1 shl j
            }
        }
        return t
    }

    fun destroy(pos: Int) {
        if (Terrain.flags[map[pos]] and Terrain.UNSTITCHABLE == 0) {

            set(pos, Terrain.EMBERS)

        } else {
            var flood = false
            for (j in PathFinder.NEIGHBOURS4.indices) {
                if (water[pos + PathFinder.NEIGHBOURS4[j]]) {
                    flood = true
                    break
                }
            }
            if (flood) {
                set(pos, getWaterTile(pos))
            } else {
                set(pos, Terrain.EMBERS)
            }
        }
    }

    private fun cleanWalls() {
        discoverable = BooleanArray(length())
        for (i in 0 until length()) {

            var d = false

            for (j in PathFinder.NEIGHBOURS9.indices) {
                val n = i + PathFinder.NEIGHBOURS9[j]
                if (n >= 0 && n < length() &&
                        map[n] != Terrain.WALL && map[n] != Terrain.WALL_DECO &&
                        map[n] != Terrain.WALL_LIGHT_OFF && map[n] != Terrain
                                .WALL_LIGHT_ON) {
                    d = true
                    break
                }
            }

            if (d) {
                d = false

                for (j in PathFinder.NEIGHBOURS9.indices) {
                    val n = i + PathFinder.NEIGHBOURS9[j]
                    if (n >= 0 && n < length() && !pit[n]) {
                        d = true
                        break
                    }
                }
            }

            discoverable[i] = d
        }
    }

    open fun drop(item: Item, cell: Int): Heap {
        var cell = cell

        if (Challenges.isForbidden(item)) {

            //create a dummy heap, give it a dummy sprite, don't add it to the
            // game, and return it.
            //effectively nullifies whatever the logic calling this wants to do,
            // including dropping items.
            val heap = Heap()
            heap.sprite = ItemSprite()
            val sprite = heap.sprite!!
            sprite.link(heap)
            return heap
        }

        // don't drop on them
        if (map[cell] == Terrain.ALCHEMY || map[cell] == Terrain.ENCHANTING_STATION) {
            var n: Int
            do {
                n = cell + PathFinder.NEIGHBOURS8[Random.Int(8)]
            } while (map[n] != Terrain.EMPTY_SP) //fixme: as they must put on
            // empty_sp tiles!!!
            cell = n
        }

        var heap: Heap? = heaps.get(cell)
        if (heap == null) {

            heap = Heap()
            heap.seen = Dungeon.visible[cell]
            heap.pos = cell
            if (map[cell] == Terrain.CHASM || Dungeon.level != null && pit[cell]) {
                Dungeon.dropToChasm(item)
                GameScene.discard(heap)
            } else {
                heaps.put(cell, heap)
                GameScene.add(heap)
            }

        } else if (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST) {
            var n: Int
            do {
                n = cell + PathFinder.NEIGHBOURS8[Random.Int(8)]
            } while (!passable[n] && !avoid[n])
            return drop(item, n)

        }
        heap.drop(item)

        if (Dungeon.level != null) {
            press(cell, null)
        }

        return heap
    }

    fun plant(seed: Plant.Seed, pos: Int): Plant? {
        if (Dungeon.isChallenged(Challenges.NO_HERBALISM)) return null

        var plant: Plant? = plants.get(pos)
        plant?.wither()

        if (map[pos] == Terrain.HIGH_GRASS ||
                map[pos] == Terrain.EMPTY ||
                map[pos] == Terrain.EMBERS ||
                map[pos] == Terrain.EMPTY_DECO) {
            map[pos] = Terrain.GRASS
            flamable[pos] = true
            GameScene.updateMap(pos)
        }

        plant = seed.couch(pos)
        plants.put(pos, plant)

        GameScene.add(plant)

        return plant
    }

    fun uproot(pos: Int) {
        plants.remove(pos)
    }

    fun setTrap(trap: Trap, pos: Int): Trap {
        val existingTrap = traps.get(pos)
        if (existingTrap != null) {
            traps.remove(pos)
            if (existingTrap.sprite != null) existingTrap.sprite.kill()
        }
        trap.set(pos)
        traps.put(pos, trap)
        GameScene.add(trap)
        return trap
    }

    fun disarmTrap(pos: Int) {
        set(pos, Terrain.INACTIVE_TRAP)
        GameScene.updateMap(pos)
    }

    fun discover(cell: Int) {
        set(cell, Terrain.discover(map[cell]))
        val trap = traps.get(cell)
        trap?.reveal()
        GameScene.updateMap(cell)
    }

    open fun pitCell(): Int {
        return randomRespawnCell()
    }

    // hero press
    open fun press(cell: Int, ch: Char?) {

        if (ch != null && pit[cell] && !ch.flying) {
            if (ch === Dungeon.hero) Chasm.HeroFall(cell)
            else if (ch is Mob) Chasm.MobFall(ch)

            return
        }

        var trap: Trap? = null
        when (map[cell]) {

            Terrain.SECRET_TRAP -> {
                GLog.i(Messages.get(Level::class.java, "hidden_plate"))
                trap = traps.get(cell)
            }
            Terrain.TRAP -> trap = traps.get(cell)

            Terrain.HIGH_GRASS, Terrain.HIGH_GRASS_COLLECTED -> HighGrass.Trample(this, cell, ch)

            Terrain.WELL -> WellWater.AffectCell(cell)

            // Terrain.ALCHEMY -> if (ch == null) { Alchemy.transmute(cell) }

            Terrain.DOOR -> Door.Enter(cell, ch)
        }

        val timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze::class.java)

        if (trap != null) {
            if (timeFreeze == null) {

                if (ch === Dungeon.hero) Dungeon.hero.interrupt()

                trap.trigger()
            } else {

                Sample.INSTANCE.play(Assets.SND_TRAP)

                discover(cell)

                timeFreeze.addDelayedPress(cell)
            }
        }

        plants.get(cell)?.trigger()
    }

    // mob press
    fun mobPress(mob: Mob) {

        val cell = mob.pos

        if (pit[cell] && !mob.flying) {
            Chasm.MobFall(mob)
            return
        }

        var trap: Trap? = null
        when (map[cell]) {

            Terrain.TRAP -> trap = traps.get(cell)

            Terrain.DOOR -> Door.Enter(cell, mob)
        }

        trap?.trigger()

        val plant = plants.get(cell)
        plant?.trigger()
    }

    fun updateFieldOfView(c: Char, fieldOfView: BooleanArray) {
        val cx = c.pos % width()
        val cy = c.pos / width()

        val sighted = c.buff(Blindness::class.java) == null &&
                c.buff(Shadows::class.java) == null && c.isAlive
        if (sighted) {
            updateLightMap()
            ShadowCaster.castShadowRecursively(cx, cy, fieldOfView, c.viewDistance(), c.seeDistance())
        } else {
            BArray.setFalse(fieldOfView)
        }

        var sense = 1
        //Currently only the hero can get mind vision
        if (c.isAlive && c === Dungeon.hero) {
            for (b in c.buffs(MindVision::class.java)) {
                sense = Math.max((b as MindVision).distance, sense)
            }
        }

        if (!sighted || sense > 1) {

            val ax = Math.max(0, cx - sense)
            val bx = Math.min(cx + sense, width() - 1)
            val ay = Math.max(0, cy - sense)
            val by = Math.min(cy + sense, height() - 1)

            val len = bx - ax + 1
            var pos = ax + ay * width()
            var y = ay
            while (y <= by) {
                System.arraycopy(discoverable, pos, fieldOfView, pos, len)
                y++
                pos += width()
            }
        }

        //Currently only the hero can get mind vision or awareness
        if (c.isAlive && c === Dungeon.hero) {
            Dungeon.hero.mindVisionEnemies.clear()
            if (c.buff(MindVision::class.java) != null) {
                for (mob in mobs) {
                    if (!mob.isLiving) continue

                    val p = mob.pos
                    if (!fieldOfView[p]) Dungeon.hero.mindVisionEnemies.add(mob)

                    for (i in PathFinder.NEIGHBOURS9) fieldOfView[p + i] = true
                }
            } else if (c.heroPerk.has(Telepath::class.java)) {
                for (mob in mobs) {
                    if (!mob.isLiving) continue

                    val p = mob.pos
                    if (distance(c.pos, p) == 2) {

                        if (!fieldOfView[p]) {
                            Dungeon.hero.mindVisionEnemies.add(mob)
                        }
                        for (i in PathFinder.NEIGHBOURS9)
                            fieldOfView[p + i] = true
                    }
                }
            }
            if (c.buff(Awareness::class.java) != null) {
                for (heap in heaps.values()) {
                    val p = heap.pos
                    for (i in PathFinder.NEIGHBOURS9)
                        fieldOfView[p + i] = true
                }
            }
        }

        // view mark
        for (mob in mobs) {
            val vm = mob.buff(ViewMark::class.java)
            if (vm != null && vm.observer == c.id()) {
                val p = mob.pos
                for (i in PathFinder.NEIGHBOURS9)
                    fieldOfView[p + i] = true
            }
        }

        if (c === Dungeon.hero) {
            for (heap in heaps.values())
                if (!heap.seen && fieldOfView[heap.pos])
                    heap.seen = true
        }

    }

    private fun createStationaryItems(): ArrayList<Item> {
        val items = ArrayList<Item>()
        if (Dungeon.depth == 0 || Dungeon.depth == 21 || Dungeon.bossLevel())
            return items

        // quota
        items.add(Generator.FOOD.generate())

        val bonus = RingOfWealth.getBonus(Dungeon.hero, RingOfWealth.Wealth::class.java)
        val p = Math.pow(0.925, bonus.toDouble()).toFloat()
        if (Dungeon.posNeeded()) {
            items.add(if (Random.Float() > p)
                PotionOfMight()
            else
                PotionOfStrength())
            Dungeon.limitedDrops.strengthPotions.count++
        }
        if (Dungeon.souNeeded()) {
            items.add(ScrollOfUpgrade())
            Dungeon.limitedDrops.upgradeScrolls.count++
        }
        if (Dungeon.asNeeded()) {
            if (Random.Float() > p) items.add(Stylus())
            items.add(Stylus())
            Dungeon.limitedDrops.arcaneStyli.count++
        }
        if (Dungeon.wineNeeded()) {
            if (Random.Float() > p) items.add(Wine())
            items.add(if (Random.Float() < 0.6f) Wine() else BrownAle())
            Dungeon.limitedDrops.wine.count++
        }
        if (Dungeon.scrollOfLullabyNeed()) {
            if (Random.Float() > p) items.add(ScrollOfLullaby())
            items.add(ScrollOfLullaby())
            Dungeon.limitedDrops.lullabyScrolls.count++
        }

        // torch
        run {
            val prop = 0.3f - Dungeon.depth / 5 * 0.025f
            while (Random.Float() < prop) items.add(Torch())
        }

        // extra wine?

        // specials
        val rose = Dungeon.hero.belongings.getItem(DriedRose::class.java)
        if (rose != null && !rose.cursed) {
            // this way if a rose is dropped later in the game, player still has a
            // chance to max it out.
            val petalsNeeded = Math.ceil(((Dungeon.depth / 2 - rose.droppedPetals).toFloat() / 3).toDouble()).toInt()

            for (i in 1..petalsNeeded) {
                //the player may miss a single petal and still max their rose.
                if (rose.droppedPetals < 11) {
                    items.add(DriedRose.Companion.Petal())
                    rose.droppedPetals = rose.droppedPetals + 1
                }
            }
        }


        return items
    }

    fun distance(a: Int, b: Int): Int {
        val ax = a % width()
        val ay = a / width()
        val bx = b % width()
        val by = b / width()
        return Math.max(Math.abs(ax - bx), Math.abs(ay - by))
    }

    fun adjacent(a: Int, b: Int): Boolean {
        return distance(a, b) == 1
    }

    //returns true if the input is a valid tile within the level
    fun insideMap(tile: Int): Boolean {
        //top and bottom row and beyond
        return !//left and right column
        (tile < width || tile >= length - width || tile % width == 0 || tile % width == width - 1)
    }

    fun cellToPoint(cell: Int): Point {
        return Point(cell % width(), cell / width())
    }

    fun pointToCell(p: Point): Int {
        return p.x + p.y * width()
    }

    fun xy2cell(x: Int, y: Int): Int {
        return x + y * width()
    }

    open fun tileName(tile: Int): String {

        if (tile >= Terrain.WATER_TILES) {
            return tileName(Terrain.WATER)
        }
        if (tile == Terrain.HIGH_GRASS_COLLECTED)
            return tileName(Terrain.HIGH_GRASS)

        if (tile != Terrain.CHASM && Terrain.flags[tile] and Terrain.PIT != 0) {
            return tileName(Terrain.CHASM)
        }

        when (tile) {
            Terrain.CHASM -> return Messages.get(Level::class.java, "chasm_name")
            Terrain.EMPTY, Terrain.EMPTY_SP, Terrain.EMPTY_DECO, Terrain.SECRET_TRAP -> return Messages.get(Level::class.java, "floor_name")
            Terrain.GRASS -> return Messages.get(Level::class.java, "grass_name")
            Terrain.WATER -> return Messages.get(Level::class.java, "water_name")
            Terrain.WALL, Terrain.WALL_DECO, Terrain.SECRET_DOOR -> return Messages.get(Level::class.java, "wall_name")
            Terrain.DOOR -> return Messages.get(Level::class.java, "closed_door_name")
            Terrain.OPEN_DOOR -> return Messages.get(Level::class.java, "open_door_name")
            Terrain.ENTRANCE -> return Messages.get(Level::class.java, "entrace_name")
            Terrain.EXIT -> return Messages.get(Level::class.java, "exit_name")
            Terrain.EMBERS -> return Messages.get(Level::class.java, "embers_name")
            Terrain.LOCKED_DOOR -> return Messages.get(Level::class.java, "locked_door_name")
            Terrain.PEDESTAL -> return Messages.get(Level::class.java, "pedestal_name")
            Terrain.BARRICADE -> return Messages.get(Level::class.java, "barricade_name")
            Terrain.HIGH_GRASS -> return Messages.get(Level::class.java, "high_grass_name")
            Terrain.LOCKED_EXIT -> return Messages.get(Level::class.java, "locked_exit_name")
            Terrain.UNLOCKED_EXIT -> return Messages.get(Level::class.java, "unlocked_exit_name")
            Terrain.SIGN -> return Messages.get(Level::class.java, "sign_name")
            Terrain.WELL -> return Messages.get(Level::class.java, "well_name")
            Terrain.EMPTY_WELL -> return Messages.get(Level::class.java, "empty_well_name")
            Terrain.STATUE, Terrain.STATUE_SP -> return Messages.get(Level::class.java, "statue_name")
            Terrain.INACTIVE_TRAP -> return Messages.get(Level::class.java, "inactive_trap_name")
            Terrain.BOOKSHELF -> return Messages.get(Level::class.java, "bookshelf_name")
            Terrain.ALCHEMY -> return Messages.get(Level::class.java, "alchemy_name")
            Terrain.WALL_LIGHT_ON -> return Messages.get(Level::class.java, "lighton_name")
            Terrain.WALL_LIGHT_OFF -> return Messages.get(Level::class.java, "lightoff_name")
            Terrain.ENCHANTING_STATION -> return Messages.get(Level::class.java, "enchanting_station_name")


            else -> return Messages.get(Level::class.java, "default_name")
        }
    }

    open fun tileDesc(tile: Int): String {

        when (tile) {
            Terrain.CHASM -> return Messages.get(Level::class.java, "chasm_desc")
            Terrain.WATER -> return Messages.get(Level::class.java, "water_desc")
            Terrain.ENTRANCE -> return Messages.get(Level::class.java, "entrance_desc")
            Terrain.EXIT, Terrain.UNLOCKED_EXIT -> return Messages.get(Level::class.java, "exit_desc")
            Terrain.EMBERS -> return Messages.get(Level::class.java, "embers_desc")
            Terrain.HIGH_GRASS, Terrain.HIGH_GRASS_COLLECTED -> return Messages.get(Level::class.java, "high_grass_desc")
            Terrain.LOCKED_DOOR -> return Messages.get(Level::class.java, "locked_door_desc")
            Terrain.LOCKED_EXIT -> return Messages.get(Level::class.java, "locked_exit_desc")
            Terrain.BARRICADE -> return Messages.get(Level::class.java, "barricade_desc")
            Terrain.SIGN -> return Messages.get(Level::class.java, "sign_desc")
            Terrain.INACTIVE_TRAP -> return Messages.get(Level::class.java, "inactive_trap_desc")
            Terrain.STATUE, Terrain.STATUE_SP -> return Messages.get(Level::class.java, "statue_desc")
            Terrain.ALCHEMY -> return Messages.get(Level::class.java, "alchemy_desc")
            Terrain.EMPTY_WELL -> return Messages.get(Level::class.java, "empty_well_desc")
            Terrain.WALL_LIGHT_ON -> return Messages.get(Level::class.java, "lighton_desc")
            Terrain.WALL_LIGHT_OFF -> return Messages.get(Level::class.java, "lightoff_desc")
            Terrain.ENCHANTING_STATION -> return Messages.get(Level::class.java, "enchanting_station_desc")

            else -> {
                if (tile >= Terrain.WATER_TILES) {
                    return tileDesc(Terrain.WATER)
                }
                return if (Terrain.flags[tile] and Terrain.PIT != 0) {
                    tileDesc(Terrain.CHASM)
                } else Messages.get(Level::class.java, "default_desc")
            }
        }
    }

    companion object {
        //FIXME should not be static!
        lateinit var fieldOfView: BooleanArray

        lateinit var passable: BooleanArray
        lateinit var losBlocking: BooleanArray
        lateinit var flamable: BooleanArray
        lateinit var secret: BooleanArray
        lateinit var solid: BooleanArray
        lateinit var avoid: BooleanArray
        lateinit var water: BooleanArray
        lateinit var pit: BooleanArray
        lateinit var lighted: BooleanArray  // lighted by luminaries

        lateinit var discoverable: BooleanArray

        //FIXME this is sloppy. Should be able to keep track of this without static
        // variables
        var pitRoomNeeded = false
        var weakFloorCreated = false

        private const val VERSION = "version"
        private const val MAP = "map"
        private const val VISITED = "visited"
        private const val MAPPED = "mapped"
        private const val ENTRANCE = "entrance"
        private const val EXIT = "exit"
        private const val LOCKED = "locked"
        private const val HEAPS = "heaps"
        private const val PLANTS = "plants"
        private const val TRAPS = "traps"
        private const val CUSTOM_TILES = "customTiles"
        private const val MOBS = "mobs"
        private const val BLOBS = "blobs"
        private const val FEELING = "feeling"

        //FIXME this is a temporary fix here to avoid changing the tiles texture
        //This logic will be changed in 0.4.3 anyway
        private val N4Indicies = intArrayOf(0, 2, 3, 1)

        operator fun set(cell: Int, terrain: Int) {
            Digger.Set(Dungeon.level, cell, terrain)

            if (terrain != Terrain.TRAP && terrain != Terrain.SECRET_TRAP && terrain != Terrain.INACTIVE_TRAP) {
                Dungeon.level.traps.remove(cell)
            }

            val flags = Terrain.flags[terrain]
            passable[cell] = flags and Terrain.PASSABLE != 0
            losBlocking[cell] = flags and Terrain.LOS_BLOCKING != 0
            flamable[cell] = flags and Terrain.FLAMABLE != 0
            secret[cell] = flags and Terrain.SECRET != 0
            solid[cell] = flags and Terrain.SOLID != 0
            avoid[cell] = flags and Terrain.AVOID != 0
            pit[cell] = flags and Terrain.PIT != 0
            water[cell] = terrain == Terrain.WATER || terrain >= Terrain.WATER_TILES
        }
    }
}
