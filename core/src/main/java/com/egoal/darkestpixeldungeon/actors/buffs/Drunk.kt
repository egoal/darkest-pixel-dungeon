package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Random
import kotlin.math.round

class Drunk : FlavourBuff() {
    init {
        type = buffType.NEUTRAL
    }

    override fun icon(): Int = BuffIndicator.DRUNK

    override fun toString(): String = Messages.get(this, "name")

    override fun desc(): String = Messages.get(this, "desc", dispTurns())

    fun procGivenDamage(dmg: Damage) {
        dmg.value = round(dmg.value * 1.2f).toInt()
    }

    fun attackProc(dmg: Damage) {
        val hero = dmg.from as Hero

        if (Random.Float() < 0.1f)
            hero.say(M.L(this, "boxing_${Random.Int(5)}"))

        if (Random.Float() < 0.12f) {
            dmg.value += dmg.value / 4
            dmg.addFeature(Damage.Feature.CRITICAL)
            prolong(hero, MustDodge::class.java, 2f)

            if (Random.Float() < 0.35f) hero.recoverSanity(Random.Float(5f))

            hero.sprite.showStatus(CharSprite.WARNING, M.L(this, "boxing"))
        }
    }

    fun onEvade(dmg: Damage) {
        if (Random.Float() < 0.15f)
            (target as Hero).say(M.L(this, "evade_${Random.Int(5)}"))
    }

    fun procTakenDamage(dmg: Damage) {
        if (dmg.type == Damage.Type.MENTAL) dmg.value /= 2
        else {
            dmg.value -= (dmg.value / 5 + Random.NormalIntRange(0, 4))
            if (dmg.value < 0) dmg.value = 0
        }
    }

    companion object {
        private const val BASE_DURATION = 30f

        fun duration(ch: Char): Float {
            val r = ch.buff(RingOfResistance.Resistance::class.java)
            return if (r == null) BASE_DURATION else r.durationFactor() * BASE_DURATION
        }

        fun Affect(hero: Hero) {
            Affect(hero, duration(hero))
        }

        fun Affect(hero: Hero, dur: Float) {
            prolong(hero, Vertigo::class.java, dur)
            prolong(hero, Drunk::class.java, dur)
        }
    }
}