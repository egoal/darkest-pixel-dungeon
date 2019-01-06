package com.egoal.darkestpixeldungeon.levels.diggers.normal

import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Gold
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.watabou.utils.Random

class GraveyardDigger : Digger() {
    private val shapeDigger = when (Random.Int(8)) {
        0 -> RoundDigger()
        1 -> DiamondDigger()
        else -> RectDigger()
    }

    override fun chooseDigArea(wall: Wall) = shapeDigger.chooseDigArea(wall)

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = shapeDigger.dig(level, wall, rect)

        // grass 
        rect.getAllPoints().map { level.pointToCell(it) }.filter { level.map[it] == Terrain.EMPTY }.forEach { level.map[it] = Terrain.GRASS }

        // put tombs
        val tryPlaceATomb = { item: Item ->
            for (i in 1..30) {
                val cell = level.pointToCell(rect.random(1))
                if (level.map[cell] == Terrain.GRASS && level.heaps.get(cell) == null) {
                    level.drop(item, cell).type = Heap.Type.TOMB
                    break
                }
            }
        }

        val n = Random.IntRange(2, 4)
        val index = Random.Int(n) // random item
        for (i in 0 until n)
            tryPlaceATomb(if (i == index) Generator.random() else Gold().random())

        return dr
    }
}