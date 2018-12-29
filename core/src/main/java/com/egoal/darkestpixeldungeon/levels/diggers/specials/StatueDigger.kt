package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.Statue
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Direction
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/12.
 */

class StatueDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 6), Random.IntRange(4, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)

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

        val s = Statue()
        s.pos = level.pointToCell(plat)
        level.mobs.add(s)

        level.addItemToSpawn(IronKey(Dungeon.depth))

        return DigResult(rect, DigResult.Type.Locked)
    }
}
