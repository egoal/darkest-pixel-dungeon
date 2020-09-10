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
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Levitation : FlavourBuff() {

    override fun attachTo(target: Char): Boolean {
        if (super.attachTo(target)) {
            target.flying = true
            Buff.Companion.detach(target, Roots::class.java)
            return true
        } else {
            return false
        }
    }

    override fun detach() {
        target.flying = false
        Dungeon.level.press(target.pos, target)
        super.detach()
    }

    override fun icon(): Int {
        return BuffIndicator.LEVITATION
    }

    override fun fx(on: Boolean) {
        if (on)
            target.sprite.add(CharSprite.State.LEVITATING)
        else
            target.sprite.remove(CharSprite.State.LEVITATING)
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns())
    }

    companion object {

        val DURATION = 20f
    }
}
