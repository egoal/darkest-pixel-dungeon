package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/17.
 */

class LatticeDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(1, 4) * 2 + 1,
            Random.IntRange(1, 4) * 2 + 1)


    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        // lattice paint
        val tile = if (Random.Int(4) == 0) Terrain.CHASM else Terrain.WALL

        for (r in 1 until rect.height step 2)
            for (c in 1 until rect.width step 2)
                Set(level, rect.x1 + c, rect.y1 + r, tile)

        return dr
    }
}
