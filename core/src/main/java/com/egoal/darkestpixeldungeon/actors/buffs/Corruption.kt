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
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

open class Corruption : Buff() {

    private var buildToDamage = 0f

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun attachTo(target: Char): Boolean {
        target.camp = Char.Camp.HERO
        return super.attachTo(target)
    }

    override fun act(): Boolean {
        buildToDamage += target.HT / 200f

        val damage = buildToDamage.toInt()
        buildToDamage -= damage.toFloat()

        if (damage > 0)
            target.takeDamage(Damage(0, this, target).setAdditionalDamage(Damage.Element.SHADOW, damage))

        spend(TICK)

        return true
    }

    override fun fx(on: Boolean) {
        if (on)
            target.sprite.add(CharSprite.State.DARKENED)
        else if (target.invisible == 0)
            target.sprite.remove(CharSprite.State.DARKENED)
    }

    override fun icon(): Int {
        return BuffIndicator.CORRUPT
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc")
    }
}
