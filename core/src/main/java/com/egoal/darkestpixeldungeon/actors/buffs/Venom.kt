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
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

class Venom : Poison(), Hero.Doom {
    private var damage = 1

    init {
        type = buffType.NEGATIVE
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DAMAGE, damage)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        damage = bundle.getInt(DAMAGE)
    }

    operator fun set(duration: Float, damage: Int) {
        set(duration)
        if (this.damage < damage) this.damage = damage
    }

    override fun icon(): Int = BuffIndicator.POISON

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns(left), damage)

    override fun act(): Boolean {
        if (target.isAlive) {
            // targetpos.damage(damage, this);
            target.takeDamage(Damage(damage, this, target).addElement(Damage
                    .Element.POISON))
            if (damage < (Dungeon.depth + 1) / 2 + 1)
                damage++

            //want it to act after the cloud of venom it came from.
            spend(Actor.TICK + 0.1f)
            left -= TICK
            if (left <= 0) {
                detach()
            }
        } else {
            detach()
        }

        return true
    }

    companion object {
        private const val DAMAGE = "damage"
    }

}
