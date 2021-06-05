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

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

open class Dagger : MeleeWeapon() {

    init {
        image = ItemSpriteSheet.DAGGER

        tier = 1
    }

    override fun min(lvl: Int): Int = lvl

    // 0~9， down from 1~10, scaling unchanged
    override fun max(lvl: Int): Int = 4 * (tier + 1) + 1 + lvl * (tier + 1)

    // check AssassinsBlade
    override fun giveDamage(hero: Hero, target: Char): Damage =
            if (target is Mob && target.surprisedBy(hero)) {
                // assassin, deals avg damage to max on surprise, instead of min to max.
                val dmg = Damage(imbue.damageFactor(Random.NormalIntRange((min() + max()) / 2, max())), hero, target)
                val exStr = hero.STR() - STRReq()
                if (exStr > 0) dmg.value += exStr
                dmg
            } else
                super.giveDamage(hero, target)
}
