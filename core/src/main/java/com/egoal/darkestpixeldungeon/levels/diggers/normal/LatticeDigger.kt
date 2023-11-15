package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.KRandom
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.watabou.utils.Point
import com.watabou.utils.Random

class LatticeDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(1, 4) * 2 + 1,
            Random.IntRange(1, 4) * 2 + 1)

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)
        for (r in 1 until rect.height step 2)
            for (c in 1 until rect.width step 2)
                Set(level, rect.x1 + c, rect.y1 + r, getLatticeTile())

        return dr
    }

    private fun getLatticeTile(): Int {
        val pmap = mapOf(
                Terrain.CHASM to 1f,
                Terrain.WALL to 1f,
                Terrain.EMPTY to 0.5f,
                Terrain.WALL_LIGHT_ON to 0.5f,
        )
        return KRandom.Chances(pmap)!!
    }
}
