package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.Piranha
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.PotionOfInvisibility
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.*
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.Point
import com.watabou.utils.Random

/**
 * Created by 93942 on 2018/12/8.
 */
const val NUM_PIRANHAS = 3

class PoolDigger : RectDigger() {

    override fun chooseRoomSize(wall: Wall) = if (Random.Int(3) == 0) 
        Point(Random.IntRange(7, 9), Random.IntRange(7, 9)) else super.chooseRoomSize(wall)


    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        if (rect.width >= 7 && rect.height >= 7)
            return digBig(level, wall, rect)

        Fill(level, rect, Terrain.WATER)

        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.DOOR)

        val plat = when (wall.direction) {
            Direction.Left -> level.xy2cell(rect.x1, rect.center.y)
            Direction.Right -> level.xy2cell(rect.x2, rect.center.y)
            Direction.Up -> level.xy2cell(rect.center.x, rect.y1)
            Direction.Down -> level.xy2cell(rect.center.x, rect.y2)
        }
        Set(level, plat, Terrain.PEDESTAL)

        // items
        level.drop(prize(level), plat).type = if (Random.Int(3) == 0) Heap.Type.CHEST else Heap.Type.HEAP
        level.addItemToSpawn(PotionOfInvisibility())

        // piranhas
        for (i in 0 until NUM_PIRANHAS) {
            val p = Piranha()
            do {
                p.pos = level.pointToCell(rect.random())
            } while (level.map[p.pos] != Terrain.WATER || level.findMob(p.pos) != null)
            level.mobs.add(p)
        }

        return DigResult(rect, DigResult.Type.Special)
    }

    private fun digBig(level: Level, wall: Wall, rect: Rect): DigResult {
        // big room, can be expanded
        Fill(level, rect, Terrain.EMPTY)
        Fill(level, rect.shrink(1), Terrain.EMPTY_SP)
        Fill(level, rect.shrink(2), Terrain.WATER)

        // door
        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.DOOR)

        val cen = level.pointToCell(rect.center)
        Set(level, cen, Terrain.PEDESTAL)
        level.drop(prize(level), cen).type = if (Random.Int(3) == 0) Heap.Type.CHEST else Heap.Type.HEAP

        level.addItemToSpawn(PotionOfInvisibility())

        // piranhas
        for (i in 1..NUM_PIRANHAS) {
            val p = Piranha()
            do {
                p.pos = level.pointToCell(rect.random(2))
            } while (level.map[p.pos] != Terrain.WATER || level.findMob(p.pos) != null)
            level.mobs.add(p)
        }

        return DigResult(rect, Wall.ArroundBut(rect, wall.direction.opposite), DigResult.Type.Special)
    }

    private fun prize(level: Level): Item {
        if (Random.Int(3) == 0) {
            val prize = level.findPrizeItem()
            if (prize != null)
                return prize
        }

        //1 floor set higher in probability, never cursed
        var prize = if (Random.Int(2) == 0) Generator.randomWeapon(Dungeon.depth / 5 + 1)
        else Generator.randomArmor(Dungeon.depth / 5 + 1)
        while (prize.cursed)
            prize = if (Random.Int(2) == 0) Generator.randomWeapon(Dungeon.depth / 5 + 1)
            else Generator.randomArmor(Dungeon.depth / 5 + 1)

        //33% chance for an extra update.
        if (prize !is MissileWeapon && Random.Int(3) == 0)
            prize.upgrade()

        return prize
    }
    
}
