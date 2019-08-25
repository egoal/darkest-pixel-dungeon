package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Dungeon

import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLevitation
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.egoal.darkestpixeldungeon.levels.traps.BlazingTrap
import com.egoal.darkestpixeldungeon.levels.traps.ConfusionTrap
import com.egoal.darkestpixeldungeon.levels.traps.DisintegrationTrap
import com.egoal.darkestpixeldungeon.levels.traps.ExplosiveTrap
import com.egoal.darkestpixeldungeon.levels.traps.FlockTrap
import com.egoal.darkestpixeldungeon.levels.traps.GrimTrap
import com.egoal.darkestpixeldungeon.levels.traps.ParalyticTrap
import com.egoal.darkestpixeldungeon.levels.traps.SpearTrap
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap
import com.egoal.darkestpixeldungeon.levels.traps.TeleportationTrap
import com.egoal.darkestpixeldungeon.levels.traps.ToxicTrap
import com.egoal.darkestpixeldungeon.levels.traps.Trap
import com.egoal.darkestpixeldungeon.levels.traps.VenomTrap
import com.egoal.darkestpixeldungeon.levels.traps.WarpingTrap
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/8.
 */

class TrapsDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = if (Random.Int(3) == 0)
        Point(Random.IntRange(7, 9), Random.IntRange(7, 9)) else super.chooseRoomSize(wall)

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        if (rect.width >= 7 && rect.height >= 7)
            return digBig(level, wall, rect)

        val trapClass = chooseTrap()

        Fill(level, rect, if (trapClass == null) Terrain.CHASM else Terrain.TRAP)
        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.DOOR)

        val lastRowTile = if (trapClass == null) Terrain.CHASM else Terrain.EMPTY
        val plat = rect.center
        when (wall.direction) {
            Direction.Left -> {
                LinkVertical(level, rect.x1, rect.y1, rect.y2, lastRowTile)
                plat.x = rect.x1
            }
            Direction.Right -> {
                LinkVertical(level, rect.x2, rect.y1, rect.y2, lastRowTile)
                plat.x = rect.x2
            }
            Direction.Up -> {
                LinkHorizontal(level, rect.y1, rect.x1, rect.x2, lastRowTile)
                plat.y = rect.y1
            }
            Direction.Down -> {
                LinkHorizontal(level, rect.y2, rect.x1, rect.x2, lastRowTile)
                plat.y = rect.y2
            }
        }

        putTraps(level, rect, trapClass)

        val pos = level.pointToCell(plat)
        if (Random.Int(3) == 0) {
            if (lastRowTile == Terrain.CHASM)
                Set(level, pos, Terrain.EMPTY)
            level.drop(prize(level), pos).type = Heap.Type.CHEST
        } else {
            Set(level, pos, Terrain.PEDESTAL)
            level.drop(prize(level), pos)
        }

        level.addItemToSpawn(PotionOfLevitation())

        return DigResult(rect, DigResult.Type.Locked)
    }

    private fun digBig(level: Level, wall: Wall, rect: Rect): DigResult {
        val trapClass = chooseTrap()

        Fill(level, rect, Terrain.EMPTY)
        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.DOOR)

        Fill(level, rect.shrink(1), if (trapClass == null) Terrain.CHASM else Terrain.TRAP)
        val plat = level.pointToCell(rect.center)
        Set(level, plat, Terrain.EMPTY)

        putTraps(level, rect.shrink(1), trapClass)
        if (Random.Int(3) == 0) {
            level.drop(prize(level), plat)
        } else {
            Set(level, plat, Terrain.PEDESTAL)
            level.drop(prize(level), plat)
        }

        level.addItemToSpawn(PotionOfLevitation())

        return DigResult(rect, Wall.ArroundBut(rect, wall.direction.opposite), DigResult.Type.Special)
    }

    private fun chooseTrap(): Class<out Trap>? = when (Random.Int(5)) {
        0 -> SpearTrap::class.java
        1 -> if (!Dungeon.bossLevel(Dungeon.depth + 1)) null else SummoningTrap::class.java
        2, 3, 4 -> Random.oneOf(*LevelTraps[Dungeon.depth / 5])
        else -> SpearTrap::class.java
    }

    private fun putTraps(level: Level, rect: Rect, trapClass: Class<out Trap>?) {
        for (p in rect.getAllPoints()) {
            val c = level.pointToCell(p)
            if (level.map[c] == Terrain.TRAP) {
                try {
                    level.setTrap((trapClass!!.newInstance() as Trap).reveal(), c)
                } catch (e: Exception) {
                    DarkestPixelDungeon.reportException(e)
                }

            }
        }
    }


    private fun prize(level: Level): Item {
        if (Random.Int(4) != 0) {
            val prize = level.findPrizeItem()
            if (prize != null)
                return prize
        }

        var prize = Random.oneOf(Generator.WEAPON, Generator.ARMOR).generate()

        // 3 more chances.
        for (i in 0..2) {
            val another = Random.oneOf(Generator.WEAPON, Generator.ARMOR).generate()
            if (another.level() > prize.level()) {
                prize = another
            }
        }
        prize.cursedKnown = true 
        
        return prize
    }

    companion object {
        private val LevelTraps = arrayOf(
                //sewers
                arrayOf(ToxicTrap::class.java, TeleportationTrap::class.java, FlockTrap::class.java),
                //prison
                arrayOf(ConfusionTrap::class.java, ExplosiveTrap::class.java, ParalyticTrap::class.java),
                //caves
                arrayOf(BlazingTrap::class.java, VenomTrap::class.java, ExplosiveTrap::class.java),
                //city
                arrayOf(WarpingTrap::class.java, VenomTrap::class.java, DisintegrationTrap::class.java),
                //halls, muahahahaha
                arrayOf(GrimTrap::class.java))
    }
}
