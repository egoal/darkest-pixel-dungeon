package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.XRect
import com.egoal.darkestpixeldungeon.levels.diggers.XWall
import com.watabou.utils.PathFinder

/**
 * Created by 93942 on 2018/12/19.
 */
class BrightDigger : NormalRectDigger() {
    override fun dig(level: Level, wall: XWall?, rect: XRect): DigResult {
        val dr = super.dig(level, wall, rect)

        val ccen = level.pointToCell(rect.cen())
        Set(level, ccen, Terrain.WALL_LIGHT_ON)
        for (i in PathFinder.NEIGHBOURS8)
            Set(level, ccen + i, Terrain.EMPTY_SP)

        return dr
    }
}