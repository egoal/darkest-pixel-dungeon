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
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

class Charm : FlavourBuff() {
    var objectid = 0

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(OBJECT, objectid)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        objectid = bundle.getInt(OBJECT)
    }

    override fun icon(): Int = BuffIndicator.HEART

    override fun toString(): String = M.L(this, "name")

    override fun heroMessage(): String? = M.L(this, "heromsg")

    override fun desc(): String = M.L(this, "desc", dispTurns())

    // charm attach should be delayed to avoid detach in Char::takeDamage
    class Attacher(charmer_id: Int, charmDuration: Int) : FlavourBuff() {

        internal var charmer = -1
        internal var charm_duration = 0

        init {
            actPriority = Integer.MIN_VALUE
            type = Buff.buffType.NEGATIVE
        }

        init {
            charmer = charmer_id
            charm_duration = charmDuration
        }

        override fun act(): Boolean {
            Buff.affect(target, Charm::class.java, Charm.durationFactor(target) * charm_duration).objectid = charmer
            return super.act()
        }

        override fun toString(): String = ""

    }

    companion object {
        private const val OBJECT = "objectid"

        fun durationFactor(ch: Char): Float = ch.buff(RingOfResistance.Resistance::class.java)?.durationFactor()
                ?: 1f
    }
}
