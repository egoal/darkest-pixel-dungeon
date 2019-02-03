package com.egoal.darkestpixeldungeon.levels.diggers.unordinary

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.RotHeart
import com.egoal.darkestpixeldungeon.actors.mobs.RotLasher
import com.egoal.darkestpixeldungeon.items.keys.IronKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.PathFinder
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/18.
 */

class RotGardenDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(5, 9), Random.IntRange(5, 9))


    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.GRASS)

        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.LOCKED_DOOR)
        level.addItemToSpawn(IronKey(Dungeon.depth))

        val hpc = run {
            val hp = rect.random()
            when (wall.direction) {
                Direction.Left -> hp.x = rect.x1
                Direction.Right -> hp.x = rect.x2
                Direction.Up -> hp.y = rect.y1
                Direction.Down -> hp.y = rect.y2
            }
            level.pointToCell(hp)
        }

        placePlant(level, hpc, RotHeart())

        val lashers = rect.area / 8
        for (i in 0 until lashers) {
            var pos: Int
            do {
                pos = level.pointToCell(rect.random())
            } while (!isValidPlantPos(level, pos))
            placePlant(level, pos, RotLasher().apply { setLevel(Dungeon.depth) })
        }

        return DigResult(rect, DigResult.Type.Locked)
    }

    private fun isValidPlantPos(level: Level, pos: Int): Boolean {
        return level.map[pos] == Terrain.GRASS &&
                PathFinder.NEIGHBOURS9.all { level.findMob(pos + it) == null }
    }

    private fun placePlant(level: Level, pos: Int, plant: Mob) {
        plant.pos = pos
        level.mobs.add(plant)

        PathFinder.NEIGHBOURS8.forEach {
            if (level.map[pos + it] == Terrain.GRASS)
                Set(level, pos + it, Terrain.HIGH_GRASS)
        }
    }
}
