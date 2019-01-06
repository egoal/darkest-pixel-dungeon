package com.egoal.darkestpixeldungeon.levels.diggers.secret

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Gold
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.egoal.darkestpixeldungeon.levels.traps.DisintegrationTrap
import com.egoal.darkestpixeldungeon.levels.traps.PoisonTrap
import com.egoal.darkestpixeldungeon.levels.traps.RockfallTrap
import com.egoal.darkestpixeldungeon.levels.traps.Trap
import com.watabou.utils.Point
import com.watabou.utils.Random

class SecretTreasuryDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(3, 6), Random.IntRange(3, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY)
        Set(level, rect.center, Terrain.STATUE)
        Set(level, overlappedWall(wall, rect).random(), Terrain.SECRET_DOOR)

        val cls: Class<out Trap> = when {
            Random.Int(2) == 0 -> RockfallTrap::class.java
            Dungeon.depth >= 0 -> DisintegrationTrap::class.java
            else -> PoisonTrap::class.java
        }

        val golds = rect.area / 2
        val ratio = 8f / golds.toFloat()

        for (i in 1..golds) {
            var pos = level.pointToCell(rect.random())
            while (level.heaps.get(pos) != null || level.map[pos] != Terrain.EMPTY)
                pos = level.pointToCell(rect.random())

            val gold = Gold().random()
            gold.quantity(Math.round(gold.quantity() * ratio))
            level.drop(gold, pos)
        }

        // put traps
        for (cell in rect.getAllPoints().map { level.pointToCell(it) })
            if (Random.Int(2) == 0 && level.map[cell] == Terrain.EMPTY) {
                level.setTrap(cls.newInstance().reveal(), cell)
                Digger.Set(level, cell, Terrain.TRAP)
            }

        return DigResult(rect, DigResult.Type.Secret)
    }
}
