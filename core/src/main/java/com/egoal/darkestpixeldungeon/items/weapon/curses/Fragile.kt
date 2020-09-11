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
package com.egoal.darkestpixeldungeon.items.weapon.curses

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Bundle

class Fragile : Weapon.Enchantment() {
    private var hits = 0

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val defender = damage.to as Char
        val attacker = damage.from as Char
        //degrades from 100% to 25% damage over 150 hits
        damage.value *= (1f - hits * 0.005f).toInt()
        if (hits < 150) hits++
        return damage
    }

    override fun curse(): Boolean {
        return true
    }

    override fun glowing(): ItemSprite.Glowing {
        return BLACK
    }

    override fun restoreFromBundle(bundle: Bundle) {
        hits = bundle.getInt(HITS)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(HITS, hits)
    }

    companion object {

        private val BLACK = ItemSprite.Glowing(0x000000)

        private val HITS = "hits"
    }

}
