package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/8.
 */

class PitDigger : RectDigger() {
    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)

        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.LOCKED_DOOR)

        val well = when (wall.direction) {
            Direction.Left -> level.xy2cell(rect.x1, rect.center.y)
            Direction.Right -> level.xy2cell(rect.x2, rect.center.y)
            Direction.Up -> level.xy2cell(rect.center.x, rect.y1)
            Direction.Down -> level.xy2cell(rect.center.x, rect.y2)
        }
        Set(level, well, Terrain.EMPTY_WELL)

        // items
        var remains = level.pointToCell(rect.random())
        while (level.map[remains] != Terrain.EMPTY)
            remains = level.pointToCell(rect.random())

        level.drop(IronKey(Dungeon.depth), remains).type = Heap.Type.SKELETON

        when (Random.Int(3)) {
            0 -> level.drop(KGenerator.RING.generate(), remains)
            1 -> level.drop(KGenerator.ARTIFACT.generate(), remains)
            else -> level.drop(Random.oneOf(KGenerator.WEAPON, KGenerator.ARMOR).generate(), remains)
        }

        // extra drop
        val n = Random.IntRange(1, 2)
        for (i in 1..n) level.drop(prize(level), remains)

        return DigResult(rect, DigResult.Type.Pit)
    }

    private fun prize(level: Level): Item {
        if (Random.Int(2) == 0) {
            val prize = level.findPrizeItem()
            if (prize != null) return prize
        }

        return Random.oneOf(KGenerator.POTION, KGenerator.SCROLL, KGenerator.FOOD, KGenerator.GOLD).generate()
    }
}
