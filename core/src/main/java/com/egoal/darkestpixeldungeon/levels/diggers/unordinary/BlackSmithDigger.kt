package com.egoal.darkestpixeldungeon.levels.diggers.unordinary

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Blacksmith
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.egoal.darkestpixeldungeon.levels.traps.FireTrap
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 12/4/2018.
 */

class BlackSmithDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(4, 7), Random.IntRange(4, 7))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.TRAP)
        Fill(level, rect.shrink(1), Terrain.EMPTY_SP)

        val din = overlappedWall(wall, rect).random(0)
        Set(level, din, Terrain.DOOR)

        // 2 weapons
        for (i in 0..1) {
            var pos: Int
            do {
                pos = level.pointToCell(rect.random(1))
            } while (level.map[pos] != Terrain.EMPTY_SP)
            level.drop(Generator.random(Random.oneOf(
                    Generator.Category.ARMOR, Generator.Category.WEAPON)), pos)
        }

        // smith
        val npc = Blacksmith()
        do {
            npc.pos = level.pointToCell(rect.random(1))
        } while (level.heaps.get(npc.pos) != null)
        level.mobs.add(npc)

        // traps
        for (p in rect.getAllPoints()) {
            val cell = level.pointToCell(p)
            if (level.map[cell] == Terrain.TRAP)
                level.setTrap(FireTrap().reveal(), cell)
        }

        return DigResult(rect, Wall.ArroundBut(rect, wall.direction.opposite), DigResult.Type.Special)
    }
}
