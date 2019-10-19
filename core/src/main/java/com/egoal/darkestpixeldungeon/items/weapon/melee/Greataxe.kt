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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.PathFinder

class Greataxe : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.GREATAXE

        tier = 5
    }

    override fun max(lvl: Int): Int {
        return 5 * (tier + 3) +    //40 base, up from 30
                lvl * (tier + 1)   //scaling unchanged
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) + 1

    override fun proc(dmg: Damage): Damage {
        val pos = (dmg.to as Char).pos
        for (i in PathFinder.NEIGHBOURS8) {
            val mob = Dungeon.level.findMobAt(pos + i)
            if (mob != null && mob.hostile)
                mob.takeDamage(mob.defendDamage(Damage(dmg.value / 2, dmg.from, dmg.to).type(dmg.type)))
        }

        return super.proc(dmg)
    }
}
