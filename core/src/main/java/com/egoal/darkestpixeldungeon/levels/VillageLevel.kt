package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.blobs.MagicWellWater
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class VillageLevel : RegularLevel() {
    init {
        color1 = 0x48763c
        color2 = 0x59994a
    }

    private val MAP_FILE = "data/VillageLevel.map"

    override fun tilesTex(): String = Assets.TILES_VILLAGE

    override fun waterTex(): String = Assets.WATER_VILLAGE

    override fun water(): BooleanArray = BooleanArray(length) { false }

    override fun grass(): BooleanArray = Patch.Generate(this, 0.5f, 8)

    override fun build(iteration: Int): Boolean {
        loadMapDataFromFile(MAP_FILE)

//        map[xy2cell(7, 31)] = Terrain.ALCHEMY

        // map some terrains
        for (i in 0 until length) {
            when (map[i]) {
                Terrain.EMBERS -> map[i] = Terrain.WATER // water flag
            }
        }

        // no luminary& traps
        paintWater()
        paintGrass()

//        val cell = xy2cell(15, 26)
//        map[cell] = Terrain.WELL
//        val b = blobs[MagicWellWater::class.java]
//        val mww = if (b == null) MagicWellWater() else b as MagicWellWater
//        mww.seed(this, cell, 10)
//        blobs[MagicWellWater::class.java] = mww

        return true
    }

    override fun decorate() {
        // just put some tiny stone on the floor, some grass on the wall
        for (i in width until length - width) {
            if (map[i] == Terrain.WALL) {
                val nGrass = PathFinder.NEIGHBOURS4.count { map[i + it] == Terrain.GRASS }
                if (Random.Int(5) < nGrass)
                    map[i] = Terrain.WALL_DECO
            } else if (map[i] == Terrain.EMPTY) {
                val nWall = PathFinder.NEIGHBOURS4.count { map[i + it] == Terrain.WALL }
                if (Random.Int(16) < nWall * nWall)
                    map[i] = Terrain.EMPTY_DECO
            }
        }
    }

    override fun randomRespawnCell(): Int {
        var cell: Int
        do {
            cell = Random.Int(length())
        } while (!passable[cell] || Dungeon.visible[cell] || Actor.findChar(cell) != null)
        return cell
    }

    override fun nMobs(): Int = 0

    override fun createMobs() {
        // egoal
        putMobAt(CatEgoal::class.java, 8, 31)

        // old alchemist
        putMobAt(Alchemist::class.java, 18, 6)
        Alchemist.Quest.reset()

        // jessica
        putMobAt(Jessica::class.java, 14, 3)
        Jessica.Quest.reset()

        // sodan
        putMobAt(DisheartenedBuddy::class.java, 17, 11)

        // scholar
        putMobAt(Scholar::class.java, 27, 11)

        // minstrel
        putMobAt(Minstrel::class.java, 14, 19)

        // battle mage
        putMobAt(SPDBattleMage::class.java, 6, 13)

        putMobAt(CatRawberry::class.java, 12, 6)

        putMobAt(Passerby::class.java, 24, 23)

        putMobAt(Monument::class.java, 21, 2)

        // seeker
        putMobAt(Seeker::class.java, 12, 18)

//         Buff.affect(putMobAt(Rat::class.java, 16, 29), Dementage::class.java)
//        (putMobAt(Merchant::class.java, 16, 27) as Merchant).apply {
//            for (i in 1..10) addItemToSell(Generator.generate())
//        }
//        (putMobAt(ScrollSeller::class.java, 15, 27) as ScrollSeller).apply {
//            addItemToSell(Dart(8))
//            addItemToSell(BattleGloves())
//            addItemToSell(TomeOfPerk())
//        }
//        (putMobAt(UndeadShopkeeper::class.java, 15, 27) as UndeadShopkeeper).initSellItems()
//        putMobAt(Yvette::class.java, 15, 27)
//        putMobAt(Rotten::class.java, 15, 27)
        // (putMobAt(BarterMan::class.java, 15, 27) as BarterMan).initSellItems()
    }

    override fun onSwitchedIn() {
        if (Yvette.Quest.Completed && mobs.find { it.pos == xy2cell(19, 3) } == null)
            putMobAt(Yvette::class.java, 19, 3)
    }

    override fun respawner(): Actor? = null

    override fun createItems() {
//        for (i in 1..20)
//            drop(Generator.POTION.generate(), xy2cell(15, i + 3))
//        drop(MasterThievesArmband().apply {
//            identify()
//            level(5)
//        }, xy2cell(16, 30))

//        drop(MendingRune(), xy2cell(17, 5))
    }

    private fun putMobAt(cls: Class<out Mob>, x: Int, y: Int): Mob {
        val mob = cls.newInstance().apply { pos = xy2cell(x, y) }
        mobs.add(mob)
        return mob
    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(VillageLevel::class.java, "water_name")
        Terrain.STATUE -> Messages.get(VillageLevel::class.java, "statue_name")
        else -> super.tileName(tile)
    }

    override fun tileDesc(tile: Int) = when (tile) {
        Terrain.EMPTY_DECO -> Messages.get(SewerLevel::class.java, "empty_deco_desc")
        Terrain.BOOKSHELF -> Messages.get(SewerLevel::class.java, "bookshelf_desc")
        Terrain.WATER -> Messages.get(VillageLevel::class.java, "water_desc")
        Terrain.STATUE -> Messages.get(VillageLevel::class.java, "statue_desc")
        else -> super.tileDesc(tile)
    }
}