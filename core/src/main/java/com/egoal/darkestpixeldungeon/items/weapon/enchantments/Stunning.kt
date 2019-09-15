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
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Random

class Stunning : Weapon.Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val defender = damage.to as Char
        // lvl 0 - 13%
        // lvl 1 - 22%
        // lvl 2 - 30%
        val level = Math.max(0, weapon.level())

        if (Random.Int(level + 8) >= 7) {
            Buff.prolong(defender, Paralysis::class.java, Random.Float(1f, 1.5f + level))
            defender.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12)
        }

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = YELLOW

    companion object {
        private val YELLOW = ItemSprite.Glowing(0xCCAA44)
    }
}
