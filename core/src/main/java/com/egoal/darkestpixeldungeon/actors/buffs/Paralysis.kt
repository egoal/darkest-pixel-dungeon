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
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance.Resistance
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Paralysis : FlavourBuff() {

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun attachTo(target: Char): Boolean {
        if (super.attachTo(target)) {
            target.paralysed = target.paralysed + 1
            return true
        } else {
            return false
        }
    }

    override fun detach() {
        super.detach()
        if (target.paralysed > 0)
            target.paralysed = target.paralysed - 1
    }

    override fun icon(): Int {
        return BuffIndicator.PARALYSIS
    }

    override fun fx(on: Boolean) {
        if (on)
            target.sprite.add(CharSprite.State.PARALYSED)
        else
            target.sprite.remove(CharSprite.State.PARALYSED)
    }

    override fun heroMessage(): String? {
        return Messages.get(this, "heromsg")
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns())
    }

    companion object {

        private val DURATION = 10f

        fun duration(ch: Char): Float {
            val r = ch.buff(Resistance::class.java)
            return if (r != null) r.durationFactor() * DURATION else DURATION
        }
    }
}
