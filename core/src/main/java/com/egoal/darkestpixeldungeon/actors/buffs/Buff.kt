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
package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

import java.util.HashSet

open class Buff : Actor() {
    lateinit var target: Char

    var type = buffType.SILENT

    // var resistances = HashSet<Class<*>>()
    var immunities = HashSet<Class<*>>()

    init {
        actPriority = 3 //low priority, at the end of a turn
    }

    //determines how the buff is announced when it is shown.
    // buffs that work behind the scenes, or have other visual indicators can usually be silent.
    enum class buffType {
        POSITIVE, NEGATIVE, NEUTRAL, SILENT
    }

    open fun attachTo(target: Char): Boolean {
        if (target.immunizedBuffs().contains(javaClass))
            return false

        this.target = target
        target.add(this)

        return if (target.buffs().contains(this)) {
            if (target.hasSprite) fx(true)
            true
        } else
            false
    }

    open fun detach() {
        fx(false)
        target.remove(this)
    }

    public override fun act(): Boolean {
        diactivate()
        return true
    }

    open fun icon(): Int = BuffIndicator.NONE

    open fun fx(on: Boolean) {
        //do nothing by default
    }

    open fun heroMessage(): String? = null

    open fun desc(): String = ""

    // to handle the common case of showing how many turns are remaining in a buff description.
    protected fun dispTurns(input: Float): String = String.format("%.2f", input)

    companion object {
        // creates a fresh instance of the buff and attaches that, this allows duplication.
        fun <T : Buff> append(target: Char, buffClass: Class<T>): T {
            val buff = buffClass.newInstance()
            buff.attachTo(target)
            return buff
        }

        fun <T : FlavourBuff> append(target: Char, buffClass: Class<T>, duration: Float): T {
            val buff = append(target, buffClass)
            buff.spend(duration)
            return buff
        }

        // same as append, but prevents duplication.
        fun <T : Buff> affect(target: Char, buffClass: Class<T>): T = target.buff(buffClass)
                ?: append(target, buffClass)

        fun <T : FlavourBuff> affect(target: Char, buffClass: Class<T>, duration: Float): T = affect(target, buffClass).apply { spend(duration) }

        // postpones an already active buff, or creates & attaches a new buff and delays that.
        fun <T : FlavourBuff> prolong(target: Char, buffClass: Class<T>, duration: Float): T = affect(target, buffClass).apply { postpone(duration) }

        fun detach(buff: Buff?) {
            buff?.detach()
        }

        fun detach(target: Char, cl: Class<out Buff>) {
            detach(target.buff(cl))
        }
    }
}
