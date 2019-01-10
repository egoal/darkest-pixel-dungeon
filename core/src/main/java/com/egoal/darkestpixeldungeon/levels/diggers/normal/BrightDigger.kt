package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/19.
 */
class BrightDigger : Digger() {
    private val shapeDigger = when (Random.Int(8)) {
        0 -> RoundDigger()
        1 -> DiamondDigger()
        else -> RectDigger()
    }

    override fun chooseDigArea(wall: Wall) = shapeDigger.chooseDigArea(wall)

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = shapeDigger.dig(level, wall, rect)

        // put light centered
        val cen = level.pointToCell(rect.center)
        Set(level, cen, Terrain.WALL_LIGHT_ON)

        val tile = if (rect.width > 5 && rect.height > 5) {
            if (Random.Int(2) == 0) Terrain.EMPTY_SP else Terrain.CHASM
        } else if (Random.Int(3) != 0) Terrain.EMPTY_SP
        else Terrain.EMPTY
        
        for (i in PathFinder.NEIGHBOURS8)
            Set(level, cen + i, tile)

        return dr
    }
}