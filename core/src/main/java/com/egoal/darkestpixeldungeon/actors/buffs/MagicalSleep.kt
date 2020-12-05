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

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import kotlin.math.min

open class MagicalSleep : Buff() {

    private var sleeped_ = 0f

    override fun attachTo(target: Char): Boolean {
        if (super.attachTo(target) && !target.immunizedBuffs().contains(Sleep::class.java)) {

            if (target is Hero)
                GLog.i(Messages.get(this, "fallasleep"))
            else if (target is Mob)
                target.state = target.SLEEPING

            target.paralysed++

            return true
        } else {
            return false
        }
    }

    override fun act(): Boolean {
        if (target is Hero) {
            target.HP = min(target.HP + 1, target.HT)
            (target as Hero).resting = true
            target.buff(Pressure::class.java)!!.downPressure(.5f)
            sleeped_ += STEP
            if (sleeped_ > MAX_SLEEP_TIME) {
                GLog.p(Messages.get(this, "wakeup"))
                detach()
            }
        }
        spend(STEP)
        return true
    }

    override fun detach() {
        if (target.paralysed > 0) target.paralysed--
        if (target is Hero) (target as Hero).resting = false
        super.detach()
    }

    override fun icon(): Int = BuffIndicator.MAGIC_SLEEP

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc")

    companion object {
        private const val STEP = 1f
        private const val MAX_SLEEP_TIME = 30f
    }

    class Deep : MagicalSleep() {
        init {
            type = buffType.NEGATIVE
        }

        var damage: Damage? = null
        var ratio: Float = 0.01f

        override fun icon(): Int = BuffIndicator.NONE

        override fun detach() {
            if (damage != null) {
                // delay it
                Actor.addDelayed(object : Actor() {
                    override fun act(): Boolean {
                        if (target.isAlive) {
                            val dmg = damage!!
                            if (dmg.value > 0) {
                                dmg.value = (1 + dmg.value * ratio).toInt()
                                target.takeDamage(dmg.type(Damage.Type.MAGICAL).addElement(Damage.Element.SHADOW))
                            }

                            if (dmg.from is Char)
                                affect(target, Terror::class.java, 3f).objectid = (dmg.from as Char).id()
                        }

                        Actor.remove(this)
                        return true
                    }
                }, -1f)
            }

            super.detach()
        }
    }
}
