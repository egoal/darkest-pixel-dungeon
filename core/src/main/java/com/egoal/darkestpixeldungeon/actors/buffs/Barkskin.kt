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

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Barkskin : Buff() {
    private var level = 0

    override fun act(): Boolean {
        if (target.isAlive) {
            spend(TICK)
            if (--level <= 0)
                detach()
        } else
            detach()

        return true
    }

    fun level(): Int = level

    fun level(value: Int) {
        if (level < value)
            level = value
    }

    override fun icon(): Int = BuffIndicator.BARKSKIN

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", level)
}
