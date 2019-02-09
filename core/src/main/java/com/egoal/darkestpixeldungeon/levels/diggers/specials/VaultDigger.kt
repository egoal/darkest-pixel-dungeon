package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.keys.GoldenKey
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.PathFinder
import com.watabou.utils.Point
import com.watabou.utils.Random

class VaultDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 6), Random.IntRange(4, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY_SP)
        Fill(level, rect.shrink(1), Terrain.EMPTY)

        // door
        val dp = level.pointToCell(overlappedWall(wall, rect).random())
        Set(level, dp, Terrain.LOCKED_DOOR)
        level.addItemToSpawn(IronKey(Dungeon.depth))

        // prize
        val c = level.pointToCell(rect.center)
        when (Random.Int(3)) {
            0 -> {
                level.drop(prize(level), c).type = Heap.Type.LOCKED_CHEST
                level.addItemToSpawn(GoldenKey(Dungeon.depth))
            }
            1 -> {
                // two different categories
                val c1 = Random.oneOf(Generator.Category.WAND, Generator.Category.RING,
                        Generator.Category.ARTIFACT)
                var c2 = Random.oneOf(Generator.Category.WAND, Generator.Category.RING,
                        Generator.Category.ARTIFACT)
                while (c2 == c1)
                    c2 = Random.oneOf(Generator.Category.WAND, Generator.Category.RING,
                            Generator.Category.ARTIFACT)

                val i1 = Generator.random(c1)
                val i2 = Generator.random(c2)
                level.drop(i1, c).type = Heap.Type.CRYSTAL_CHEST
                level.drop(i2, c + PathFinder.NEIGHBOURS4[Random.Int(4)]).type = Heap.Type.CRYSTAL_CHEST
                level.addItemToSpawn(GoldenKey(Dungeon.depth))
            }
            2 -> {
                level.drop(prize(level), c)
                Set(level, c, Terrain.PEDESTAL)
            }
        }

        return DigResult(rect, DigResult.Type.Locked)
    }

    private fun prize(level: Level) = Random.oneOf(KGenerator.WAND, KGenerator.RING, KGenerator.ARTIFACT).generate()

}