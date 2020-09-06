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
package com.egoal.darkestpixeldungeon.actors

import android.util.SparseArray

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.Dungeon
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

import java.util.HashSet

abstract class Actor : Bundlable {
    private var time = 0f

    private var id = 0

    //used to determine what order actors act in.
    //hero should always act on 0, therefore negative is before hero, positive
    // is after hero
    protected var actPriority = Integer.MAX_VALUE

    protected abstract fun act(): Boolean

    protected open fun spend(time: Float) {
        this.time += time
    }

    protected fun postpone(time: Float) {
        if (this.time < now + time)
            this.time = now + time
    }

    fun cooldown(): Float {
        return time - now
    }

    protected fun diactivate() {
        time = Float.MAX_VALUE
    }

    protected open fun onAdd() {}

    protected open fun onRemove() {}

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(TIME, time)
        bundle.put(ID, id)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        time = bundle.getFloat(TIME)
        id = bundle.getInt(ID)
    }

    fun id(): Int {
        if (id <= 0) id = nextID++
        return id
    }

    /*protected*/
    open operator fun next() {
        if (current === this)
            current = null
    }

    companion object {
        const val TICK = 1f

        private const val TIME = "time"
        private const val ID = "id"

        private var nextID: Int = 1

        // **********************
        // *** Static members ***

        private val all = HashSet<Actor>()
        private val chars = HashSet<Char>()

        @Volatile
        private var current: Actor? = null

        @Volatile
        private var processing: Boolean = false

        private val ids = SparseArray<Actor>()

        private var now = 0f

        fun clear() {
            now = 0f

            all.clear()
            chars.clear()

            ids.clear()
        }

        fun fixTime() {
            if (Dungeon.hero != null && all.contains(Dungeon.hero)) Statistics.Duration += now

            var min = Float.MAX_VALUE
            for (a in all) {
                if (a.time < min) {
                    min = a.time
                }
            }
            for (a in all) {
                a.time -= min
            }
            now = 0f
        }

        fun init() {
            add(Dungeon.hero)

            for (mob in Dungeon.level.mobs) {
                add(mob)
            }

            for (blob in Dungeon.level.blobs.values) {
                add(blob)
            }

            current = null
        }

        private const val NEXTID = "nextid"

        fun storeNextID(bundle: Bundle) {
            bundle.put(NEXTID, nextID)
        }

        fun restoreNextID(bundle: Bundle) {
            nextID = bundle.getInt(NEXTID)
        }

        fun resetNextID() {
            nextID = 1
        }

        fun processing(): Boolean {
            return current != null
        }

        fun process() {
            if (current != null) {
                return
            }

            var doNext: Boolean

            do {
                now = java.lang.Float.MAX_VALUE
                current = null


                for (actor in all) {

                    //some actors will always go before others if time is equal.
                    if (actor.time < now || actor.time == now && (current == null || actor.actPriority < current!!.actPriority)) {
                        now = actor.time
                        current = actor
                    }

                }

                if (current != null) {

                    val acting = current

                    if (acting is Char) {
                        // If it's character's turn to act, but its sprite
                        // is moving, wait till the movement is over
                        try {
                            synchronized(acting.sprite) {
                                if (acting.sprite.isMoving) {
                                    (acting.sprite as java.lang.Object).wait()
                                }
                            }
                        } catch (e: InterruptedException) {
                            DarkestPixelDungeon.reportException(e)
                        }

                    }

                    doNext = acting!!.act()
                    if (doNext && !Dungeon.hero.isAlive) {
                        doNext = false
                        current = null
                    }
                } else {
                    doNext = false
                }

            } while (doNext)
        }

        fun add(actor: Actor) {
            add(actor, now)
        }

        fun addDelayed(actor: Actor, delay: Float) {
            add(actor, now + delay)
        }

        private fun add(actor: Actor, time: Float) {
            if (all.contains(actor)) {
                return
            }

            ids.put(actor.id(), actor)

            all.add(actor)
            actor.time += time
            actor.onAdd()

            if (actor is Char) {
                chars.add(actor)
                for (buff in actor.buffs()) {
                    // may add twice when loading: the level is not initialized.
                    // if (all.contains(buff)) continue;

                    all.add(buff)
                    buff.onAdd()
                }
            }
        }

        fun remove(actor: Actor?) {

            if (actor != null) {
                all.remove(actor)
                chars.remove(actor)
                actor.onRemove()

                if (actor.id > 0) {
                    ids.remove(actor.id)
                }
            }
        }

        fun findChar(pos: Int): Char? = chars.find { it.pos == pos }

        fun findById(id: Int): Actor = ids.get(id)

        fun all(): HashSet<Actor> = all

        fun chars(): HashSet<Char> = chars
    }
}
