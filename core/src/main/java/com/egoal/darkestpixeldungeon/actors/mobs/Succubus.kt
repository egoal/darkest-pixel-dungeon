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
import com.egoal.darkestpixeldungeon.actors.buffs.Sleep
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.CharmAttack
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.sprites.SuccubusSprite
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.*

class Succubus : Mob() {
    private var delay = 0

    init {
        spriteClass = SuccubusSprite::class.java

        abilities.add(CharmAttack())
        immunities.add(Sleep::class.java)
    }

    override fun viewDistance(): Int = 6

    override fun giveDamage(target: Char): Damage = super.giveDamage(target).addElement(Damage.Element.ICE)

    override fun getCloser(target: Int): Boolean {
        if (Level.fieldOfView[target] && Dungeon.level.distance(pos, target) > 2
                && delay <= 0) {

            blink(target)
            spend(-1 / speed())
            return true

        } else {

            delay--
            return super.getCloser(target)

        }
    }

    private fun blink(target: Int) {

        val route = Ballistica(pos, target, Ballistica.PROJECTILE)
        var cell = route.collisionPos

        //can't occupy the same cell as another char, so move back one.
        if (Actor.findChar(cell) != null && cell != this.pos)
            cell = route.path[route.dist - 1]

        if (Level.avoid[cell]) {
            val candidates = ArrayList<Int>()
            for (n in PathFinder.NEIGHBOURS8) {
                cell = route.collisionPos + n
                if (Level.passable[cell] && Actor.findChar(cell) == null) {
                    candidates.add(cell)
                }
            }
            if (candidates.size > 0)
                cell = Random.element(candidates)!!
            else {
                delay = BLINK_DELAY
                return
            }
        }

        ScrollOfTeleportation.appear(this, cell)

        delay = BLINK_DELAY
    }

    companion object {
        private const val BLINK_DELAY = 5
    }
}
