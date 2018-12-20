package com.egoal.darkestpixeldungeon.levels.diggers

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/19.
 */
class NormalCellDigger : NormalRectDigger() {
    override fun chooseRoomSize(wall: XWall?): Point {
        return Point(Random.IntRange(5, 9), Random.IntRange(5, 9))
    }

    override fun dig(level: Level?, wall: XWall?, rect: XRect): DigResult {
        val dr = super.dig(level, wall, rect)

        val maxInner = (Math.min(rect.w(), rect.h()) - 3) / 2
        val i = Random.LowIntRange(1, maxInner)
        Fill(level, rect.inner(i), Terrain.WALL)

        val innerSpace = rect.inner(i + 1)
        Fill(level, innerSpace, Terrain.EMPTY)
        val innerDoor = innerSpace.random()
        when (Random.Int(4)) {
            0 -> innerDoor.x = innerSpace.x1 - 1
            1 -> innerDoor.x = innerSpace.x2 + 1
            2 -> innerDoor.y = innerSpace.y1 - 1
            else -> innerDoor.y = innerSpace.y2 + 1
        }
        Set(level, innerDoor, Terrain.DOOR)

        return dr
    }
}