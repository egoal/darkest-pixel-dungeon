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

class Sai : MeleeWeapon() {

    init {
        image = ItemSpriteSheet.SAI

        tier = 3
        DLY = 0.5f //2x speed
    }

    //10 base, down from 20, +2 per level, down from +4
    override fun max(lvl: Int): Int = Math.round(2.5f * (tier + 1)) +
            lvl * Math.round(0.5f * (tier + 1))

    override fun defendDamage(dmg: Damage): Damage {
        if (dmg.to !is Hero || STRReq() <= (dmg.to as Hero).STR()) {
            if (dmg.type == Damage.Type.NORMAL)
                dmg.value -= 3
        }

        return dmg
    }
}
