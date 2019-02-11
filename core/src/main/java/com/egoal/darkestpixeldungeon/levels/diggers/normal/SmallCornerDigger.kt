package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.watabou.utils.Point
import com.watabou.utils.Random

class SmallCornerDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall): Point = Point(3, 3)

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        val cen = when (Random.Int(10)) {
            in 0..2 -> Terrain.EMPTY
            3 -> Terrain.EMPTY_WELL
            4 -> Terrain.CHASM
            else -> Terrain.WALL
        }
        Set(level, rect.center, cen)

        return dr
    }

}