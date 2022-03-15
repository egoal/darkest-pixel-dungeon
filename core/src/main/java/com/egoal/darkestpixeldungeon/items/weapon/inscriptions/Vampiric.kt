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
package com.egoal.darkestpixeldungeon.items.weapon.inscriptions

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class Vampiric : Inscription(9) {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val defender = damage.to as Char
        val attacker = damage.from as Char

        if(defender.properties().contains(Char.Property.UNDEAD) ||
                defender.properties().contains(Char.Property.MACHINE)) {
            val level = max(0, weapon.level())

            // lvl 0 - 20% -> .25
            // lvl 1 - 21.5% -> .268
            // lvl 2 - 23% -> .286
            val maxValue = round(damage.value * ((level + 10) / (level + 40).toFloat())).toInt()
            val effValue = min(Random.IntRange(0, maxValue), attacker.HT - attacker.HP)

            if (effValue > 0)
                attacker.recoverHP(effValue, this)
        }

        return damage
    }
}
