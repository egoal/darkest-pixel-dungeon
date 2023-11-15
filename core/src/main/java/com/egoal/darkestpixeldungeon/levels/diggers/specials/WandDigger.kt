package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.WandGuard
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.PathFinder
import com.watabou.utils.Point
import com.watabou.utils.Random

class WandDigger : RectDigger() {
    val guard = WandGuard()

    override fun chooseRoomSize(wall: Wall): Point {
        val x = Random.IntRange(5, 9)
        val y = Random.IntRange(5, 9)

        return if (wall.direction.vertical)
            if (x > y) Point(y, x) else Point(x, y)
        else
            if (x > y) Point(x, y) else Point(y, x)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        if (rect.width >= 5 && rect.height >= 5 && rect.area >= 30) return digCentered(level, wall, rect)

        Fill(level, rect, Terrain.EMPTY_SP)
        val lastRowTile = Terrain.STATUE
        val plat = rect.center
        when (wall.direction) {
            Direction.Left -> {
                LinkVertical(level, rect.x1, rect.y1, rect.y2, lastRowTile)
                plat.x = rect.x1 + 1
            }
            Direction.Right -> {
                LinkVertical(level, rect.x2, rect.y1, rect.y2, lastRowTile)
                plat.x = rect.x2 - 1
            }
            Direction.Up -> {
                LinkHorizontal(level, rect.y1, rect.x1, rect.x2, lastRowTile)
                plat.y = rect.y1 + 1
            }
            Direction.Down -> {
                LinkHorizontal(level, rect.y2, rect.x1, rect.x2, lastRowTile)
                plat.y = rect.y2 - 1
            }
        }

        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.LOCKED_DOOR)

        val cen = level.pointToCell(plat)
        PathFinder.NEIGHBOURS8.map { it + cen }.forEach {
            if (level.map[it] == Terrain.EMPTY)
                level.map[it] = Terrain.EMPTY_SP
        }

        guard.pos = cen
        level.mobs.add(guard)

        level.addItemToSpawn(IronKey(Dungeon.depth))

        return DigResult(rect, DigResult.Type.Locked)
    }


    private fun digCentered(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)
        val cen = rect.center
        val ceni = level.pointToCell(cen)
        for (i in PathFinder.NEIGHBOURS9) Set(level, i + ceni, Terrain.EMPTY_SP)

        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.DOOR)

        guard.pos = ceni
        level.mobs.add(guard)

        return DigResult(rect, Wall.ArroundBut(rect, wall.direction.opposite), DigResult.Type.Special)
    }
}