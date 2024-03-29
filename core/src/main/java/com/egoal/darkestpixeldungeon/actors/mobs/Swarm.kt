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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.features.Door
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.SwarmSprite
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class Swarm : Mob() {
    init {
        spriteClass = SwarmSprite::class.java
    }

    override fun giveDamage(target: Char): Damage {
        return if (Random.Int(3) == 0)
            Damage(Random.IntRange(1, 2), this, target).type(Damage.Type.MENTAL)
        else super.giveDamage(target)
    }

    override fun defenseProc(damage: Damage): Damage {
        if (HP >= damage.value + 2) {
            val passable = Level.passable

            val candidates = PathFinder.NEIGHBOURS4.map { it + pos }.filter { passable[it] && Actor.findChar(it) == null }
            if (candidates.isNotEmpty()) {
                val clone = split()
                clone.HP = (HP - damage.value) / 2
                clone.pos = Random.element(candidates)!!
                clone.state = clone.HUNTING
                clone.properties.add(Property.PHANTOM)

                if (Dungeon.level.map[clone.pos] == Terrain.DOOR)
                    Door.Enter(clone.pos, clone)

                GameScene.add(clone, SPLIT_DELAY)
                Actor.addDelayed(Pushing(clone, pos, clone.pos), -1f)

                HP -= clone.HP
            }
        }

        return super.defenseProc(damage)
    }

    private fun split(): Swarm {
        val clone = Swarm()
        if (buff(Burning::class.java) != null) {
            Buff.affect(clone, Burning::class.java).reignite(clone)
        }
        if (buff(Poison::class.java) != null) {
            Buff.affect(clone, Poison::class.java).set(2f)
        }
        if (buff(Corruption::class.java) != null) {
            Buff.affect(clone, Corruption::class.java)
        }
        return clone
    }

    override fun createLoot(): Item? = super.createLoot()?.apply {
        if (this is PotionOfHealing) identify() // identify poh
    }

    companion object {

        private const val SPLIT_DELAY = 1f

        private const val GENERATION = "generation"
    }
}
