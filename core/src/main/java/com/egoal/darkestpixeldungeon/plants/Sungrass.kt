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
package com.egoal.darkestpixeldungeon.plants

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

class Sungrass : Plant(4) {

    override fun activate() {
        val ch = Actor.findChar(pos)

        if (ch === Dungeon.hero) {
            Buff.affect(ch!!, Health::class.java).boost(ch.HT)
        }

        if (Dungeon.visible[pos]) {
            CellEmitter.get(pos).start(ShaftParticle.FACTORY, 0.2f, 3)
        }
    }

    class Seed : Plant.Seed(Sungrass::class.java, PotionOfHealing::class.java) {
        init {
            image = ItemSpriteSheet.SEED_SUNGRASS
            bones = true
        }
    }

    class Health : Buff() {

        private var pos: Int = 0
        private var healCurr = 1
        private var count = 0
        private var level: Int = 0

        init {
            type = buffType.POSITIVE
        }

        override fun act(): Boolean {
            if (target.pos != pos) {
                detach()
            }
            if (count == 5) {
                if (level <= healCurr.toDouble() * .025 * target.HT.toDouble()) {
                    target.recoverHP(level, this)
                    detach()
                } else {
                    val heal = (healCurr * 0.025f * target.HT).toInt()
                    level -= heal
                    if (healCurr < 6) healCurr++

                    target.recoverHP(heal, this)
                }
                if (target.HP == target.HT && target is Hero) {
                    (target as Hero).resting = false
                }
                count = 1
            } else {
                count++
            }
            if (level <= 0)
                detach()
            spend(STEP)
            return true
        }

        fun absorb(damage: Int): Int {
            level -= damage
            if (level <= 0)
                detach()
            return damage
        }

        fun boost(amount: Int) {
            level += amount
            pos = target.pos
        }

        override fun icon(): Int = BuffIndicator.HEALING

        override fun toString(): String = Messages.get(this, "name")

        override fun desc(): String = Messages.get(this, "desc", level)

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(POS, pos)
            bundle.put(HEALCURR, healCurr)
            bundle.put(COUNT, count)
            bundle.put(LEVEL, level)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            pos = bundle.getInt(POS)
            healCurr = bundle.getInt(HEALCURR)
            count = bundle.getInt(COUNT)
            level = bundle.getInt(LEVEL)

        }

        companion object {
            private const val STEP = 1f

            private const val POS = "pos"
            private const val HEALCURR = "healCurr"
            private const val COUNT = "count"
            private const val LEVEL = "level"
        }
    }
}
