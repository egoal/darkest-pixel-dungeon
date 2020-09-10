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
package com.egoal.darkestpixeldungeon.items.armor.curses

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Random

class Metabolism : Armor.Glyph() {

    override fun proc(armor: Armor, damage: Damage): Damage {
        val attacker = damage.from as Char
        val defender = damage.to as Char

        if (Random.Int(6) == 0) {

            //assumes using up 10% of starving, and healing of 1 hp per 10 turns;
            val healing = Math.min(Hunger.STARVING.toInt() / 100, defender.HT - defender.HP)

            if (healing > 0) {

                val hunger = defender.buff(Hunger::class.java)

                if (hunger != null && !hunger.isStarving) {

                    hunger.reduceHunger((healing * -10).toFloat())
                    BuffIndicator.refreshHero()

                    defender.HP = defender.HP + healing
                    defender.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1)
                    defender.sprite.showStatus(CharSprite.POSITIVE, Integer.toString(healing))
                }
            }

        }

        return damage
    }

    override fun glowing(): ItemSprite.Glowing {
        return BLACK
    }

    override fun curse(): Boolean {
        return true
    }

    companion object {

        private val BLACK = ItemSprite.Glowing(0x000000)
    }
}
