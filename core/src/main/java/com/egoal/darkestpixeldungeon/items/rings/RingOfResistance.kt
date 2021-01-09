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
import kotlin.math.pow

class RingOfResistance : Ring() {

    override fun buff(): Ring.RingBuff = Resistance()

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
        if (!Dungeon.isHeroNull && isEquipped(Dungeon.hero!!)) {
            detach(Dungeon.hero!!)
            super.upgrade()
            attach(Dungeon.hero!!)
        } else super.upgrade()
        
        return this
    }

    private fun detach(hero: Hero) {
        hero.magicalResistance -= magicalResistance(level())
        val eleResistance = elementalResistance(level())
        for (i in 0 until hero.elementalResistance.size) hero.elementalResistance[i] -= eleResistance
    }

    private fun attach(hero: Hero) {
        hero.magicalResistance += magicalResistance(level())
        val eleResistance = elementalResistance(level())
        for (i in 0 until hero.elementalResistance.size) hero.elementalResistance[i] += eleResistance
    }

    inner class Resistance : Ring.RingBuff() {
        // decrease debuff duration
        fun durationFactor(): Float {
            return if (level() < 0) 1f else (1f + 0.5f * level()) / (1f + level())
        }
    }

    companion object {
        //todo: generalize this into hero, perhaps use trigger
        private fun magicalResistance(level: Int) = 0.5f - 0.5f * 0.8f.pow(level / 2f)

        private fun elementalResistance(level: Int) = 1f - 0.9f.pow(level / 3f)
    }
}
