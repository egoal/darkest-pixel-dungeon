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
package com.egoal.darkestpixeldungeon.levels.traps

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.buffs.Roots
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.sprites.TrapSprite

class GrippingTrap : Trap() {

    init {
        color = TrapSprite.GREY
        shape = TrapSprite.CROSSHAIR
    }

    override fun activate() {

        val c = Actor.findChar(pos)

        if (c != null) {
            var damage = c.defendDamage(Damage(Dungeon.depth, this, c)).value
            if (damage < 0) damage = 0
            Buff.affect(c, Bleeding::class.java).set(damage)
            Buff.prolong(c, Cripple::class.java, 15f)
            Buff.prolong(c, Roots::class.java, 5f)
            Wound.hit(c)
        } else {
            Wound.hit(pos)
        }

    }
}
