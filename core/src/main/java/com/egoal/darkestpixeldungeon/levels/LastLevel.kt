package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.items.Amulet
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.Group
import com.watabou.utils.Bundle

class LastLevel : Level() {

    init {
        color1 = 0x801500
        color2 = 0xa68521

        viewDistance = 4
    }

    private val MAP_FILE = "data/LastLevel.map"

    override fun setupSize() {
        if (width * height == 0) {
            width = 27
            height = 48
        }
        length = width * height
    }

    override fun tilesTex() = Assets.TILES_HALLS

    override fun waterTex() = Assets.WATER_HALLS

    override fun create() {
        super.create()

        // cannot jump down
        disableChasms()
    }

    override fun build(iteration: Int): Boolean {
        loadMapDataFromFile(MAP_FILE)

        for (i in 0 until length)
            if (map[i] == Terrain.EMBERS)
                map[i] = Terrain.WATER

        feeling = Feeling.NONE

        return true
    }

    override fun decorate() {}

    override fun createMobs() {}

    override fun respawner() = null

    override fun createItems() {
        drop(Amulet(), xy2cell(13, 5))
    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(HallsLevel::class.java, "water_name")
        Terrain.GRASS -> Messages.get(HallsLevel::class.java, "grass_name")
        Terrain.HIGH_GRASS -> Messages.get(HallsLevel::class.java, "high_grass_name")
        Terrain.STATUE, Terrain.STATUE_SP -> Messages.get(HallsLevel::class.java, "statue_name")
        else -> super.tileName(tile)
    }

    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(HallsLevel::class.java, "water_desc")
        Terrain.STATUE, Terrain.STATUE_SP -> Messages.get(HallsLevel::class.java, "statue_desc")
        Terrain.BOOKSHELF -> Messages.get(HallsLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }

    override fun addVisuals(): Group {
        super.addVisuals()
        KHallsLevel.AddHallsVisuals(this, visuals)
        return visuals
    }

    override fun restoreFromBundle(bundle: Bundle?) {
        super.restoreFromBundle(bundle)
        disableChasms()
    }

    private fun disableChasms() {
        for (i in 0 until length)
            if ((Terrain.flags[map[i]] and Terrain.PIT) != 0) {
                passable[i] = false
                avoid[i] = false
                solid[i] = true
            }
    }
}