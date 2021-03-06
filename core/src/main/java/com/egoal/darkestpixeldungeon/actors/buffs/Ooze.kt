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
package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random

class Ooze : Buff() {

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun icon(): Int {
        return BuffIndicator.OOZE
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun heroMessage(): String? {
        return Messages.get(this, "heromsg")
    }

    override fun desc(): String {
        return Messages.get(this, "desc")
    }

    override fun act(): Boolean {
        if (target.isAlive) {
            if (Dungeon.depth > 4)
            // targetpos.damage( Dungeon.depth/5, this );
                target.takeDamage(Damage(Dungeon.depth / 5, this, target)
                        .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE))
            else if (Random.Int(2) == 0)
                target.takeDamage(Damage(1, this, target)
                        .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE))
            if (!target.isAlive && target === Dungeon.hero) {
                Dungeon.fail(javaClass)
                GLog.n(Messages.get(this, "ondeath"))
            }

            spend(Actor.TICK)
        }
        if (Level.water[target.pos]) {
            detach()
        }
        return true
    }

}
