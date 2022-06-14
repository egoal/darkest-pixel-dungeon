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

import com.egoal.darkestpixeldungeon.Database
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Charm
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.HashSet

abstract class NPC : Mob() {
    init {
        Config = Database.DummyMobConfig.copy(
                MaxHealth = 1, EXP = 0,
        )

        camp = Camp.NEUTRAL
        state = PASSIVE
    }

    // never overlap with an item
    protected fun throwItem() {
        Dungeon.level.heaps.get(pos)?.let {
            val newPos = PathFinder.NEIGHBOURS8.map { it + pos }.filter { Level.passable[it] || Level.avoid[it] }.random()
            Dungeon.level.drop(it.pickUp(), newPos).sprite.drop()
        }
    }

    override fun act(): Boolean {
        return super.act()
    }

    override fun beckon(cell: Int) {}

    abstract fun interact(): Boolean
    
    protected fun tell(text: String) {
        GameScene.show(WndQuest(this, text))
    }

    abstract class Unbreakable : NPC() {
        override fun act(): Boolean {
            if (properties.contains(Property.IMMOVABLE)) throwItem()
            return super.act()
        }

        override fun dexRoll(damage: Damage): Float = 1000f
        override fun takeDamage(dmg: Damage): Int = 0
        override fun add(buff: Buff) {}

        override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

        companion object {
            private val IMMUNITIES = hashSetOf<Class<*>>(
                    Corruption::class.java, Charm::class.java
            )
        }
    }
}