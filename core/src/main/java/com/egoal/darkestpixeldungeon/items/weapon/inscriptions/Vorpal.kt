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
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.PointF
import com.watabou.utils.Random
import kotlin.math.max

class Vorpal : Inscription(10) {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        val defender = damage.to as Char
        // lvl 0 - 33%
        // lvl 1 - 50%
        // lvl 2 - 60%
        val level = max(0, weapon.level())

        if (Random.Int(level + 3) >= 2) {

            Buff.affect(defender, Bleeding::class.java).set(damage.value / 4)
            Splash.at(defender.sprite.center(), -PointF.PI / 2, PointF.PI / 6,
                    defender.sprite.blood(), 10)

        }

        return damage
    }
}
