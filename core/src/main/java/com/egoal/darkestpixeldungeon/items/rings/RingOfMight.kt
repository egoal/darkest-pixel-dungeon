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


import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item

class RingOfMight : Ring() {

    override fun doEquip(hero: Hero): Boolean {
        if (super.doEquip(hero)) {
            hero.HT = hero.HT + level() * 5
            hero.HP = Math.min(hero.HP, hero.HT)
            return true
        } else {
            return false
        }
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {

        if (super.doUnequip(hero, collect, single)) {
            hero.HT = hero.HT - level() * 5
            hero.HP = Math.min(hero.HP, hero.HT)
            return true
        } else {
            return false
        }

    }

    override fun upgrade(): Item {
        if (buff != null && buff!!.target != null) {
            buff!!.target.HT = buff!!.target.HT + 5
        }
        return super.upgrade()
    }

    override fun level(value: Int) {
        if (buff != null && buff!!.target != null) {
            buff!!.target.HT = buff!!.target.HT - level() * 5
        }
        super.level(value)
        if (buff != null && buff!!.target != null) {
            buff!!.target.HT = buff!!.target.HT + level() * 5
            buff!!.target.HP = Math.min(buff!!.target.HP, buff!!.target.HT)
        }
    }

    override fun buff(): Ring.RingBuff {
        return Might()
    }

    inner class Might : Ring.RingBuff()
}

