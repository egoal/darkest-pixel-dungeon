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

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import kotlin.math.min

class Recharging : FlavourBuff() {

    override fun icon(): Int = BuffIndicator.RECHARGING

    override fun toString(): String = M.L(this, "name")

    //want to process partial turns for this buff, and not count it when it's
    // expiring.
    //firstly, if this buff has half a turn left, should give out half the
    // benefit.
    //secondly, recall that buffs execute in random order, so this can cause a
    // problem where we can't simply check
    //if this buff is still attached, must instead directly check its remaining
    // time, and act accordingly.
    //otherwise this causes inconsistent behaviour where this may detach
    // before, or after, a wand charger acts.
    fun remainder(): Float = min(1f, this.cooldown())

    override fun desc(): String = M.L(this, "desc", dispTurns())
}
