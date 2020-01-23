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
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.rings.RingOfElements.Resistance
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Weakness : FlavourBuff() {

    init {
        type = buffType.NEGATIVE
    }

    override fun icon(): Int = BuffIndicator.WEAKNESS

    override fun toString(): String = M.L(this, "name")

    override fun attachTo(target: Char): Boolean {
        val attached = super.attachTo(target)

        if (attached && target is Hero) {
            target.weakened = true
        }

        return attached
    }

    override fun detach() {
        super.detach()
        if (target is Hero)
            (target as Hero).weakened = false
    }

    override fun heroMessage(): String? = M.L(this, "heromsg")

    override fun desc(): String = M.L(this, "desc", dispTurns())

    companion object {
        private const val DURATION = 20f

        fun duration(ch: Char): Float {
            val r = ch.buff(Resistance::class.java)
            return if (r != null) r.durationFactor() * DURATION else DURATION
        }
    }
}
