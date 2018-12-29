package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Challenges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.Foliage
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.egoal.darkestpixeldungeon.plants.BlandfruitBush
import com.egoal.darkestpixeldungeon.plants.Sungrass
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 12/4/2018.
 */

class GardenDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall): Point {
        val size = Random.IntRange(3, 6)
        return Point(size, size)
    }

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val circles = (rect.width - 1) / 2
        for (i in 0..circles)
            Fill(level, rect.shrink(i), if (i % 2 == 0) Terrain.HIGH_GRASS else Terrain.GRASS)

        val door = overlappedWall(wall, rect).random(0)
        Set(level, door, Terrain.DOOR)

        if (Dungeon.isChallenged(Challenges.NO_FOOD)) {
            if (Random.Int(2) == 0)
                level.plant(Sungrass.Seed(), level.pointToCell(rect.random(0)))
        } else {
            when (Random.Int(3)) {
                0 -> level.plant(Sungrass.Seed(), level.pointToCell(rect.random(0)))
                1 -> level.plant(BlandfruitBush.Seed(), level.pointToCell(rect.random(0)))
                else -> if (Random.Int(5) == 0) {
                    // both 
                    val p1 = level.pointToCell(rect.random(0))
                    var p2 = level.pointToCell(rect.random(0))
                    while (p1 == p2)
                        p2 = level.pointToCell(rect.random(0))

                    level.plant(Sungrass.Seed(), p1)
                    level.plant(BlandfruitBush.Seed(), p2)
                }
            }
        }

        var light: Foliage? = level.blobs[Foliage::class.java] as Foliage
        if (light == null)
            light = Foliage()

        for (p in rect.getAllPoints())
            light.seed(level, level.pointToCell(p), 1)
        level.blobs[Foliage::class.java] = light

        // now, the garden is open!
        return DigResult(rect, Wall.ArroundBut(rect, wall.direction.opposite), DigResult.Type.Special)
    }
}
