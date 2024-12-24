package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.actors.mobs.Piranha
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.watabou.utils.Point
import com.watabou.utils.Random

class PuddleDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(6, 8), Random.IntRange(6, 8))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)

        val w = Random.NormalIntRange(2, rect.width - 4)
        val h = Random.NormalIntRange(2, rect.height - 4)
        val x = Random.IntRange(rect.x1 + 2, rect.x2 - w - 1)
        val y = Random.IntRange(rect.y1 + 2, rect.y2 - h - 1)

        Fill(level, x, y, w, h, Terrain.EMPTY_SP)
        Fill(level, x + 1, y + 1, w - 2, h - 2, Terrain.WATER)

        level.mobs.add(Piranha().apply {
            pos = level.pointToCell(rect.center)
        })

        return dr
    }
}