package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon

import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/12.
 */

class ArmoryDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 6), Random.IntRange(4, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)
        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.LOCKED_DOOR)

        Set(level, rect.random(1), Terrain.STATUE)

        val n = Random.IntRange(1, 3)
        for (i in 1..n) {
            var pos: Int
            do {
                pos = level.pointToCell(rect.random())
            } while (level.map[pos] != Terrain.EMPTY || level.heaps.get(pos) != null)
            level.drop(prize(level), pos)
        }

        level.addItemToSpawn(IronKey(Dungeon.depth))

        return DigResult(rect, DigResult.Type.Locked)
    }

    private fun prize(level: Level) = Random.chances(PrizeGenerators).generate()
    
    companion object {
        private val PrizeGenerators =   hashMapOf(
                Generator.ARMOR to 0.75f,
                Generator.WEAPON to 1f,
                Generator.HELMET to 0.45f
        )
    }
}
