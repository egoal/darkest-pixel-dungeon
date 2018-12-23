package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.XRect
import com.egoal.darkestpixeldungeon.levels.diggers.XWall
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/23.
 */
class StripDigger : RectDigger() {
    override fun chooseRoomSize(wall: XWall): Point {
        val width = Random.IntRange(3, 6)
        val len = Random.IntRange(5, 9)

        return if (wall.direction == Digger.LEFT || wall.direction == Digger.RIGHT)
            Point(len, width)
        else
            Point(width, len)
    }

    override fun dig(level: Level, wall: XWall, rect: XRect): DigResult {
        val pattern = strip()

        Set(level, overlapedWall(wall, rect).random(), Terrain.DOOR)
        
        return when (wall.direction) {
            Digger.LEFT, Digger.RIGHT -> {
                for (x in rect.x1..rect.x2)
                    Digger.LinkV(level, x, rect.y1, rect.y2, if (x % 2 == 0) pattern.x else pattern.y)
                DigResult(DigResult.Type.NORMAL).walls(walls(rect, wall.direction))
            }
            else -> {
                for (y in rect.y1..rect.y2)
                    Digger.LinkH(level, y, rect.x1, rect.x2, if (y % 2 == 0) pattern.x else pattern.y)
                DigResult(DigResult.Type.NORMAL).walls(walls(rect, wall.direction))
            }
        }
    }

    private fun strip(): Point {
        // empty, empty_sp, grass
        return when (Random.Int(4)) {
            0, 1 -> Point(Terrain.EMPTY_SP, Terrain.HIGH_GRASS)
            2 -> Point(Terrain.EMPTY, Terrain.HIGH_GRASS)
            else -> Point(Terrain.EMPTY, Terrain.EMPTY_SP)
        }
    }

}