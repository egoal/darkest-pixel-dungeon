package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.watabou.utils.Point
import com.watabou.utils.Random

open class RectDigger : Digger() {
    open fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 8), Random.IntRange(4, 8))

    override fun chooseDigArea(wall: Wall): Rect {
        val size = chooseRoomSize(wall)

        var x = -1
        var y = -1
        when (wall.direction) {
            Direction.Left -> {
                x = wall.x1 - size.x
                y = Random.IntRange(wall.y1 - size.y + 1, wall.y1)
            }
            Direction.Right -> {
                x = wall.x2 + 1
                y = Random.IntRange(wall.y1 - size.y + 1, wall.y1)
            }
            Direction.Up -> {
                x = Random.IntRange(wall.x1 - size.x + 1, wall.x1)
                y = wall.y1 - size.y
            }
            Direction.Down -> {
                x = Random.IntRange(wall.x1 - size.x + 1, wall.x1)
                y = wall.y2 + 1
            }
        }

        return Rect.Create(x, y, size.x, size.y)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)

        var walls = Wall.ArroundBut(rect, wall.direction.opposite)

        if (Random.Int(8) == 0)
            Fill(level, overlappedWall(wall, rect), Terrain.EMPTY)
        else {
            Set(level, overlappedWall(wall, rect).random(), Terrain.DOOR)

            // add remain wall is longer enough
            val remainWall = unoverlappedWall(wall, rect)
            if (remainWall.width >= 3)
                walls = walls.plus(remainWall)
        }

        return DigResult(rect, walls)
    }

    protected fun overlappedWall(wall: Wall, rect: Rect) =
            if (wall.direction.horizontal)
                Rect(wall.x1, wall.x2, Math.max(wall.y1, rect.y1), Math.min(wall.y2, rect.y2))
            else
                Rect(Math.max(wall.x1, rect.x1), Math.min(wall.x2, rect.x2), wall.y1, wall.y2)

    // only return the longer one
    protected fun unoverlappedWall(wall: Wall, rect: Rect): Wall = when {
        wall.direction.horizontal -> {
            val rleft = Wall(wall.x1, rect.x1 - 1, wall.y1, wall.y2, wall.direction)
            val rright = Wall(rect.x2 + 1, wall.x2, wall.y1, wall.y2, wall.direction)
            if (rleft.width > rright.width) rleft else rright
        }
        else -> {
            val rlow = Wall(wall.x1, wall.x2, wall.y1, rect.y1 - 1, wall.direction)
            val rhigh = Wall(wall.x1, wall.x2, rect.y2 + 1, wall.y2, wall.direction)
            if (rlow.height > rhigh.height) rlow else rhigh
        }
    }

}