package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/19.
 */
class CellDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) =
            Point(Random.HighIntRange(6, 10), Random.HighIntRange(6, 10))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        val maxInner = (Math.min(rect.width, rect.height) - 3) / 2
        val i = Random.NormalIntRange(1, maxInner)
        Fill(level, rect.shrink(i), Terrain.WALL)

        val innerSpace = rect.shrink(i + 1)
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