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

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite.Glowing
import com.watabou.utils.Random
import kotlin.math.max

class Lucky : Inscription(4) {
    private var chanceFix = 0f // no need to store.

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val level = max(0, weapon.level())
        val ratio = .55f + chanceFix

        if(Random.Float()< ratio) {
            damage.value *= 2
            chanceFix = 0f
        }
        else {
            damage.value = 0
            chanceFix += 0.01f * level
        }

        return damage
    }

    companion object {
        private const val RADIX = 10
    }
}
