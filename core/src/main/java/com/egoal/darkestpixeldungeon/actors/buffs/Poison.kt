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

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.effects.particles.PoisonParticle
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import kotlin.math.max

open class Poison : Buff(), Hero.Doom {
    protected var left: Float = 0f
    private var extraDamage = 1

    init {
        type = buffType.NEGATIVE
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEFT, left)
        bundle.put(EXTRA_DAMAGE, extraDamage)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        left = bundle.getFloat(LEFT)
        extraDamage = bundle.getInt(EXTRA_DAMAGE)
    }

    fun set(duration: Float) {
        left = max(duration, left)
    }

    fun addExtraDamage(damage: Int) {
        extraDamage += damage
    }

    fun extend(duration: Float) {
        left += duration
    }

    override fun icon(): Int = BuffIndicator.POISON

    override fun toString(): String = M.L(this, "name")

    override fun heroMessage(): String? = M.L(this, "heromsg")

    override fun desc(): String = M.L(this, "desc", dispTurns(left))

    override fun attachTo(target: Char): Boolean {
        if (super.attachTo(target) && target.hasSprite) {
            CellEmitter.center(target.pos).burst(PoisonParticle.SPLASH, 5)
            return true
        } else
            return false
    }

    override fun act(): Boolean {
        if (target.isAlive) {
            val dmg = Damage(this, target, Damage.Type.MAGICAL)
                    .setAdditionalDamage(Damage.Element.Poison, (left / 3 + extraDamage).toInt() + 1)
            target.takeDamage(dmg)
            spend(TICK)

            left -= TICK
            if (left <= 0)
                detach()
        } else
            detach()

        return true
    }

    override fun onDeath() {
        Badges.validateDeathFromPoison()

        Dungeon.fail(javaClass)
        GLog.n(M.L(this, "ondeath"))
    }

    companion object {
        private const val LEFT = "left"
        private const val EXTRA_DAMAGE = "extra-damage"

        fun durationFactor(ch: Char): Float {
            val r = ch.buff(RingOfResistance.Resistance::class.java)
            return r?.durationFactor() ?: 1f
        }
    }
}
