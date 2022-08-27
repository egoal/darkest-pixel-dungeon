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
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.BleedingAttack
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.OozeAttack
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.ReleaseGasDefend_StenchGas
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Ghost
import com.egoal.darkestpixeldungeon.sprites.AlbinoSprite
import com.egoal.darkestpixeldungeon.sprites.FetidRatSprite
import com.egoal.darkestpixeldungeon.sprites.RatSprite

class Rat : Mob() {
    init {
        spriteClass = RatSprite::class.java;
    }
}

class Albino : Mob() {
    init {
        spriteClass = AlbinoSprite::class.java
        abilities.add(BleedingAttack())
    }

    override fun die(cause: Any?) {
        super.die(cause)
        Badges.validateRare(this)
    }
}

class FetidRat : Mob() {
    init {
        spriteClass = FetidRatSprite::class.java
        state = WANDERING

        abilities.add(OozeAttack())
        abilities.add(ReleaseGasDefend_StenchGas())
    }

    override fun die(cause: Any?) {
        super.die(cause)

        Ghost.Quest.process()
    }
}
