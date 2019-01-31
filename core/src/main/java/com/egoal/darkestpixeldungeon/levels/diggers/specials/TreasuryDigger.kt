package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/8.
 */

class TreasuryDigger : RectDigger() {

    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 6), Random.IntRange(4, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)
        Set(level, rect.center, Terrain.STATUE)

        Set(level, overlappedWall(wall, rect).random(), Terrain.LOCKED_DOOR)
        level.addItemToSpawn(IronKey(Dungeon.depth))

        val ht = if (Random.Int(2) == 0) Heap.Type.CHEST else Heap.Type.HEAP
        val n = Random.IntRange(2, 3)
        for (i in 0 until n) {
            var pos: Int
            do {
                pos = level.pointToCell(rect.random())
            } while (level.map[pos] != Terrain.EMPTY || level.heaps.get(pos) != null)
            level.drop(prize(level), pos).type =
                    if (ht == Heap.Type.CHEST && Random.Int(10) == 0) Heap.Type.MIMIC else ht
        }

        if (ht == Heap.Type.HEAP) {
            // some little gold
            for (i in 0..5) {
                var pos: Int
                do {
                    pos = level.pointToCell(rect.random())
                } while (level.map[pos] != Terrain.EMPTY)
                level.drop(Gold(Random.IntRange(5, 20)), pos)
            }
        }


        return DigResult(rect, DigResult.Type.Locked)
    }

    private fun prize(level: Level): Item {
        return Gold().random()
    }

}
