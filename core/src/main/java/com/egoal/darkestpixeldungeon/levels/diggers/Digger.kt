package com.egoal.darkestpixeldungeon.levels.diggers

import com.egoal.darkestpixeldungeon.levels.Level
import com.watabou.utils.Point
import com.watabou.utils.Random
import kotlin.collections.ArrayList

data class DigResult(var rect: Rect, var walls: List<Wall>, val type: Type = Type.Normal) {
    constructor(rect: Rect, type: Type) : this(rect, listOf(), type)
    
    enum class Type {
        Normal, Special, Locked,
        Pit, WeakFloor,
        Exit, Entrance,
    }
}

abstract class Digger {
    // assists 
    companion object {
        fun Set(level: Level, cell: Int, tile: Int) {
            level.map[cell] = tile
        }

        fun Set(level: Level, x: Int, y: Int, tile: Int) {
            Set(level, level.xy2cell(x, y), tile)
        }

        fun Set(level: Level, p: Point, tile: Int) {
            Set(level, level.pointToCell(p), tile)
        }

        fun Fill(level: Level, rect: Rect, tile: Int) {
            for (x in rect.x1..rect.x2)
                for (y in rect.y1..rect.y2)
                    Set(level, x, y, tile)
        }

        fun Fill(level: Level, x: Int, y: Int, w: Int, h: Int, tile: Int) {
            Fill(level, Rect.Create(x, y, w, h), tile)
        }

        fun LinkVertical(level: Level, x: Int, y1: Int, y2: Int, tile: Int) {
            when {
                y1 <= y2 -> for (y in y1..y2) Set(level, x, y, tile)
                else -> for (y in y2..y1) Set(level, x, y, tile)
            }
        }

        fun LinkHorizontal(level: Level, y: Int, x1: Int, x2: Int, tile: Int) {
            when {
                x1 <= x2 -> for (x in x1..x2) Set(level, x, y, tile)
                else -> for (x in x2..x1) Set(level, x, y, tile)
            }
        }

        fun RandomLink(level: Level, x1: Int, y1: Int, x2: Int, y2: Int, tile: Int) {
            val dx = if (x1 > x2) -1 else 1
            val dy = if (y1 > y2) -1 else 1

            val steps = ArrayList<Point>()

            for (i in 1..Math.abs(x1 - x2))
                steps.add(Point(dx, 0))
            for (i in 1..Math.abs(y1 - y2))
                steps.add(Point(0, dy))

            steps.shuffle()

            var x = x1
            var y = y1
            for (p in steps) {
                x += p.x
                y += p.y

                Set(level, x, y, tile)
            }
        }

        fun RandomLink(level: Level, s: Point, e: Point, tile: Int) {
            RandomLink(level, s.x, s.y, e.x, e.y, tile)
        }

        fun All(level: Level, rect: Rect, tile: Int): Boolean {
            for (x in rect.x1..rect.x2)
                for (y in rect.y1..rect.y2)
                    if (level.map[level.xy2cell(x, y)] != tile) return false

            return true
        }
    }

    // digger class
    // choose digger area
    abstract fun chooseDigArea(wall: Wall): Rect

    abstract fun dig(level: Level, wall: Wall, rect: Rect): DigResult

    protected fun chooseCenteredRect(wall: Wall, w: Int, h: Int): Rect {
        // door is centered
        var x = -1
        var y = -1
        when (wall.direction) {
            Direction.Left -> {
                x = wall.x1 - 1
                y = Random.IntRange(wall.y1, wall.y2) - h / 2
            }
            Direction.Right -> {
                x = wall.x2 + 1
                y = Random.IntRange(wall.y1, wall.y2) - h / 2
            }
            Direction.Up -> {
                x = Random.IntRange(wall.x1, wall.x2) - w / 2
                y = wall.y1 - h
            }
            Direction.Down -> {
                x = Random.IntRange(wall.x1, wall.x2) - w / 2
                y = wall.y2 + 1
            }
        }

        return Rect.Create(x, y, w, h)
    }
}