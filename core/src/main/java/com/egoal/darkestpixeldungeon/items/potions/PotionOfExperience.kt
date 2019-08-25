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
package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.actors.hero.Hero

class PotionOfExperience : Potion() {
    init {
        initials = 0

        bones = true
    }

    override fun apply(hero: Hero) {
        setKnown()
        hero.earnExp(if (reinforced) hero.maxExp() else hero.maxExp() * 2 / 3)
    }

    override fun price(): Int = if (isKnown) (50 * quantity * if (reinforced) 1.5f else 1f).toInt()
    else super.price()

    override fun canBeReinforced(): Boolean = !reinforced

}
