package com.egoal.darkestpixeldungeon.levels.diggers.secret

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.RatKing
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Point
import com.watabou.utils.Random

class SecretRatKingDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.Int(3, 6), Random.Int(3, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY_SP)
        val door = level.pointToCell(overlappedWall(wall, rect).random())
        Set(level, door, Terrain.SECRET_DOOR)

        for (x in rect.x1..rect.x2) {
            dropChest(level, x, rect.y1, door)
            dropChest(level, x, rect.y2, door)
        }
        for (y in rect.y1..rect.y2) {
            dropChest(level, rect.x1, y, door)
            dropChest(level, rect.x2, y, door)
        }

        val r = RatKing().apply {
            pos = level.pointToCell(rect.random(1))
        }
        level.mobs.add(r)

        return DigResult(rect, DigResult.Type.Secret)
    }

    private fun dropChest(level: Level, x: Int, y: Int, door: Int) {
        if (door in arrayOf(level.xy2cell(x - 1, y), level.xy2cell(x + 1, y),
                        level.xy2cell(x, y - 1), level.xy2cell(x, y + 1)))
            return

        level.drop(Gold(Random.IntRange(1, 25)), level.xy2cell(x, y)).type = Heap.Type.CHEST
    }
}