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
package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.BanditSprite
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.utils.Random

class Bandit : Thief() {

    var item: Item? = null

    init {
        spriteClass = BanditSprite::class.java

        //1 in 30 chance to be a crazy bandit, equates to overall 1/90 chance.
        // lootChance = 0.333f;
    }

    override fun steal(hero: Hero): Boolean {
        return if (super.steal(hero)) {
            Buff.prolong(hero, Blindness::class.java, Random.Int(2, 5).toFloat())
            Buff.affect(hero, Poison::class.java).set(Random.Int(5, 7) * Poison.durationFactor(enemy))
            Buff.prolong(hero, Cripple::class.java, Random.Int(3, 8).toFloat())
            Dungeon.observe()

            true
        } else false

    }

    override fun die(cause: Any?) {
        super.die(cause)
        Badges.validateRare(this)
    }
}
