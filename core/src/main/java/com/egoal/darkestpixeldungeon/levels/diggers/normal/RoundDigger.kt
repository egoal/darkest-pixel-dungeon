package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.watabou.utils.PathFinder
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/20.
 */

open class RoundDigger : Digger() {
    override fun chooseDigArea(wall: Wall): Rect {
        val size = Random.IntRange(3, 5) * 2 + 1
        return chooseCenteredRect(wall, size, size)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val cen = rect.center
        val hs2 = (rect.width / 2) * (rect.width / 2)
        for (p in rect.getAllPoints())
            if (Point.DistanceL22(cen, p) <= hs2)
                Set(level, p, Terrain.EMPTY)

        rect.getAllPoints()
                .filter {
                    val i = level.pointToCell(it)
                    level.map[i] == Terrain.WALL &&
                            PathFinder.NEIGHBOURS4.any { level.map[i + it] == Terrain.EMPTY }
                }.filter {
                    Random.Float() < .3f
                }.forEach {
                    Set(level, it, Terrain.EMPTY)
                }
        rect.getAllPoints().filter {
            val i = level.pointToCell(it)
            level.map[i] == Terrain.WALL && PathFinder.NEIGHBOURS4.count { level.map[i + it] == Terrain.EMPTY } == 3
        }.forEach {
            Set(level, it, Terrain.EMPTY)
        }

        val door = rect.center
        if (wall.direction.horizontal)
            door.x = wall.x1
        else door.y = wall.y1
        Set(level, door, Terrain.DOOR)

        val walls = ArrayList<Wall>()
        if (wall.direction.opposite != Direction.Left)
            walls.add(Wall(rect.x1 - 1, cen.y, Direction.Left))
        if (wall.direction.opposite != Direction.Right)
            walls.add(Wall(rect.x2 + 1, cen.y, Direction.Right))
        if (wall.direction.opposite != Direction.Up)
            walls.add(Wall(cen.x, rect.y1 - 1, Direction.Up))
        if (wall.direction.opposite != Direction.Down)
            walls.add(Wall(cen.x, rect.y2 + 1, Direction.Down))

        return DigResult(rect, walls.toList(), DigResult.Type.Special)
    }
}
