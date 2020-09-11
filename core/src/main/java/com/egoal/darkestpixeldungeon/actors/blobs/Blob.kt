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

import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.watabou.utils.Bundle
import com.watabou.utils.Rect

open class Blob : Actor() {
    var volume = 0

    lateinit var cur: IntArray
    protected lateinit var off: IntArray

    var emitter: BlobEmitter? = null

    var area = Rect()

    init {
        actPriority = 1 //take priority over mobs, but not the hero
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        if (volume > 0) {
            var start: Int = 0
            while (start < Dungeon.level.length()) {
                if (cur!![start] > 0) {
                    break
                }
                start++
            }
            var end: Int
            end = Dungeon.level.length() - 1
            while (end > start) {
                if (cur!![end] > 0) {
                    break
                }
                end--
            }

            bundle.put(START, start)
            bundle.put(LENGTH, cur!!.size)
            bundle.put(CUR, trim(start, end + 1))

        }
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        if (bundle.contains(LENGTH)) {
            cur = IntArray(bundle.getInt(LENGTH))
        } else {
            //compatability with pre-0.4.2
            // cur = new int[Dungeon.level.length()]; // level has not initialized,
            // now, the map size is 36 x 36
            cur = IntArray(36 * 36)
        }
        off = IntArray(cur.size)

        val data = bundle.getIntArray(CUR)
        if (data != null) {
            val start = bundle.getInt(START)
            for (i in data.indices) {
                cur[i + start] = data[i]
                volume += data[i]
            }
        }
    }

    private fun trim(start: Int, end: Int): IntArray {
        val len = end - start
        val copy = IntArray(len)
        System.arraycopy(cur, start, copy, 0, len)
        return copy
    }

    public override fun act(): Boolean {
        spend(TICK)

        if (volume > 0) {

            if (area.isEmpty)
                setupArea()

            volume = 0

            evolve()
            val tmp = off
            off = cur
            cur = tmp

        } else {
            area.setEmpty()
        }

        return true
    }

    fun setupArea() {
        cur.indices.filter { cur[it] != 0 }.forEach { area.union(it % Dungeon.level.width(), it / Dungeon.level.width()) }

        for (cell in cur.indices) {
            if (cur[cell] != 0)
                area.union(cell % Dungeon.level.width(), cell / Dungeon.level.width())
        }
    }

    fun affectedChars() = sequence<Char> {
        for (i in area.left until area.right)
            for (j in area.top until area.bottom) {
                val cell = i + j * Dungeon.level.width()
                if (cur[cell] > 0) {
                    val ch = findChar(cell)
                    if (ch != null) yield(ch)
                }
            }
    }

    open fun use(emitter: BlobEmitter) {
        this.emitter = emitter
    }

    protected open fun evolve() {
        val blocking = Level.solid
        var cell: Int
        for (i in area.top - 1..area.bottom) {
            for (j in area.left - 1..area.right) {
                cell = j + i * Dungeon.level.width()
                if (Dungeon.level.insideMap(cell)) {
                    if (!blocking[cell]) {

                        var count = 1
                        var sum = cur[cell]

                        if (j > area.left && !blocking[cell - 1]) {
                            sum += cur[cell - 1]
                            count++
                        }
                        if (j < area.right && !blocking[cell + 1]) {
                            sum += cur[cell + 1]
                            count++
                        }
                        if (i > area.top && !blocking[cell - Dungeon.level.width()]) {
                            sum += cur[cell - Dungeon.level.width()]
                            count++
                        }
                        if (i < area.bottom && !blocking[cell + Dungeon.level.width()]) {
                            sum += cur[cell + Dungeon.level.width()]
                            count++
                        }

                        val value = if (sum >= count) sum / count - 1 else 0
                        off[cell] = value

                        if (value > 0) {
                            if (i < area.top)
                                area.top = i
                            else if (i >= area.bottom)
                                area.bottom = i + 1
                            if (j < area.left)
                                area.left = j
                            else if (j >= area.right)
                                area.right = j + 1
                        }

                        volume += value
                    } else {
                        off[cell] = 0
                    }
                }
            }
        }
    }

    open fun seed(level: Level, cell: Int, amount: Int) {
        if (!::cur.isInitialized) cur = IntArray(level.length())
        if (!::off.isInitialized) off = IntArray(cur.size)

        cur[cell] += amount
        volume += amount

        area.union(cell % level.width(), cell / level.width())
    }

    fun clear(cell: Int) {
        volume -= cur[cell]
        cur[cell] = 0
    }

    fun fullyClear() {
        volume = 0
        area.setEmpty()
        cur = IntArray(Dungeon.level.length())
        off = IntArray(Dungeon.level.length())
    }

    open fun tileDesc(): String? = null

    companion object {
        private const val CUR = "cur"
        private const val START = "start"
        private const val LENGTH = "length"

        fun <T : Blob> seed(cell: Int, amount: Int, type: Class<T>): T {
            var gas = Dungeon.level.blobs[type] as T?
            if (gas == null) {
                gas = type.newInstance()
                Dungeon.level.blobs[type] = gas
            }
            gas!!.seed(Dungeon.level, cell, amount)


            return gas
        }
    }
}
