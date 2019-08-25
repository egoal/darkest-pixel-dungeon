package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.Alchemy

import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Direction
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.PathFinder
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 12/4/2018.
 */

class LaboratoryDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 6), Random.IntRange(4, 6))


    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY_SP)

        // lock the door
        val door = overlappedWall(wall, rect).random(0)
        Set(level, door, Terrain.LOCKED_DOOR)
        level.addItemToSpawn(IronKey(Dungeon.depth))

        val cA = when (wall.direction) {
            Direction.Left -> level.xy2cell(rect.x1, rect.center.y)
            Direction.Right -> level.xy2cell(rect.x2, rect.center.y)
            Direction.Up -> level.xy2cell(rect.center.x, rect.y1)
            Direction.Down -> level.xy2cell(rect.center.x, rect.y2)
        }

        var cE = PathFinder.NEIGHBOURS4[Random.Int(4)] + cA
        while (level.map[cE] == Terrain.WALL)
            cE = PathFinder.NEIGHBOURS4[Random.Int(4)] + cA

        // alchemy
        Set(level, cA, Terrain.ALCHEMY)
        val a = Alchemy()
        a.seed(level, cA, 1)
        level.blobs[Alchemy::class.java] = a

        // enchanting station
        Set(level, cE, Terrain.ENCHANTING_STATION)

        // two potions, perhaps some day, i should add some enchanting stone
        val n = Random.IntRange(2, 3)
        for (i in 1..n) {
            var pos: Int
            do {
                pos = level.pointToCell(rect.random())
            } while (level.map[pos] != Terrain.EMPTY_SP || level.heaps.get(pos) != null)

            level.drop(prize(level), pos)
        }

        return DigResult(rect, DigResult.Type.Locked)
    }

    private fun prize(level: Level) = level.findPrizeItem(Potion::class.java)
            ?: Generator.POTION.generate()
}
