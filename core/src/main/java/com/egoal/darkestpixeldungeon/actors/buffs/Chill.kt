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
import com.egoal.darkestpixeldungeon.actors.mobs.Thief
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.food.FrozenCarpaccio
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random

import java.text.DecimalFormat

class Chill : FlavourBuff() {

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun attachTo(target: Char): Boolean {
        //can't chill what's frozen!
        if (target.buff(Frost::class.java) != null) return false

        if (super.attachTo(target)) {
            Buff.Companion.detach(target, Burning::class.java)

            //chance of potion breaking is the same as speed factor.
            if (Random.Float(1f) > speedFactor() && target is Hero) {

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

    //reduces speed by 10% for every turn remaining, capping at 50%
    fun speedFactor(): Float {
        return Math.max(0.5f, 1 - cooldown() * 0.1f)
    }

    override fun icon(): Int {
        return BuffIndicator.FROST
    }

    override fun fx(on: Boolean) {
        if (on)
            target.sprite.add(CharSprite.State.CHILLED)
        else
            target.sprite.remove(CharSprite.State.CHILLED)
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns(), DecimalFormat("#.##").format(((1f - speedFactor()) * 100f).toDouble()))
    }
}
