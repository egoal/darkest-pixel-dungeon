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
package com.egoal.darkestpixeldungeon

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.VampiricBite
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M

enum class Challenge {
    LowPressure,
    Gifted,
    BruteCourage,
    Immortality {
        override fun affect(hero: Hero) {
            Buff.affect(hero, VampiricBite::class.java)
        }
    },
    GreedIsGood,
    Loner,
    Faith,
    ;

    fun title(): String = M.L(this, "${name.toLowerCase()}.name")
    fun desc(): String = M.L(this, "${name.toLowerCase()}.desc")
    
    open fun affect(hero: Hero) {}
}
