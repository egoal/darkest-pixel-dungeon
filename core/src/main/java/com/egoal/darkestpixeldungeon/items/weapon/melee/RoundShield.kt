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
package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import kotlin.math.round

class RoundShield : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.ROUND_SHIELD

        tier = 3
    }

    //12 base, down from 20, +2 per level, down from +4
    override fun max(lvl: Int): Int = 3 * (tier + 1) + lvl * (tier - 1)

    override fun defendDamage(dmg: Damage): Damage {
        var value = 4 + round(1.75f * level()).toInt()

        if(dmg.to is Hero) {
            val burden = STRReq() - (dmg.to as Hero).STR()
            if (burden > 0) value -= 2 * burden
        }
            
        if (value > 0) {
            if (dmg.isFeatured(Damage.Feature.RANGED))
                value += round(value / 5f).toInt()

            if (dmg.type == Damage.Type.NORMAL)
                dmg.value -= value
            else if (dmg.type == Damage.Type.MAGICAL)
                dmg.value -= value * 4 / 5
        }

        return dmg
    }
}