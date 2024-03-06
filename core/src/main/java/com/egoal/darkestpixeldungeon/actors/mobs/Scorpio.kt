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
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.CrippleAttack
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.sprites.ScorpioSprite
import com.watabou.utils.Random

open class Scorpio : Mob() {
    init {
        spriteClass = ScorpioSprite::class.java

        abilities.add(CrippleAttack())
    }

    override fun viewDistance(): Int = 6

    override fun giveDamage(target: Char): Damage = super.giveDamage(target)
            .setAdditionalDamage(Damage.Element.Poison, Random.NormalIntRange(0, 12))
            .addFeature(Damage.Feature.RANGED)

    override fun canAttack(enemy: Char): Boolean {
        val attack = Ballistica(pos, enemy.pos, Ballistica.PROJECTILE)
        return !Dungeon.level.adjacent(pos, enemy.pos) && attack.collisionPos == enemy.pos
    }

    override fun getCloser(target: Int): Boolean {
        return if (state === HUNTING) {
            enemySeen && getFurther(target)
        } else {
            super.getCloser(target)
        }
    }
}
