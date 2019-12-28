/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.sprites.BeeSprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.HashSet

class Bee : Mob() {

    private var level: Int = 0

    //-1 refers to a pot that has gone missing.
    private var potPos: Int = 0
    //-1 for no owner
    private var potHolder: Int = 0

    init {
        spriteClass = BeeSprite::class.java

        EXP = 0

        flying = true
        state = WANDERING

        addResistances(Damage.Element.POISON, 0.2f)
    }

    override fun viewDistance(): Int = 4

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEVEL, level)
        bundle.put(POTPOS, potPos)
        bundle.put(POTHOLDER, potHolder)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        spawn(bundle.getInt(LEVEL))
        potPos = bundle.getInt(POTPOS)
        potHolder = bundle.getInt(POTHOLDER)
    }

    fun spawn(level: Int) {
        this.level = level

        HT = (2 + level) * 4
        defSkill = (9 + level).toFloat()
    }

    fun setPotInfo(potPos: Int, potHolder: Char?) {
        this.potPos = potPos
        this.potHolder = potHolder?.id() ?: -1
    }

    override fun attackSkill(target: Char): Float = defSkill

    override fun giveDamage(target: Char): Damage = Damage(Random.NormalIntRange(HT / 10, HT / 4), this, target)

    override fun attackProc(damage: Damage): Damage {
        if (damage.to is Mob) (damage.to as Mob).aggro(this)
        return damage
    }

    override fun chooseEnemy(): Char? {
        //if the pot is no longer present, target the hero
        if (potHolder == -1 && potPos == -1)
            return Dungeon.hero
        else if (Actor.findById(potHolder) != null)
            return Actor.findById(potHolder) as Char
        else {
            //if already targeting something, and that thing is still alive and
            // near the pot, keeping targeting it.
            if (enemy != null && enemy.isAlive && Dungeon.level.mobs.contains(enemy)
                    && Level.fieldOfView[enemy.pos] && enemy.invisible == 0 && nearPot(enemy))
                return enemy

            //pick one, if there are none, check if the hero is near the pot, go for them, otherwise go for nothing.
            val enemies = Dungeon.level.mobs.filter { it.camp != Camp.NEUTRAL && it !is Bee && nearPot(it) }

            return when {
                enemies.isNotEmpty() -> enemies.random()
                nearPot(Dungeon.hero) -> Dungeon.hero
                else -> null
            }
        }//if the pot is on the ground
        //if something is holding the pot, target that
    }

    override fun getCloser(target: Int): Boolean {
        var target = target
        if (enemy != null && Actor.findById(potHolder) === enemy) {
            target = enemy.pos
        } else if (potPos != -1 && (state === WANDERING || Dungeon.level.distance(target, potPos) > 3)) {
            target = potPos
            this.target = target
        }
        return super.getCloser(target)
    }

    private fun nearPot(char: Char): Boolean = Dungeon.level.distance(char.pos, potPos) <= 3

    companion object {
        private const val LEVEL = "level"
        private const val POTPOS = "potpos"
        private const val POTHOLDER = "potholder"
    }

}