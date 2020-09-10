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
package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Viscosity : Armor.Glyph() {

    override fun proc(armor: Armor, damage: Damage): Damage {
        val attacker = damage.from as Char
        val defender = damage.to as Char

        if (damage.type == Damage.Type.MENTAL || damage.value <= 0) {
            damage.value = 0
            return damage
        }

        val level = Math.max(0, armor.level())

        if (Random.Int(level + 4) >= 3) {

            var debuff = defender.buff(DeferedDamage::class.java)
            if (debuff == null) {
                debuff = DeferedDamage()
                debuff.attachTo(defender)
            }
            debuff.prolong(damage.value)

            defender.sprite.showStatus(CharSprite.WARNING, Messages.get(this,
                    "deferred", damage.value))

            damage.value = 0
            return damage

        } else {
            return damage
        }
    }

    override fun glowing(): ItemSprite.Glowing {
        return PURPLE
    }

    class DeferedDamage : Buff() {

        protected var damage = 0

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(DAMAGE, damage)

        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            damage = bundle.getInt(DAMAGE)
        }

        override fun attachTo(target: Char): Boolean {
            if (super.attachTo(target)) {
                postpone(Actor.TICK)
                return true
            } else {
                return false
            }
        }

        fun prolong(damage: Int) {
            this.damage += damage
        }

        override fun icon(): Int {
            return BuffIndicator.DEFERRED
        }

        override fun toString(): String {
            return Messages.get(this, "name")
        }

        override fun act(): Boolean {
            if (target.isAlive) {

                val damageThisTick = Math.max(1, damage / 10)
                target.takeDamage(Damage(damageThisTick, this, target))
                if (target === Dungeon.hero && !target.isAlive) {

                    Dungeon.fail(javaClass)
                    GLog.n(Messages.get(this, "ondeath"))

                    Badges.validateDeathFromGlyph()
                }
                spend(Actor.TICK)

                damage -= damageThisTick
                if (damage <= 0) {
                    detach()
                }

            } else {

                detach()

            }

            return true
        }

        override fun desc(): String {
            return Messages.get(this, "desc", damage)
        }

        companion object {

            private val DAMAGE = "damage"
        }
    }

    companion object {

        private val PURPLE = ItemSprite.Glowing(0x8844CC)
    }
}
