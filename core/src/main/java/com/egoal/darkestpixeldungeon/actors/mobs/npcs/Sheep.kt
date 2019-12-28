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

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.SheepSprite
import com.watabou.utils.Random

class Sheep : NPC.Unbreakable() {
    var lifespan: Float = 0f

    private var initialized = false

    init {
        spriteClass = SheepSprite::class.java
    }

    override fun act(): Boolean {
        if (initialized) {
            HP = 0

            destroy()
            sprite.die()
        } else {
            initialized = true
            spend(lifespan + Random.Float(2f))
        }
        return true
    }

    override fun interact(): Boolean {
        yell(Messages.get(this, Random.element(LINE_KEYS)))
        return false
    }

    companion object {

        private val LINE_KEYS = arrayOf("Baa!", "Baa?", "Baa.", "Baa...")
    }
}