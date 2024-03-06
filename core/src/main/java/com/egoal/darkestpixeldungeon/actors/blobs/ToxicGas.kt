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
package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random
import kotlin.math.max

class ToxicGas : Blob(), Hero.Doom {

    override fun evolve() {
        super.evolve()

        val levelDamage = 5 + Dungeon.depth * 4
        for (ch in affectedChars()) {
            var damage = max(1, (ch.HT + levelDamage) / 30)
            if (Random.Int(30) < (ch.HT + levelDamage) % 30) damage += Random.Int(Dungeon.depth)

            ch.takeDamage(Damage(this, ch, Damage.Type.MAGICAL)
                    .setAdditionalDamage(Damage.Element.Poison, damage))
        }
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)

        emitter.pour(Speck.factory(Speck.TOXIC), 0.4f)
    }

    override fun tileDesc(): String? {
        return Messages.get(this, "desc")
    }

    override fun onDeath() {
        Badges.validateDeathFromGas()

        Dungeon.fail(javaClass)
        GLog.n(Messages.get(this, "ondeath"))
    }
}
