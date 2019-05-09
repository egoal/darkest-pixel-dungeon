package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.watabou.utils.Point
import com.watabou.utils.Random

class PillarDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall): Point = Point(Random.IntRange(6, 9), Random.IntRange(6, 9))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        // pillar
        val pillars = Random.NormalIntRange(1, 3)
        repeat(pillars) {
            val w = Random.NormalIntRange(1, 3)
            val h = Random.NormalIntRange(1, 3)
            val x = Random.IntRange(rect.x1 + 1, rect.x2 - w)
            val y = Random.IntRange(rect.y1 + 1, rect.y2 - h)
            Companion.Fill(level, x, y, w, h, Terrain.WALL)
        }

        return dr
    }
}