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

        val w = Random.NormalIntRange(4, rect.width - 2)
        val h = Random.NormalIntRange(4, rect.height - 2)
        val x = Random.IntRange(rect.x1 + 1, rect.x2 - w)
        val y = Random.IntRange(rect.y1 + 1, rect.y2 - h)
        val r = Rect.Create(x, y, w, h)

        Fill(level, r, Terrain.EMPTY_SP)
        Fill(level, r.shrink(1), Terrain.EMPTY)
        Fill(level, r.shrink(1), Terrain.WATER)

        level.mobs.add(Piranha().apply {
            pos = level.pointToCell(rect.center)
        })

        return dr
    }
}