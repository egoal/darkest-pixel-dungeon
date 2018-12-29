package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/9.
 */

class WeakFloorDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 8), Random.IntRange(4, 8))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.CHASM)
        Set(level, overlappedWall(wall, rect).random(), Terrain.DOOR)


        val well = rect.center
        val walls = arrayListOf<Wall>()
        when (wall.direction) {
            Direction.Left -> {
                val cx = rect.x2 - rect.width / 2 + 1
                Fill(level, cx, rect.y1, rect.width / 2, rect.height, Terrain.EMPTY_SP)
                well.x = rect.x1

                walls.add(Wall.Up(rect.y1 - 1, cx, rect.x2))
                walls.add(Wall.Down(rect.y2 + 1, cx, rect.x2))
            }
            Direction.Right -> {
                Fill(level, rect.x1, rect.y1, rect.width / 2, rect.height, Terrain.EMPTY_SP)
                well.x = rect.x2

                walls.add(Wall.Up(rect.y1 - 1, rect.x1, rect.x1 + rect.width / 2 - 1))
                walls.add(Wall.Up(rect.y2 + 1, rect.x1, rect.x1 + rect.width / 2 - 1))
            }
            Direction.Up -> {
                val cy = rect.y2 - rect.height / 2 + 1
                Fill(level, rect.x1, cy, rect.width, rect.height / 2, Terrain.EMPTY_SP)
                well.y = rect.y1 + 1 // +1: better visual 

                walls.add(Wall.Left(rect.x1 - 1, cy, rect.y2))
                walls.add(Wall.Right(rect.x2 + 1, cy, rect.y2))
            }
            Direction.Down -> {
                Fill(level, rect.x1, rect.y1, rect.width, rect.height / 2, Terrain.EMPTY_SP)
                well.y = rect.y2

                walls.add(Wall.Left(rect.x1 - 1, rect.y1, rect.y1 + rect.height / 2 - 1))
                walls.add(Wall.Right(rect.x2 + 1, rect.y1, rect.y1 + rect.height / 2 - 1))
            }
        }

        val cts = HiddenWell()
        cts.pos(well.x, well.y)
        level.customTiles.add(cts)

        return DigResult(rect, walls, DigResult.Type.WeakFloor)
    }

    class HiddenWell : CustomTileVisual() {
        init {
            name = Messages.get(this, "name")

            tx = Assets.WEAK_FLOOR
            txX = Dungeon.depth / 5
            txY = 0
        }

        override fun desc(): String? {
            return Messages.get(this, "desc")
        }
    }
}
