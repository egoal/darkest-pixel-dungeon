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

import com.egoal.darkestpixeldungeon.actors.buffs.Amok
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Sleep
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Imp
import com.egoal.darkestpixeldungeon.sprites.GolemSprite

class Golem : Mob() {
    init {
        spriteClass = GolemSprite::class.java
        immunities.addAll(listOf(Amok::class.java, Terror::class.java, Sleep::class.java, Bleeding::class.java))
    }

    override fun attackDelay(): Float = 1.5f

    override fun die(cause: Any?) {
        Imp.Quest.process(this)

        super.die(cause)
    }
}
