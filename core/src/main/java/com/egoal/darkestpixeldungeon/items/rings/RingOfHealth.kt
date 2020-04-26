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
package com.egoal.darkestpixeldungeon.items.rings

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

class RingOfHealth : Ring() {
    private fun ratio() = 1.25f.pow(level() * 0.3f)

    override fun doEquip(hero: Hero): Boolean {
        return if (super.doEquip(hero)) {
            attach(hero)
            true
        } else false
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        return if (super.doUnequip(hero, collect, single)) {
            detach(hero)
            true
        } else false
    }

    override fun upgrade(): Item {
        if (Dungeon.hero != null && isEquipped(Dungeon.hero!!)) {
            detach(Dungeon.hero!!)
            super.upgrade()
            attach(Dungeon.hero!!)
        } else super.upgrade()

        return this
    }

    private fun detach(hero: Hero) {
        hero.HT = round(hero.HT / ratio()).toInt()
        hero.HP = max(1, round(hero.HP / ratio()).toInt())
    }

    private fun attach(hero: Hero) {
        hero.HT = round(hero.HT * ratio()).toInt()
        hero.HP = max(1, round(hero.HP * ratio()).toInt())
    }

    override fun buff(): RingBuff = Health()

    inner class Health : Ring.RingBuff()
}

