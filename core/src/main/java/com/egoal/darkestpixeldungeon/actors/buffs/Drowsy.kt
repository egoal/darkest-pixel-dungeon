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
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Random

class Drowsy : Buff() {

    init {
        type = buffType.NEUTRAL
    }

    override fun icon(): Int = BuffIndicator.DROWSY

    override fun attachTo(target: Char): Boolean {
        if (!target.immunizedBuffs().contains(Sleep::class.java) && super.attachTo(target)) {
            if (cooldown() == 0f)
                spend(Random.Int(3, 6).toFloat())
            return true
        }
        return false
    }

    override fun act(): Boolean {
        affect(target, MagicalSleep::class.java)

        detach()
        return true
    }

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns(cooldown() + 1))
}
