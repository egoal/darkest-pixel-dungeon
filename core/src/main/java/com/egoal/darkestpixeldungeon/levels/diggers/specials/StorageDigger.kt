package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.unclassified.Honeypot
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLiquidFlame
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/8.
 */

class StorageDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 6), Random.IntRange(4, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY_SP)

        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.BARRICADE)

        val hp = Random.Int(2) == 0
        if (hp) {
            var pos: Int
            do {
                pos = level.pointToCell(rect.random())
            } while (level.map[pos] != Terrain.EMPTY_SP)
            level.drop(Honeypot(), pos)
        }

        var n = Random.IntRange(3, 4)
        if (hp) --n
        for (i in 1..n) {
            var pos: Int
            do {
                pos = level.pointToCell(rect.random())
            } while (level.map[pos] != Terrain.EMPTY_SP)
            level.drop(prize(level), pos)
        }

        level.addItemToSpawn(PotionOfLiquidFlame())

        return DigResult(rect, DigResult.Type.Locked)
    }

    private fun prize(level: Level): Item? {
        if (Random.Int(2) != 0) {
            val prize = level.findPrizeItem()
            if (prize != null)
                return prize
        }

        return Generator.random(Random.oneOf<Generator.Category>(
                Generator.Category.POTION, Generator.Category.SCROLL,
                Generator.Category.FOOD, Generator.Category.GOLD))
    }
}
