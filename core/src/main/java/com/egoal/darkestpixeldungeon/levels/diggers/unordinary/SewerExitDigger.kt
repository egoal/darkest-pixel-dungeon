package com.egoal.darkestpixeldungeon.levels.diggers.unordinary

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Direction
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Point
import com.watabou.utils.Random

class SewerExitDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall): Point = Point(Random.IntRange(6, 8), Random.IntRange(6, 8))

    override fun chooseDigArea(wall: Wall): Rect {
        // cannot down to this room.
        if (wall.direction == Direction.Down) return Rect(-1, -1, -1, -1)
        
        return super.chooseDigArea(wall)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)
        LinkHorizontal(level, rect.y1, rect.x1, rect.x2, Terrain.WALL_DECO)

        Set(level, overlappedWall(wall, rect.shrink(1)).random(), Terrain.DOOR)

        val walls = Wall.Arround(rect,
                listOf(Direction.Left, Direction.Right, Direction.Down).filter { it != wall.direction.opposite })

        level.entrance = level.pointToCell(rect.random(1))
        level.exit = level.xy2cell((rect.x1 + rect.x2) / 2, rect.y1)

        return DigResult(rect, walls, DigResult.Type.Entrance)
    }
}