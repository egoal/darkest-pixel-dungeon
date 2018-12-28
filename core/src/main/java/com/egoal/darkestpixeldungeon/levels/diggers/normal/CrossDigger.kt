package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.XRect
import com.egoal.darkestpixeldungeon.levels.diggers.XWall
import com.watabou.utils.Random

class CrossDigger : Digger() {
    override fun chooseDigArea(wall: XWall) =
            chooseCenteredRect(wall, Random.IntRange(3, 4) * 2 + 1, Random.IntRange(3, 4) * 2 + 1)

    override fun dig(level: Level, wall: XWall, rect: XRect): DigResult {
        val dw = Math.max((rect.w() - 3) / 2, 2)
        val dh = Math.max((rect.h() - 3) / 2, 2)
        Fill(level, rect, Terrain.EMPTY)
        for (i in 0 until dw)
            for (j in 0 until dh) {
                Set(level, rect.x1 + i, rect.y1 + j, Terrain.WALL)
                Set(level, rect.x2 - i, rect.y1 + j, Terrain.WALL)
                Set(level, rect.x1 + i, rect.y2 - j, Terrain.WALL)
                Set(level, rect.x2 - i, rect.y2 - j, Terrain.WALL)
            }

        // lights
        Set(level, rect.x1 + dw - 1, rect.y1 + dh - 1, if (Random.Int(2) == 0) Terrain.WALL_LIGHT_ON else Terrain.WALL_LIGHT_OFF)
        Set(level, rect.x2 - dw + 1, rect.y1 + dh - 1, if (Random.Int(2) == 0) Terrain.WALL_LIGHT_ON else Terrain.WALL_LIGHT_OFF)
        Set(level, rect.x1 + dw - 1, rect.y2 - dh + 1, if (Random.Int(2) == 0) Terrain.WALL_LIGHT_ON else Terrain.WALL_LIGHT_OFF)
        Set(level, rect.x2 - dw + 1, rect.y2 - dh + 1, if (Random.Int(2) == 0) Terrain.WALL_LIGHT_ON else Terrain.WALL_LIGHT_OFF)

        // door
        val pd = rect.cen()
        when (wall.direction) {
            LEFT, RIGHT -> pd.x = wall.x1
            else -> pd.y = wall.y1
        }
        Set(level, pd, Terrain.DOOR)

        val dr = DigResult(DigResult.Type.SPECIAL)
        if (-wall.direction != LEFT)
            dr.walls.add(XWall.Left(rect.x1 - 1, rect.y1 + dh, rect.y2 - dh))
        if (-wall.direction != RIGHT)
            dr.walls.add(XWall.Right(rect.x2 + 1, rect.y1 + dh, rect.y2 - dh))
        if (-wall.direction != UP)
            dr.walls.add(XWall.Up(rect.y1 - 1, rect.x1 + dw, rect.x2 - dw))
        if (-wall.direction != DOWN)
            dr.walls.add(XWall.Down(rect.y2 + 1, rect.x1 + dw, rect.x2 - dw))

        return dr
    }
}