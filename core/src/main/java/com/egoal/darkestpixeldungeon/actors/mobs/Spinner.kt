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

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Web
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.actors.buffs.Roots
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.PoisonAttack
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.SpinnerSprite
import com.watabou.utils.Random

class Spinner : Mob() {

    init {
        spriteClass = SpinnerSprite::class.java

        FLEEING = Fleeing()

        immunities.add(Roots::class.java)
        abilities.add(PoisonAttack())
    }

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy)

    override fun act(): Boolean {
        val result = super.act()

        if (state === FLEEING && buff(Terror::class.java) == null &&
                enemy != null && enemySeen && enemy!!.buff(Poison::class.java) == null) {
            state = HUNTING
        }
        return result
    }

    override fun attackProc(dmg: Damage): Damage {
        val enemy = dmg.to as Char
        if (enemy.buff(Poison::class.java) != null) state = FLEEING

        return dmg
    }

    override fun move(step: Int) {
        if (state === FLEEING) {
            GameScene.add(Blob.seed(pos, Random.Int(5, 7), Web::class.java))
        }
        super.move(step)
    }

    private inner class Fleeing : Mob.Fleeing() {
        override fun nowhereToRun() {
            if (buff(Terror::class.java) == null) {
                state = HUNTING
            } else {
                super.nowhereToRun()
            }
        }
    }
}
