package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Challenge
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.actors.hero.perks.Optimistic
import com.egoal.darkestpixeldungeon.actors.hero.perks.PressureIsPower
import com.egoal.darkestpixeldungeon.items.artifacts.GoddessRadiance
import com.egoal.darkestpixeldungeon.items.helmets.Mantilla
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.Random
import kotlin.math.exp
import kotlin.math.min

class Pressure : Buff(), Hero.Doom {

    enum class Level(val title: String) {
        CONFIDENT("confident"), NORMAL("normal"), NERVOUS("nervous"), COLLAPSE
        ("collapse")
    }

    private var collapseDuration = 0
    var pressure = 0f
    var level = Level.CONFIDENT
        private set

    private fun updateLevel() {
        val newLevel = when {
            pressure <= LVL_CONFIDENT -> Level.CONFIDENT
            pressure <= LVL_NORMAL -> Level.NORMAL
            pressure < LVL_NERVOUS -> Level.NERVOUS
            else -> Level.COLLAPSE
        }

        if (level === newLevel) return

        // level changed, 
        level = newLevel
        BuffIndicator.refreshHero()

        // reset collapse
        if (level != Level.COLLAPSE)
            collapseDuration = 0

        when (level) {
            Level.CONFIDENT -> GLog.p(Messages.get(this, "reach_" + level.title))
            Level.NORMAL -> {
            }
            Level.NERVOUS -> GLog.w(Messages.get(this, "reach_" + level.title))
            Level.COLLAPSE -> GLog.n(Messages.get(this, "reach_" + level.title))
        }

    }

    override fun icon(): Int = when (level) {
        Level.CONFIDENT -> BuffIndicator.CONFIDENT
        Level.NORMAL -> BuffIndicator.NONE
        Level.NERVOUS -> BuffIndicator.NERVOUS
        Level.COLLAPSE -> BuffIndicator.COLLAPSE
    }

    override fun toString(): String = Messages.get(this, level.title)

    override fun desc(): String = Messages.get(this, "desc_intro_" + level.title) + Messages.get(this, "desc")

    // pressure
    private fun ignoreChance(): Float {
        val hero = target as Hero

        val p1 = hero.heroPerk.get(Optimistic::class.java)?.resistChance() ?: 0f
        val p2 = hero.buff(GoddessRadiance.Recharge::class.java)?.evadeRatio() ?: 0f
        val p3 = if (hero.belongings.helmet is Mantilla && !hero.belongings.helmet!!.cursed) 0.1f else 0f
        val p4 = if (hero.challenges.contains(Challenge.PathOfAsceticism)) .1f else 0f

        return GameMath.ProbabilityPlus(p1, p2, p3, p4)
    }

    fun upPressure(p: Float): Float {
        val r = min(p, LVL_NERVOUS - pressure)
        if (r >= 1f && Random.Float() < ignoreChance()) {
            target.sprite.showStatus(CharSprite.DEFAULT, M.L(Hero::class.java, "mental_resist"))
            return 0f
        }

        pressure += r
        updateLevel()
        return r
    }

    fun downPressure(p: Float): Float {
        val r = min(p, pressure)
        pressure -= r
        updateLevel()
        return r
    }

    override fun act(): Boolean {
        spend(procStep())

        if (target.isAlive) {
            if (Dungeon.depth > 0 && Random.Int(10) == 0) {
                //^ up pressure, not in the village
                upPressure(procValue())
            }

            // mental damage
            if (level === Level.COLLAPSE) {
                val ed = exp(collapseDuration++ - 4f)
                target.takeDamage(Damage((target.HT.toFloat() * ed / (ed + 1f)).toInt(), this, target)
                        .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE))

                if (target === Dungeon.hero && target.isAlive) {
                    Dungeon.hero.interrupt()
                    GLog.n(Messages.get(this, "onhurt"))

                    val n = Random.Int(6)
                    if (n < 2) (target as Hero).sayShort(HeroLines.MY_RETRIBUTION)
                    else (target as Hero).sayShort(HeroLines.HEADACHE)
                }
            }
        } else {
            diactivate()
        }

        return true
    }

    private fun procValue(): Float {
        if (Dungeon.hero.challenges.contains(Challenge.LowPressure)) return 0f

        var value = if (Dungeon.level.locked) 2f else 1f
        value += Random.Float(Dungeon.depth / 5f)
        if (Statistics.Clock.state == Statistics.ClockTime.State.MidNight &&
                !com.egoal.darkestpixeldungeon.levels.Level.lighted[target.pos])
            value *= 1.5f

        return value
    }

    private fun procStep(): Float = if (target === Dungeon.hero) 10f * (target as Hero).mentalFactor() else 10f

    // affect damage
    fun procGivenDamage(dmg: Damage): Damage {
        when (level) {
            Level.CONFIDENT -> {
                // crit chance move to hero
                if (!dmg.isFeatured(Damage.Feature.CRITICAL)) dmg.value = dmg.value * 11 / 10
            }
            Level.NORMAL -> {
            }
            Level.NERVOUS -> {
            }
            Level.COLLAPSE -> dmg.value /= 2
        }

        (target as Hero).heroPerk.get(PressureIsPower::class.java)?.affectDamage(dmg, target as Hero, this)

        return dmg
    }

    fun accuracyFactor(): Float = when (level) {
        Level.CONFIDENT -> 1.15f
        Level.NORMAL -> 1f
        Level.NERVOUS -> 0.8f
        Level.COLLAPSE -> 0.2f
    }

    fun evasionFactor(): Float = when (level) {
        Level.CONFIDENT -> 1f
        Level.NORMAL -> 1f
        Level.NERVOUS -> 0.8f
        Level.COLLAPSE -> 0f
    }

    fun chargeFactor(): Float = when (level) {
        Level.CONFIDENT, Level.NORMAL -> 1f
        Level.NERVOUS -> 0.7f
        Level.COLLAPSE -> 0f
    }

    //
    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(PRESSURE, pressure)
        bundle.put(COLLAPSE_DURATION, collapseDuration)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        pressure = bundle.getFloat(PRESSURE)
        collapseDuration = bundle.getInt(COLLAPSE_DURATION)
        updateLevel()
    }

    companion object {
        private const val LVL_CONFIDENT = 30f
        private const val LVL_NORMAL = 70f
        private const val LVL_NERVOUS = 100f
        private const val LVL_COLLAPSE = 110f // actually no use.
        const val MAX_PRESSURE = 100f

        private const val PRESSURE = "pressure"
        private const val COLLAPSE_DURATION = "collapse_duration"

        fun HeroPressure() = if (!Dungeon.isHeroNull && Dungeon.hero.isAlive)
            Dungeon.hero.buff(Pressure::class.java)!!.pressure else 0f
    }

    // doom
    override fun onDeath() {
        Dungeon.fail(javaClass)
        GLog.n(Messages.get(this, "ondeath"))
    }
}
