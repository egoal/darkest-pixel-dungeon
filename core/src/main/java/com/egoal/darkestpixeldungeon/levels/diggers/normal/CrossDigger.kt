package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.watabou.utils.Random

class CrossDigger : Digger() {
    override fun chooseDigArea(wall: Wall) =
            chooseCenteredRect(wall, Random.IntRange(3, 4) * 2 + 1, Random.IntRange(3, 4) * 2 + 1)

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dw = Math.max((rect.width - 3) / 2, 2)
        val dh = Math.max((rect.height - 3) / 2, 2)
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
        val door = rect.center
        if (wall.direction.horizontal)
            door.x = wall.x1
        else door.y = wall.y1
        Set(level, door, Terrain.DOOR)

        val walls = ArrayList<Wall>()
        if (wall.direction.opposite != Direction.Left)
            walls.add(Wall.Left(rect.x1 - 1, rect.y1 + dh, rect.y2 - dh))
        if (wall.direction.opposite != Direction.Right)
            walls.add(Wall.Right(rect.x2 + 1, rect.y1 + dh, rect.y2 - dh))
        if (wall.direction.opposite != Direction.Up)
            walls.add(Wall.Up(rect.y1 - 1, rect.x1 + dw, rect.x2 - dw))
        if (wall.direction.opposite != Direction.Down)
            walls.add(Wall.Down(rect.y2 + 1, rect.x1 + dw, rect.x2 - dw))

        return DigResult(rect, walls.toList(), DigResult.Type.Special)
    }
}