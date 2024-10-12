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

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.Thief
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.food.FrozenCarpaccio
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance.Resistance
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog

class Frost : FlavourBuff(), Char.IIncomingDamageProc {

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun attachTo(target: Char): Boolean {
        if (super.attachTo(target)) {

            target.paralysed = target.paralysed + 1
            Buff.Companion.detach(target, Burning::class.java)
            Buff.Companion.detach(target, Chill::class.java)

            if (target is Hero) {

                val hero = target
                var item = hero.belongings.randomUnequipped()
                if (item is Potion && !(item is PotionOfStrength || item is PotionOfMight)) {

                    item = item.detach(hero.belongings.backpack)
                    GLog.w(Messages.get(this, "freezes", item!!.toString()))
                    (item as Potion).shatter(hero.pos)

                } else if (item is MysteryMeat) {

                    item = item.detach(hero.belongings.backpack)
                    val carpaccio = FrozenCarpaccio()
                    if (!carpaccio.collect(hero.belongings.backpack)) {
                        Dungeon.level.drop(carpaccio, target.pos).sprite.drop()
                    }
                    GLog.w(Messages.get(this, "freezes", item!!.toString()))

                }
            } else if (target is Thief) {

                val item = target.item

                if (item is Potion && !(item is PotionOfStrength || item is PotionOfMight)) {
                    (target.item as Potion).shatter(target.pos)
                    target.item = null
                }

            }

            return true
        } else {
            return false
        }
    }

    override fun procIncommingDamage(damage: Damage) {
        damage.scale(1.5f)
    }

    override fun detach() {
        super.detach()
        if (target.paralysed > 0)
            target.paralysed = target.paralysed - 1
        if (Level.water[target.pos])
            Buff.Companion.prolong(target, Chill::class.java, 4f)
    }

    override fun icon(): Int {
        return BuffIndicator.FROST
    }

    override fun fx(on: Boolean) {
        if (on)
            target.sprite.add(CharSprite.State.FROZEN)
        else
            target.sprite.remove(CharSprite.State.FROZEN)
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns())
    }

    companion object {
        const val DURATION = 5f

        fun duration(ch: Char): Float {
            val r = ch.buff(Resistance::class.java)
            return if (r != null) r.durationFactor() * DURATION else DURATION
        }
    }
}
