package com.egoal.darkestpixeldungeon.levels.diggers

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/19.
 */
class BrightDigger : NormalRoomDigger() {
    override fun dig(level: Level, wall: XWall?, rect: XRect): DigResult {
        val dr = super.dig(level, wall, rect)

        val ccen = level.pointToCell(rect.cen())
        Set(level, ccen, Terrain.WALL_LIGHT_ON)
        for (i in PathFinder.NEIGHBOURS8)
            Set(level, ccen + i, Terrain.EMPTY_SP)

        val n = Math.min(Random.NormalIntRange(0, (rect.area() - 9) / 9), 3)
        for (i in 0 until n) {
            var c = -1
            do {
                c = level.pointToCell(rect.random())
            } while (level.map[c] != Terrain.EMPTY)
            Set(level, c, Terrain.WALL_LIGHT_ON)
        }

        return dr
    }
}