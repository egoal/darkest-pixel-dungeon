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

import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.VampireAttack
import com.egoal.darkestpixeldungeon.sprites.BatSprite
import com.watabou.utils.Random

class Bat : Mob() {

    init {
        spriteClass = BatSprite::class.java

        baseSpeed = 2f
        flying = true

        abilities.add(VampireAttack())
    }

    override fun viewDistance(): Int = if (Statistics.Clock.state == Statistics.ClockTime.State.Day) 3 else seeDistance()

    override fun giveDamage(enemy: Char): Damage {
        return if (Random.Int(4) == 0)
            Damage(Random.NormalIntRange(1, 5), this, enemy).type(Damage.Type.MENTAL)
        else {
            val dmg = super.giveDamage(enemy)
            if (Statistics.Clock.state != Statistics.ClockTime.State.Day)
                dmg.setAdditionalDamage(Damage.Element.SHADOW, dmg.value / 4)

            dmg
        }
    }
}
