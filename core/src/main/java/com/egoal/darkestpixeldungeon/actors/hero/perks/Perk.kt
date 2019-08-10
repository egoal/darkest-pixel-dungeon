package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.KRandom
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

private const val STR_LEVEL = "level"

abstract class Perk(val maxLevel: Int = 1, var level: Int = 1) : Bundlable {
    open fun description(): String = M.L(this, "desc")

    open fun image(): Int = 0

    open fun upgradable(): Boolean = level < maxLevel

    open fun upgrade() {
        assert(upgradable())
        level += 1
    }

    fun isAcquireAllowed(hero: Hero): Boolean {
        if (!canBeGain(hero)) return false

        val p = hero.heroPerk.get(javaClass)
        return p == null || p.upgradable()
    }

    open fun onGain() {}

    open fun onLose() {}

    protected open fun canBeGain(hero: Hero): Boolean = true

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(STR_LEVEL, level)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        level = bundle.getInt(STR_LEVEL)
    }

    companion object {
        // in case run out of perks
        class LuckFromAuthor : Perk(1000)

        fun RandomPositive(hero: Hero): Perk = KRandom.Chances(
                positives.filter { it.value > 0 && it.key.isAcquireAllowed(hero) })
                ?: LuckFromAuthor()

        fun RandomNegative(hero: Hero): Perk {
            TODO()
        }

        private val positives = mapOf<Perk, Float>(
                GoodAppetite() to 0f,
                StrongConstitution() to 1f,
                Keen() to 1f,
                WandPerception() to 1f,
                NightVision() to 1f,
                Telepath() to 1f,
                Fearless() to 1f,
                Assassin() to 0f,
                IntendedTransportation() to 0f,
                Optimistic() to 1f,
                Discount() to 1f,
                VampiricCrit() to 1f,
                PureCrit() to 1f,
                ExtraCritProbability() to 1f,
                HardCrit() to 1f,
                LowHealthRegeneration() to 1f,
                LowHealthDexterous() to 1f,
                ExtraPerkChoice() to 1f
        )
    }
}