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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.items.artifacts.CloakOfShadows
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

open class Invisibility : FlavourBuff() {

    init {
        type = Buff.buffType.POSITIVE
    }

    override fun attachTo(target: Char): Boolean {
        if (super.attachTo(target)) {
            target.invisible = target.invisible + 1
            return true
        } else {
            return false
        }
    }

    override fun detach() {
        if (target.invisible > 0)
            target.invisible = target.invisible - 1
        super.detach()
    }

    override fun icon(): Int {
        return BuffIndicator.INVISIBLE
    }

    override fun fx(on: Boolean) {
        if (on)
            target.sprite.add(CharSprite.State.INVISIBLE)
        else if (target.invisible == 0)
            target.sprite.remove(CharSprite.State.INVISIBLE)
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns())
    }

    companion object {

        val DURATION = 20f

        fun dispel() {
            val buff = Dungeon.hero.buff(Invisibility::class.java)
            buff?.detach()
            val cloakBuff = Dungeon.hero.buff(CloakOfShadows.cloakStealth::class.java)
            cloakBuff?.dispel()

            //this isn't a form of invisibilty, but it is meant to dispel at the same
            // time as it.
            //    TimekeepersHourglass.TimeFreeze timeFreeze = Dungeon.hero.buff
            //            (TimekeepersHourglass.TimeFreeze.class);
            //    if (timeFreeze != null)
            //      timeFreeze.detach();

        }
    }
}
