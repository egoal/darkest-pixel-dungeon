package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Questioner
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger

/**
 * Created by 93942 on 2018/12/17.
 */

class QuestionerDigger : RectDigger() {
    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)

        // no door
        val q = Questioner().random().hold(rect)
        q.pos = level.pointToCell(overlappedWall(wall, rect).random())
        Set(level, q.pos, Terrain.WALL_SPECIAL)
        level.mobs.add(q)

        return DigResult(rect, DigResult.Type.Locked)
    }
}
