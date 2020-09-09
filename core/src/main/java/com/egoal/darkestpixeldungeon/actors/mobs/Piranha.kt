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
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Frost
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.buffs.Roots
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat
import com.egoal.darkestpixeldungeon.items.unclassified.FishBone
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.sprites.PiranhaSprite
import com.watabou.utils.Random

import java.util.HashSet

class Piranha : Mob() {
    init {
        spriteClass = PiranhaSprite::class.java

        baseSpeed = 2f

        EXP = 0

        addResistances(Damage.Element.LIGHT, -0.5f)
    }

    init {
        HT = 10 + Dungeon.depth * 5
        HP = HT
        defSkill = 10f + Dungeon.depth * 2
    }

    override fun act(): Boolean {
        if (!Level.water[pos]) {
            die(null)
            sprite.killAndErase()
            return true
        } else {
            //this causes pirahna to move away when a door is closed on them.
            Dungeon.level.updateFieldOfView(this, Level.fieldOfView)
            enemy = chooseEnemy()
            val enemy = enemy
            if (state === this.HUNTING && !(enemy != null && enemy.isAlive && Level.fieldOfView[enemy.pos] && enemy.invisible <= 0)) {
                state = this.WANDERING
                val oldPos = pos
                var i = 0
                do {
                    i++
                    target = Dungeon.level.randomDestination()
                    if (i == 100) return true
                } while (!getCloser(target))
                moveSprite(oldPos, pos)
                return true
            }

            return super.act()
        }
    }

    private fun damageRoll(): Int = Random.NormalIntRange(Dungeon.depth, 2 + Dungeon.depth * 2)

    override fun giveDamage(target: Char): Damage = Damage(damageRoll(), this, target)

    override fun attackSkill(target: Char): Float = 20f + Dungeon.depth * 2f

    override fun attackProc(damage: Damage): Damage {
        if (Random.Int(2) == 0)
            Buff.affect(damage.to as Char, Bleeding::class.java).set(damage.value / 2)
        return damage
    }

    private fun drRoll(): Int = Random.NormalIntRange(0, Dungeon.depth)

    override fun defendDamage(dmg: Damage): Damage {
        dmg.value -= drRoll()
        return dmg
    }

    override fun die(src: Any?) {
        Dungeon.level.drop(MysteryMeat(), pos).sprite.drop()
        if (Random.Float() < 0.35f)
            Dungeon.level.drop(FishBone(), pos).sprite.drop()
        super.die(src)

        Statistics.PiranhasKilled += 1
        Badges.validatePiranhasKilled()
    }

    override fun reset(): Boolean = true

    override fun getCloser(target: Int): Boolean {
        if (rooted) return false

        val step = Dungeon.findStep(this, pos, target, Level.water, Level.fieldOfView)
        if (step != -1) {
            move(step)
            return true
        } else {
            return false
        }
    }

    override fun getFurther(target: Int): Boolean {
        val step = Dungeon.flee(this, pos, target, Level.water, Level.fieldOfView)
        if (step != -1) {
            move(step)
            return true
        } else {
            return false
        }
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    companion object {
        private val IMMUNITIES = hashSetOf<Class<*>>(
                Burning::class.java, Paralysis::class.java, ToxicGas::class.java, VenomGas::class.java,
                Roots::class.java, Frost::class.java
        )
    }
}
