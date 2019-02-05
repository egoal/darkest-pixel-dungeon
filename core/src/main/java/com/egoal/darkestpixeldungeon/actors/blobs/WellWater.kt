/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

open class WellWater : Blob() {

    protected var pos: Int = 0

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        for (i in cur.indices) {
            if (cur[i] > 0) {
                pos = i
                break
            }
        }
    }

    override fun evolve() {
        off[pos] = cur[pos]
        volume = off[pos]
        area.union(pos % Dungeon.level.width(), pos / Dungeon.level.width())

        if (Dungeon.visible[pos]) {
            if (this is WaterOfAwareness) {
                Journal.add(Journal.Feature.WELL_OF_AWARENESS)
            } else if (this is WaterOfHealth) {
                Journal.add(Journal.Feature.WELL_OF_HEALTH)
            } else if (this is WaterOfTransmutation) {
                Journal.add(Journal.Feature.WELL_OF_TRANSMUTATION)
            }
        }
    }

    protected fun affect(): Boolean {
        if (pos == Dungeon.hero.pos && affectHero(Dungeon.hero)) {

            cur[pos] = 0
            off[pos] = cur[pos]
            volume = off[pos]

            return true
        } else {
            Dungeon.level.heaps.get(pos)?.let { heap ->
                val oldItem = heap.peek()
                val newItem = affectItem(oldItem)

                if (newItem != null) {
                    if (newItem === oldItem) {
                    } else if (oldItem.quantity() > 1) {
                        oldItem.quantity(oldItem.quantity() - 1)
                        heap.drop(newItem)
                    } else heap.replace(oldItem, newItem)

                    heap.sprite.link()

                    cur[pos] = 0
                    off[pos] = cur[pos]
                    volume = off[pos]

                    return true

                } else {
                    // throw 
                    var place: Int
                    do {
                        place = pos + PathFinder.NEIGHBOURS8[Random.Int(8)]
                    } while (!Level.passable[place] && !Level.avoid[place])
                    Dungeon.level.drop(heap.pickUp(), place).sprite.drop()
                    
                    return false
                }
            }
        }

        return false
    }

    protected open fun affectHero(hero: Hero): Boolean = false

    protected open fun affectItem(item: Item): Item? = null

    override fun seed(level: Level, cell: Int, amount: Int) {
        super.seed(level, cell, amount)

        cur[pos] = 0
        pos = cell
        cur[pos] = amount
        volume = cur[pos]

        area.setEmpty()
        area.union(cell % level.width(), cell / level.width())
    }

    companion object {

        fun AffectCell(cell: Int) {

            val waters = arrayOf<Class<*>>(WaterOfHealth::class.java, WaterOfAwareness::class.java, WaterOfTransmutation::class.java)

            for (waterClass in waters) {
                val water = Dungeon.level.blobs[waterClass] as WellWater?
                if (water != null &&
                        water.volume > 0 &&
                        water.pos == cell &&
                        water.affect()) {

                    Level.set(cell, Terrain.EMPTY_WELL)
                    GameScene.updateMap(cell)

                    return
                }
            }
        }
    }
}
