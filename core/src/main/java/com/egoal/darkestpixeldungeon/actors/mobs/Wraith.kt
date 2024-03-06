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

import com.egoal.darkestpixeldungeon.Database
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Grim
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.WraithSprite
import com.watabou.noosa.tweeners.AlphaTweener
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.HashSet

open class Wraith : Mob() {

    protected var level: Int = 0

    init {
        spriteClass = WraithSprite::class.java

        Config = Database.DummyMobConfig.copy(
                MaxHealth = 1,
                DefendSkill = 1000f,
        )
        properties.add(Property.UNDEAD)
        addResistances(Damage.Element.Shadow, 0.25f)
        addResistances(Damage.Element.Holy, -0.5f)

        flying = true
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEVEL, level)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        level = bundle.getInt(LEVEL)
        adjustStats(level)
    }

    override fun giveDamage(target: Char): Damage =
            Damage(Random.NormalIntRange(1 + level / 2, 2 + level), this, target)

    override fun dexRoll(damage: Damage): Float = if(damage.type==Damage.Type.NORMAL) super.dexRoll(damage) else 0f

    open fun adjustStats(level: Int) {
        this.level = level
        atkSkill = 10f + level
        defSkill = atkSkill * 5f
        enemySeen = true
    }

    override fun reset(): Boolean {
        state = WANDERING
        return true
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    companion object {

        private const val SPAWN_DELAY = 2f

        private const val LEVEL = "level"

        fun spawnAround(pos: Int) {
            for (n in PathFinder.NEIGHBOURS4) {
                val cell = pos + n
                if (Level.passable[cell] && Actor.findChar(cell) == null) {
                    spawnAt(cell)
                }
            }
        }

        fun spawnAt(pos: Int): Wraith? {
            if (Level.passable[pos] && Actor.findChar(pos) == null) {

                val w = Wraith()
                w.adjustStats(Dungeon.depth)
                w.pos = pos
                w.state = w.HUNTING
                GameScene.add(w, SPAWN_DELAY)

                w.sprite.alpha(0f)
                w.sprite.parent.add(AlphaTweener(w.sprite, 1f, 0.5f))

                w.sprite.emitter().burst(ShadowParticle.CURSE, 5)

                return w
            } else {
                return null
            }
        }

        private val IMMUNITIES = hashSetOf<Class<*>>(
                Grim::class.java, Terror::class.java, Corruption::class.java
        )
    }
}
