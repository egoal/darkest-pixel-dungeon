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

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Unstable : Weapon.Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        Random.oneOf(*randomEnchants).newInstance().proc(weapon, damage)
        return damage
    }

    override fun glowing(): ItemSprite.Glowing = WHITE

    companion object {

        private val WHITE = ItemSprite.Glowing(0xFFFFFF)

        private val randomEnchants = arrayOf(
                Blazing::class.java, Chilling::class.java, Dazzling::class.java, Eldritch::class.java,
                Grim::class.java, Lucky::class.java, Shocking::class.java, Stunning::class.java,
                Vampiric::class.java, Vorpal::class.java)
    }
}
