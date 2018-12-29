package com.egoal.darkestpixeldungeon.levels.diggers

import com.watabou.utils.*

open class Rect(var x1: Int = 0, var x2: Int = 0, var y1: Int = 0, var y2: Int = 0) : Bundlable {

    val width: Int get() = x2 - x1 + 1
    val height: Int get() = y2 - y2 + 1
    val area: Int get() = width * height
    val center: Point get() = Point((x1 + x2) / 2, (y1 + y2) / 2)

    val valid: Boolean get() = x1 <= x2 && y1 <= y2

    fun random(inner: Int = 0) =
            Point(Random.IntRange(x1 + inner, x2 - inner), Random.IntRange(y1 + inner, y2 - inner))

    fun shrink(inner: Int) =
            Rect(x1 + inner, x2 - inner, y1 + inner, y2 - inner)

    fun inside(pt: Point) = pt.x in x1..x2 && pt.y in y1..y2

    fun getAllPoints(): HashSet<Point> {
        val points = HashSet<Point>()
        for (x in x1..x2)
            for (y in y1..y2)
                points.add(Point(x, y))
        return points
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put("x1", x1)
        bundle.put("x2", x2)
        bundle.put("y1", y1)
        bundle.put("y2", y2)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        x1 = bundle.getInt("x1")
        x2 = bundle.getInt("x2")
        y1 = bundle.getInt("y1")
        y2 = bundle.getInt("y2")
    }

    companion object {
        fun Create(left: Int, top: Int, width: Int, height: Int) =
                Rect(left, left + width - 1, top, top + height - 1)

        fun Overlap(one: Rect, other: Rect) =
                Rect(Math.max(one.x1, other.x1), Math.min(one.x2, other.x2),
                        Math.max(one.y1, other.y1), Math.min(one.y2, other.y2))
    }
}

// wall: rect with one more property: direction
enum class Direction {
    Left {
        override val opposite: Direction get() = Right
    },
    Right {
        override val opposite: Direction get() = Left
    },
    Up {
        override val opposite: Direction get() = Down
    },
    Down {
        override val opposite: Direction get() = Up
    };

    abstract val opposite: Direction
    val horizontal: Boolean get() = this == Right || this == Left
    val vertical: Boolean get() = this == Up || this == Down

}


class Wall(x1: Int, x2: Int, y1: Int, y2: Int, val direction: Direction) : Rect(x1, x2, y1, y2) {
    constructor(x: Int, y: Int, direction: Direction) : this(x, x, y, y, direction)

    // helps
    companion object {
        fun Left(x: Int, y1: Int, y2: Int) = Wall(x, x, y1, y2, Direction.Left)

        fun Right(x: Int, y1: Int, y2: Int) = Wall(x, x, y1, y2, Direction.Right)

        fun Up(y: Int, x1: Int, x2: Int) = Wall(x1, x2, y, y, Direction.Up)

        fun Down(y: Int, x1: Int, x2: Int) = Wall(x1, x2, y, y, Direction.Down)

        fun Arround(rect: Rect, directions: List<Direction>): List<Wall> =
                directions.map {
                    when (it) {
                        Direction.Left -> Left(rect.x1 - 1, rect.y1, rect.y2)
                        Direction.Right -> Right(rect.x2 + 1, rect.y1, rect.y2)
                        Direction.Up -> Up(rect.y1 - 1, rect.x1, rect.x2)
                        Direction.Down -> Up(rect.y2 + 1, rect.x1, rect.x2)
                    }
                }

        fun ArroundBut(rect: Rect, direction: Direction) =
                Arround(rect, listOf(Direction.Left, Direction.Right, Direction.Up, Direction.Down).filter {
                    it != direction
                })
    }
}