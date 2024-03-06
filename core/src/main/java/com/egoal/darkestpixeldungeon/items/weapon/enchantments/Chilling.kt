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
package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Chill
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Chilling : Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        use(weapon)

        if (Random.Float() < 0.35f) {
            val defender = damage.to as Char

            Buff.prolong(defender, Chill::class.java, Random.Float(2f, 3f))
            Splash.at(defender.sprite.center(), -0x4d2901, 5)
        }

        return damage.setAdditionalDamage(Damage.Element.Ice, Random.Int(2, damage.value / 4))
    }

    override fun glowing(): ItemSprite.Glowing = TEAL

    companion object {
        private val TEAL = ItemSprite.Glowing(0x00FFFF)
    }

}
