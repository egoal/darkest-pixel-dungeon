package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/23.
 */
class StripDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall): Point {
        val width = Random.IntRange(3, 6)
        val len = Random.IntRange(5, 9)

        return if (wall.direction.horizontal) Point(len, width) else Point(width, len)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val pattern = strip()

        Set(level, overlappedWall(wall, rect).random(), Terrain.DOOR)

        if (wall.direction.horizontal)
            for (x in rect.x1..rect.x2)
                Digger.LinkVertical(level, x, rect.y1, rect.y2, if (x % 2 == 0) pattern.x else pattern.y)
        else
            for (y in rect.y1..rect.y2)
                Digger.LinkHorizontal(level, y, rect.x1, rect.x2, if (y % 2 == 0) pattern.x else pattern.y)
        
        return DigResult(rect, Wall.Arround(rect, listOf(wall.direction)), DigResult.Type.Normal)
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