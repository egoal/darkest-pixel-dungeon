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
package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Frost
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.SnowParticle
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.levels.Level
import com.watabou.utils.Random

object Freezing {
    // Returns true, if this cell is visible
    fun affect(cell: Int, fire: Fire?): Boolean {

        Actor.findChar(cell)?.let {
            val duration = Frost.duration(it)* if(Level.water[it.pos]) Random.Float(5f, 7.5f) else Random.Float(1f, 1.5f)
            Buff.prolong(it, Frost::class.java, duration)
        }

        fire?.clear(cell)

        Dungeon.level.heaps.get(cell)?.freeze()

        return if (Dungeon.visible[cell]) {
            CellEmitter.get(cell).start(SnowParticle.FACTORY, 0.2f, 6)
            true
        } else false
    }
}
