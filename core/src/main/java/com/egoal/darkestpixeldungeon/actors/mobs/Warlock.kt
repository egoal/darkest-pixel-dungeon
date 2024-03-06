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
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.WarlockSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Callback
import com.watabou.utils.Random

class Warlock : Mob(), Callback {

    init {
        spriteClass = WarlockSprite::class.java
    }

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy)
            .setAdditionalDamage(Damage.Element.Shadow, Random.NormalIntRange(2, 10))

    override fun canAttack(enemy: Char): Boolean = Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos

    override fun doAttack(enemy: Char): Boolean {
        if (Dungeon.level.adjacent(pos, enemy.pos)) {
            return super.doAttack(enemy)
        } else {

            val visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos]
            if (visible) {
                sprite.zap(enemy.pos)
            } else {
                zap()
            }

            return !visible
        }
    }

    private fun zap() {
        spend(TIME_TO_ZAP)

        val dmg = giveDamage(enemy!!).addFeature(Damage.Feature.RANGED)

        ProcessAttackDamage(dmg, onHit = {
            if (enemy === Dungeon.hero && Random.Int(2) == 0)
                Buff.prolong(enemy!!, Weakness::class.java, Weakness.duration(enemy!!))
        }) {
            if (enemy === Dungeon.hero) {
                Dungeon.fail(javaClass)
                GLog.n(M.L(this, "bolt_kill"))
            }
        }
    }

    fun onZapComplete() {
        zap()
        next()
    }

    override fun call() {
        next()
    }

    companion object {
        private const val TIME_TO_ZAP = 1f
    }
}
