package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.KRandom
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

private const val STR_LEVEL = "level"

abstract class Perk(val maxLevel: Int = 1, var level: Int = 1) : Bundlable {
    open fun description(): String = M.L(this, "desc")

    open fun image(): Int = PerkImageSheet.NONE

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
        class LuckFromAuthor : Perk(1000) {
            override fun image(): Int = PerkImageSheet.LUCK_FROM_ME
        }

        fun RandomPositive(hero: Hero): Perk = KRandom.Chances(
                positives.filter { it.value > 0f && it.key.isAcquireAllowed(hero) })
                ?: LuckFromAuthor()

        fun RandomPositives(hero: Hero, count: Int): List<Perk> {
            val avialables = positives.filter { it.value > 0f && it.key.isAcquireAllowed(hero) }
            return if (avialables.size > count)
                KRandom.Chances(avialables, count)
            else {
                val perks = ArrayList<Perk>()
                perks.addAll(avialables.keys)
                for (i in 1..(count - perks.size)) perks.add(LuckFromAuthor())
                perks
            }
        }

        fun RandomNegative(hero: Hero): Perk {
            TODO()
        }

        // I mean, just random, give me a perk!
        fun Random(hero: Hero): Perk {
            TODO()
        }

        private val positives = mapOf<Perk, Float>(
                LuckFromAuthor() to 0.01f, // hhh
                Drunkard() to 1f,
                GoodAppetite() to 1f,
                StrongConstitution() to 1f,
                Keen() to 1f,
                WandPerception() to 1f,
                NightVision() to 0.75f,
                Telepath() to 1f,
                Fearless() to 1f,
                Assassin() to 0f,
                IntendedTransportation() to 0f,
                Optimistic() to 1f,
                Discount() to 1f,
                GreedyMidas() to 1f,
                VampiricCrit() to 0.75f,
                PureCrit() to 1f,
                ExtraCritProbability() to 1f,
                HardCrit() to 1f,
                LowHealthRegeneration() to 1f,
                LowHealthDexterous() to 1f,
                // ExtraDexterous() to 1f,
                ExtraEvasion() to 1f,
                CounterStrike() to 1f,
                ExtraDexterousGrowth() to 1f,
                ExtraPerkChoice() to 1f,
                BrewEnhancedPotion() to 1f,
                Knowledgeable() to 0.75f, // this is not ready
                EfficientSearch() to 1f,
                ExtraStrengthPower() to 1f,
                FastRegeneration() to 1f,
                EfficientPotionOfHealing() to 1.25f,
                PressureIsPower() to 1f,
                PressureRelieve() to 1f,
                WandCharger() to 1f,
                WandArcane() to 1f,
                QuickZap() to 1f,
                StealthCaster() to 1f,
                ArcaneCrit() to 1.2f,
                WandPiercing() to 1f,
                ExplodeBrokenShot() to 1f,
                RangedShot() to 1f,
                FinishingShot() to 1f,
                ExtraStrength() to 0.75f,
                ExtraRuneRegularly() to 0.8f,
                BaredAngry() to 0.8f,
                BaredSwiftness() to 1f,
                ExtraMagicalResistance() to 1f,
                QuickLearner() to 1f
//                LevelPerception() to 1f
        )
    }
}