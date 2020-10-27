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

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.actors.hero.perks.Dieting
import com.egoal.darkestpixeldungeon.actors.hero.perks.RavenousAppetite
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.items.artifacts.HornOfPlenty
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Hunger : Buff(), Hero.Doom {

    private var level: Float = 0f
    private var partialDamage: Float = 0f
    private var dmgTokenInTotal = 0

    val isStarving: Boolean get() = level >= STARVING

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEVEL, level)
        bundle.put(PARTIALDAMAGE, partialDamage)
        bundle.put(TOTAL_DAMAGE, dmgTokenInTotal)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        level = bundle.getFloat(LEVEL)
        partialDamage = bundle.getFloat(PARTIALDAMAGE)
        dmgTokenInTotal = bundle.getInt(TOTAL_DAMAGE)
    }

    override fun act(): Boolean {

        if (Dungeon.level.locked) {
            spend(STEP)
            return true
        }

        if (target.isAlive) {

            val hero = target as Hero

            if (isStarving) {
                val dhp = if (hero.heroPerk.get(RavenousAppetite::class.java) != null) target.HT / 50f
                else target.HT / 75f
                partialDamage += dhp

                if (partialDamage > 1) {
                    target.takeDamage(Damage(partialDamage.toInt(), this, target)
                            .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE))
                    if (target.isAlive) {
                        target.takeDamage(Damage(Random.Int(0, partialDamage.toInt() + 1), this, target)
                                .type(Damage.Type.MENTAL).addFeature(Damage.Feature.PURE))
                        if (Random.Float() < 0.2f)
                            (target as Hero).sayShort(HeroLines.WHY_NOT_EAT)

                        // 
                        if (dmgTokenInTotal < 100) {
                            dmgTokenInTotal += partialDamage.toInt()
                            if (dmgTokenInTotal >= 100 && !hero.heroPerk.has(Dieting::class.java)) {
                                val diet = Dieting()
                                hero.heroPerk.add(diet)
                                PerkGain.Show(hero, diet)
                                GLog.w(M.L(this, "dieting"))
                            }
                        }
                    }
                    partialDamage -= partialDamage.toInt().toFloat()
                }

            } else {

                val newLevel = level + HUNGER_PER_STEP
                var statusUpdated = false
                if (newLevel >= STARVING) {

                    GLog.n(Messages.get(this, "onstarving"))
                    hero.resting = false
                    target.takeDamage(Damage(1, this, target).type(Damage.Type
                            .MAGICAL).addFeature(Damage.Feature.PURE))
                    statusUpdated = true

                    hero.interrupt()

                } else if (newLevel >= HUNGRY && level < HUNGRY) {
                    GLog.w(Messages.get(this, "onhungry"))
                    statusUpdated = true
                }
                level = newLevel

                if (statusUpdated) {
                    BuffIndicator.refreshHero()
                }
            }


            val step = when {
                target.buff(Drunk::class.java) != null -> STEP * 0.8f
                (target as Hero).heroPerk.has(Dieting::class.java) -> STEP * 1.2f
                else -> STEP
            }

            spend(if (target.buff(Shadows::class.java) == null) step else step * 1.5f)

        } else {

            diactivate()

        }

        return true
    }

    fun satisfy(energy: Float) {
        var energy = energy

        val buff = target.buff(HornOfPlenty.hornRecharge::class.java)
        if (buff != null && buff.isCursed) {
            energy *= 0.67f
            GLog.n(Messages.get(this, "cursedhorn"))
        }

        reduceHunger(energy)
    }

    //directly interacts with hunger, no checks.
    fun reduceHunger(energy: Float) {

        level -= energy
        if (level < 0) {
            level = 0f
        } else if (level > STARVING) {
            level = STARVING
        }

        BuffIndicator.refreshHero()
    }

    fun hunger(): Int = Math.ceil(level.toDouble()).toInt()

    override fun icon(): Int = when {
        level < HUNGRY -> BuffIndicator.NONE
        level < STARVING -> BuffIndicator.HUNGER
        else -> BuffIndicator.STARVATION
    }

    override fun toString(): String = if (level < STARVING) M.L(this, "hungry")
    else M.L(this, "starving")

    override fun desc(): String {
        var result = if (level < STARVING) M.L(this, "desc_intro_hungry")
        else M.L(this, "desc_intro_starving")

        result += M.L(this, "desc")

        return result
    }

    override fun onDeath() {
        Badges.validateDeathFromHunger()

        Dungeon.fail(javaClass)
        GLog.n(M.L(this, "ondeath"))
    }

    companion object {
        private const val STEP = 10f
        private const val HUNGER_PER_STEP = 11f

        const val HUNGRY = 350f
        const val STARVING = 500f

        private const val LEVEL = "level"
        private const val PARTIALDAMAGE = "partialDamage"
        private const val TOTAL_DAMAGE = "total-damage"
    }
}
