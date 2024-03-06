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
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.ReleaseGasDefend_Toxic
import com.egoal.darkestpixeldungeon.plants.Rotberry
import com.egoal.darkestpixeldungeon.sprites.RotHeartSprite

class RotHeart : Mob() {

    init {
        spriteClass = RotHeartSprite::class.java

        state = PASSIVE

        immunities.addAll(listOf(Terror::class.java, Bleeding::class.java))
        abilities.add(ReleaseGasDefend_Toxic())
    }

    override fun takeDamage(dmg: Damage): Int {
        if (dmg.element == Damage.Element.Fire && dmg.add_value > 0) {
            destroy()
            sprite.die()

            return HP
        } else {
            return super.takeDamage(dmg)
        }

    }

    override fun beckon(cell: Int) {
        //do nothing
    }

    override fun getCloser(target: Int): Boolean = false

    override fun destroy() {
        super.destroy()
        Dungeon.level.mobs.filterIsInstance<RotLasher>().forEach { it.die(null) }
    }

    override fun die(cause: Any?) {
        super.die(cause)
        Dungeon.level.drop(Rotberry.Seed(), pos).sprite.drop()
    }
}
