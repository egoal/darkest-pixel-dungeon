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
package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.RatKingSprite

class RatKing : NPC.Unbreakable() {

    init {
        spriteClass = RatKingSprite::class.java

        state = SLEEPING
    }

    override fun speed(): Float {
        return 2f
    }

    override fun chooseEnemy(): Char? {
        return null
    }

    override fun reset(): Boolean {
        return true
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)
        if (state === SLEEPING) {
            notice()
            yell(Messages.get(this, "not_sleeping"))
            state = WANDERING
        } else {
            yell(Messages.get(this, "what_is_it"))
        }
        return true
    }

    override fun description(): String = if ((sprite as RatKingSprite).festive) Messages.get(this, "desc_festive")
    else super.description()
}
