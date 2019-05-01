package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Bones
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Imp
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.LevelDigger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.ShopDigger
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.Group
import com.watabou.utils.Random

class LastShopLevel : RegularLevel() {
    init {
        color1 = 0x4b6636
        color2 = 0xf2f2f2
    }

    override fun trackMusic(): String = Assets.TRACK_CHAPTER_5

    override fun tilesTex(): String = Assets.TILES_CITY

    override fun waterTex(): String = Assets.WATER_CITY

    override fun chooseDiggers(): ArrayList<Digger> {
        val diggers = selectDiggers(0, 4) // no special diggers
        if (Imp.Quest.isCompleted())
            diggers.add(ShopDigger())

        return diggers
    }

    override fun createLevelDigger(): LevelDigger = LevelDigger(this, 0)

    override fun placeTraps() {} // do nothing

    override fun water(): BooleanArray = Patch.Generate(this, 0.35f, 4)

    override fun grass(): BooleanArray = Patch.Generate(this, 0.30f, 3)

    override fun decorate() {
        for (i in 0 until length()) {
            when (map[i]) {
                Terrain.EMPTY -> if (Random.Int(10) == 0) map[i] = Terrain.EMPTY_DECO
                Terrain.WALL -> if (Random.Int(8) == 0) map[i] = Terrain.WALL_DECO
                Terrain.SECRET_DOOR -> map[i] = Terrain.DOOR
            }
        }

        if (Imp.Quest.isCompleted()) {
            val space = spaces.find { it.type == DigResult.Type.Entrance }!!
            var pos: Int
            do {
                pos = pointToCell(space.rect.random())
            } while (pos == entrance || traps.get(pos) != null || findMob(pos) != null)
            map[pos] = Terrain.SIGN
        }
    }

    override fun createMobs() {}

    override fun respawner(): Actor? = null

    override fun createItems() {
        Bones.get()?.let { item ->
            val space = spaces.find { it.type == DigResult.Type.Entrance }!!
            var pos: Int
            do {
                pos = pointToCell(space.rect.random())
            } while (pos == entrance || map[pos] == Terrain.SIGN)
            drop(item, pos).type = Heap.Type.REMAINS
        }
    }

    override fun randomRespawnCell(): Int = pointToCell(spaces.find { it.type == DigResult.Type.Entrance }!!.rect.random())

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(CityLevel::class.java, "water_name")
        Terrain.HIGH_GRASS -> Messages.get(CityLevel::class.java, "high_grass_name")
        else -> super.tileName(tile)
    }

    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.ENTRANCE -> Messages.get(CityLevel::class.java, "entrance_desc")
        Terrain.EXIT -> Messages.get(CityLevel::class.java, "exit_desc")
        Terrain.WALL_DECO, Terrain.EMPTY_DECO -> Messages.get(CityLevel::class.java, "deco_desc")
        Terrain.STATUE, Terrain.STATUE_SP -> Messages.get(CityLevel::class.java, "statue_desc")
        Terrain.BOOKSHELF -> Messages.get(CityLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }

    override fun addVisuals(): Group {
        super.addVisuals()
        CityLevel.AddCityVisuals(this, visuals)
        return visuals
    }
}