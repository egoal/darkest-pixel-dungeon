package com.egoal.darkestpixeldungeon

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.bags.Bag
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.watabou.noosa.Game
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

object Rankings {
    const val CAPACITY = 31 // 30 + one last 

    private const val FILE = "rankings.dat"
    private const val RECORDS = "records"
    private const val LATEST = "latest"
    private const val TOTAL = "total"
    private const val WON = "won"

    private const val HERO = "hero"
    private const val STATS = "stats"
    private const val BADGES = "badges"
    private const val HANDLERS = "handlers"

    lateinit var records: ArrayList<Record>
    var lastRecord = 0
    var totalNumber = 0
    var wonNumber = 0

    fun Submit(win: Boolean, cause: Class<*>) {
        Load()

        // make new record
        val rec = Record().apply {
            this.cause = cause
            this.win = win
            heroClass = Dungeon.hero.heroClass
            armorTier = Dungeon.hero.tier()
            heroLevel = Dungeon.hero.lvl
            depth = Dungeon.depth
            score = Score(win)
        }

        SaveGameData(rec)
        rec.gameID = UUID.randomUUID().toString()

        records.add(rec)
        records.sortBy { -it.score }

        lastRecord = records.indexOf(rec)
        // keep the size
        var size = records.size
        while (size > CAPACITY) {
            if (lastRecord == size - 1) {
                records.removeAt(size - 2)
                lastRecord--
            } else records.removeAt(size - 1)
            size = records.size
        }

        ++totalNumber
        if (win) ++wonNumber

        Badges.validateGamesPlayed()

        Save()
    }

    fun SaveGameData(rec: Record) {
        rec.gameData = Bundle()

        val belongings = Dungeon.hero.belongings
        val allItems = arrayListOf<Item>().apply { addAll(belongings.backpack.items) }

        // remove those wont show up in ranking
        val items = arrayListOf<Item>()
        for (item in belongings.backpack.items)
            if (item is Bag)
                items.addAll(item.items.filter { Dungeon.quickslot.contains(it) })
            else if (Dungeon.quickslot.contains(item)) items.add(item)
        belongings.backpack.items = items
        rec.gameData.put(HERO, Dungeon.hero)

        // stats
        val stats = Bundle()
        Statistics.storeInBundle(stats)
        rec.gameData.put(STATS, stats)

        // badges
        val badges = Bundle()
        Badges.saveLocal(badges)
        rec.gameData.put(BADGES, badges)

        // handler
        val handler = Bundle()
        Scroll.saveSelectively(handler, belongings.backpack.items)
        Potion.saveSelectively(handler, belongings.backpack.items)
        if (belongings.misc1 != null) belongings.backpack.items.add(belongings.misc1)
        if (belongings.misc2 != null) belongings.backpack.items.add(belongings.misc2)
        if (belongings.misc3 != null) belongings.backpack.items.add(belongings.misc3)
        Ring.saveSelectively(handler, belongings.backpack.items)
        rec.gameData.put(HANDLERS, handler)

        belongings.backpack.items = allItems
    }

    fun LoadGameData(rec: Record) {
        val data = rec.gameData

        Dungeon.hero = null
        Dungeon.level = null
        Generator.reset()
        Dungeon.quickslot.reset()
        QuickSlotButton.reset()

        val handler = data.getBundle(HANDLERS)
        Scroll.restore(handler)
        Potion.restore(handler)
        Ring.restore(handler)

        Badges.loadLocal(data.getBundle(BADGES))

        Dungeon.hero = data.get(HERO) as Hero

        Statistics.restoreFromBundle(data.getBundle(STATS))
    }

    private fun Save() {
        val bundle = Bundle()
        bundle.put(RECORDS, records)
        bundle.put(LATEST, lastRecord)
        bundle.put(TOTAL, totalNumber)
        bundle.put(WON, wonNumber)

        val output = Game.instance.openFileOutput(FILE, Game.MODE_PRIVATE)
        Bundle.write(bundle, output)
        output.close()
    }

    fun Load() {
        if (::records.isInitialized) return
        records = arrayListOf()

        try {
            val input = Game.instance.openFileInput(FILE)
            val bundle = Bundle.read(input)
            input.close()

            records.addAll(bundle.getCollection(RECORDS).map { it as Record })
            lastRecord = bundle.getInt(LATEST)
            totalNumber = bundle.getInt(TOTAL)
            if (totalNumber == 0) totalNumber = records.size

            wonNumber = bundle.getInt(WON)
            if (wonNumber == 0) wonNumber = records.count { it.win }

        } catch (e: IOException) {
        }
    }

    private fun Score(win: Boolean) = Statistics.GoldCollected +
            Dungeon.hero.lvl * (if (win) 26 * 2 else Dungeon.depth) * 100

    private const val REC_CAUSE = "cause"
    private const val REC_WIN = "win"
    private const val REC_SCORE = "score"
    private const val REC_TIER = "tier"
    private const val REC_LEVEL = "level"
    private const val REC_DEPTH = "depth"
    private const val REC_DATA = "gameData"
    private const val REC_ID = "gameID"

    class Record : Bundlable {
        var cause: Class<*>? = null
        var win: Boolean = false
        lateinit var heroClass: HeroClass
        var armorTier = 0
        var heroLevel = 0
        var depth = 0

        lateinit var gameData: Bundle
        var gameID = ""

        var score = 0

        fun desc(): String = if (cause == null) {
            M.L(this, "something")
        } else {
            val result = M.L(cause!!, "rankings_desc", M.L(cause!!, "name"))
            if (result.contains("missed string")) M.L(this, "something") else result
        }

        override fun storeInBundle(bundle: Bundle) {
            bundle.put(REC_CAUSE, cause)

            bundle.put(REC_WIN, win)
            bundle.put(REC_SCORE, score)
            heroClass.storeInBundle(bundle)
            bundle.put(REC_TIER, armorTier)
            bundle.put(REC_LEVEL, heroLevel)
            bundle.put(REC_DEPTH, depth)

            bundle.put(REC_DATA, gameData)
            bundle.put(REC_ID, gameID)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            cause = bundle.getClass(REC_CAUSE)

            win = bundle.getBoolean(REC_WIN)
            score = bundle.getInt(REC_SCORE)
            heroClass = HeroClass.RestoreFromBundle(bundle)
            armorTier = bundle.getInt(REC_TIER)
            heroLevel = bundle.getInt(REC_LEVEL)
            depth = bundle.getInt(REC_DEPTH)

            gameData = bundle.getBundle(REC_DATA)
            gameID = bundle.getString(REC_ID)
        }
    }
}