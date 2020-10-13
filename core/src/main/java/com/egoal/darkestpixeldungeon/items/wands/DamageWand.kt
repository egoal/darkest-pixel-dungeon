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
package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.*
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.items.rings.RingOfArcane
import com.egoal.darkestpixeldungeon.items.rings.RingOfSharpshooting
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PointF
import com.watabou.utils.Random
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.round

// for wands that directly damage a targetpos
// wands with AOE effects count here (e.g. fireblast), but wands with indrect
// damage do not (e.g. venom, transfusion)
//todo: from 0.5.0 this class can be merged with class Wand
abstract class DamageWand(isMissile: Boolean) : Wand(isMissile) {
    fun min(): Int = min(level())

    abstract fun min(lvl: Int): Int

    fun max(): Int = max(level())

    abstract fun max(lvl: Int): Int

    open fun giveDamage(enemy: Char): Damage = Damage(damageRoll(), curUser, enemy).type(Damage.Type.MAGICAL)

    private fun damageRoll(): Int = round(Random.NormalIntRange(min(), max()) * Dungeon.hero.arcaneFactor()).toInt()

    fun damageRoll(lvl: Int): Int = round(Random.NormalIntRange(min(lvl), max(lvl)) * Dungeon.hero.arcaneFactor()).toInt()

    override fun statsDesc(): String = if (levelKnown) M.L(this, "stats_desc", min(), max())
    else M.L(this, "stats_desc", min(0), max(0))

    protected open fun particleColor(): Int = 0xffffff

    // wand hit process
    override fun onZap(attack: Ballistica) {
        Actor.findChar(attack.collisionPos)?.let { damage(it) }
    }

    protected fun damage(enemy: Char): Boolean = processWandDamage(giveDamage(enemy))

    private fun checkHit(damage: Damage): Boolean {
//        return Char.CheckDamageHit(damage) //todo: use a specified wand damage check
        val attacker = curUser
        val defender = damage.to as Char

        if (attacker.buff(Shock::class.java) != null || defender.buff(MustDodge::class.java) != null) return false

        if (defender.buff(Unbalance::class.java) != null) return true

        if (damage.value == 0 || damage.isFeatured(Damage.Feature.ACCURATE)) return true // non damage wand, dont miss

        val bonus = 2f * 0.9f.pow(Dungeon.level.distance(attacker.pos, defender.pos)) // more acc
        return attacker.accRoll(damage) * bonus > defender.dexRoll(damage)
    }

    protected fun processWandDamage(damage: Damage): Boolean {
        val hero = damage.from as Hero
        val enemy = damage.to as Char

        val visibleFight = Dungeon.visible[hero.pos] || Dungeon.visible[enemy.pos]

        procGivenDamage(damage)

        // hit check
        val hit = checkHit(damage)

        if (!hit) {
            onMissed(damage)
            hero.sprite.showStatus(CharSprite.NEUTRAL, M.L(Hero::class.java, "lost_verb"))
            return false
        }

        onHit(damage)

        // directly goto damage taken, check resistance 
        enemy.takeDamage(damage)

        // fx
        if (damage.isFeatured(Damage.Feature.CRITICAL) && visibleFight) {
            Sample.INSTANCE.play(Assets.SND_BLAST)
            Splash.at(enemy.sprite.center(), PointF.angle(hero.sprite.center(), enemy.sprite.center()),
                    PI.toFloat() / 3f, particleColor(), Random.NormalIntRange(12, 20))
        }

        if (!enemy.isAlive) onKilled(damage)

        return true
    }

    // moments
    //note: the wand damage is never bounded, 
    private fun procGivenDamage(damage: Damage) {
        if (damage.value == 0) return // no damage wand.

        val hero = curUser
        var af = hero.arcaneFactor()

        if (isMissile) {
            val ross = Ring.getBonus(hero, RingOfSharpshooting.Aim::class.java)
            if (ross != 0) {
                af *= 2.5f - 1.5f * 0.9f.pow(ross)
            }
        }

        val roa = Ring.getBonus(hero, RingOfArcane.Arcane::class.java)
        if (roa > 0 && Random.Float() < 0.8f * (1f - 0.925f.pow(roa))) {
            af *= 1.5f // not bounded
            damage.addFeature(Damage.Feature.CRITICAL)
        }

        damage.value = round(damage.value * af).toInt()

        hero.buff(Preheated::class.java)?.affectWandDamage(damage) // detach here.

        hero.heroPerk.get(ArcaneCrit::class.java)?.affectDamage(hero, damage)
        hero.heroPerk.get(CloseZap::class.java)?.procDamage(damage)
    }

    protected open fun onHit(damage: Damage) {
        if (isMissile) curUser.heroPerk.get(WandPiercing::class.java)?.onHit(damage.to as Char)
    }

    //todo: may use Hero::onEvasion.
    protected open fun onMissed(damage: Damage) {
        val hero = curUser
        hero.heroPerk.get(PreheatedZap::class.java)?.let {
            Buff.prolong(hero, Preheated::class.java, 5f)
        }
    }

    protected open fun onKilled(damage: Damage) {
        val hero = curUser
        hero.heroPerk.get(ManaDrine::class.java)?.affect(hero)
    }

    // simple compatible for none damage wand
    abstract class NoDamage(isMissile: Boolean) : DamageWand(isMissile) {
        final override fun min(lvl: Int): Int = 0
        final override fun max(lvl: Int): Int = 0

        override fun statsDesc(): String = M.L(this, "stats_desc")
    }
}
