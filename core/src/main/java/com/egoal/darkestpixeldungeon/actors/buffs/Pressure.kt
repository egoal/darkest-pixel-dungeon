package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
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
    private var level = Level.CONFIDENT

    fun getLevel(): Level = level

    private fun updateLevel() {
        val newLevel = when {
            pressure <= LVL_CONFIDENT -> Level.CONFIDENT
            pressure <= LVL_NORMAL -> Level.NORMAL
            pressure <= LVL_NERVOUS -> Level.NERVOUS
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
    fun upPressure(p: Float): Float {
        val r = min(p, LVL_NERVOUS - pressure)
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

        if (Dungeon.level.locked) return true


        if (target.isAlive) {
            if (Dungeon.depth > 0 && Random.Int(10) == 0) {
                //^ up pressure, not in the village
                upPressure(Random.Int(1, 2 + Dungeon.depth / 4).toFloat())
            }

            // mental damage
            if (level === Level.COLLAPSE) {
                val ed = exp(collapseDuration++ - 4f)
                target.takeDamage(Damage((target.HT.toFloat() * ed / (ed + 1f)).toInt(), this, target)
                        .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.PURE))

                if (target === Dungeon.hero && target.isAlive) {
                    Dungeon.hero.interrupt()
                    GLog.n(Messages.get(this, "onhurt"))
                }
            }
        } else {
            diactivate()
        }

        return true
    }

    private fun procStep(): Float = if (target === Dungeon.hero) 10f * (target as Hero).mentalFactor() else 10f

    // affect damage
    fun procGivenDamage(dmg: Damage): Damage {
        when (level) {
            Level.CONFIDENT -> {
                if (!dmg.isFeatured(Damage.Feature.CRITICAL) && Random.Int(10) == 0) {
                    dmg.addFeature(Damage.Feature.CRITICAL)
                    dmg.value = dmg.value * 3 / 2
                } else dmg.value = dmg.value * 11 / 10
            }
            Level.NORMAL -> {
            }
            Level.NERVOUS -> {
            }
            Level.COLLAPSE -> dmg.value /= 2
        }
        return dmg
    }

    fun accuracyFactor(): Float = when (level) {
        Level.CONFIDENT -> 1.2f
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

        fun HeroPressure() = if (Dungeon.hero != null && Dungeon.hero.isAlive)
            Dungeon.hero.buff(Pressure::class.java)!!.pressure else 0f
    }

    // doom
    override fun onDeath() {
        Dungeon.fail(javaClass)
        GLog.n(Messages.get(this, "ondeath"))
    }
}