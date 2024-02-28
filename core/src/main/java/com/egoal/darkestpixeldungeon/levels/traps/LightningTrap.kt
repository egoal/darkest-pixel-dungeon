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

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.effects.Lightning
import com.egoal.darkestpixeldungeon.effects.particles.SparkParticle
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.utils.Random

import java.util.ArrayList
import kotlin.math.ceil
import kotlin.math.max

class LightningTrap : Trap() {

    init {
        color = TrapSprite.TEAL
        shape = TrapSprite.CROSSHAIR
    }

    override fun activate() {
        Actor.findChar(pos)?.let {
            it.takeDamage(Damage(max(1, Random.Int(it.HP / 3, it.HP * 2 / 3)), this, it).convertToElement(Damage.Element.LIGHT))

            if (it === Dungeon.hero) {
                Camera.main.shake(2f, 0.3f)
                if (!it.isAlive) {
                    Dungeon.fail(javaClass)
                    GLog.n(M.L(this, "ondeath"))
                }
            }

            val arcs = ArrayList<Lightning.Arc>()
            arcs.add(Lightning.Arc(pos - Dungeon.level.width(), pos + Dungeon.level.width()))
            arcs.add(Lightning.Arc(pos - 1, pos + 1))

            it.sprite.parent.add(Lightning(arcs, null))
        }

        Dungeon.level.heaps.get(pos)?.let {
            val item = it.items.peek()
            if (item is Wand) item.curCharges += ceil((item.maxCharges - item.curCharges) / 2f).toInt()
        }

        CellEmitter.center(pos).burst(SparkParticle.FACTORY, Random.IntRange(3, 4))
    }
}
