package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.KRandom
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Lucky
import com.egoal.darkestpixeldungeon.actors.buffs.Relieve
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import kotlin.math.pow

private const val STR_LEVEL = "level"

abstract class Perk(val maxLevel: Int = 1, var level: Int = 1) : Bundlable {
    enum class Tag {
        Bare,
        Crit,
        Melee,
        Ranged,
        Wand,
        Evade,
        Viability
    }

    private val tags = HashSet<Tag>()

    protected fun addTags(vararg thetags: Tag) {
        tags.addAll(thetags)
    }

    open fun description(): String = M.L(this, "desc")

    open fun image(): Int = PerkImageSheet.NONE

    open fun upgradable(): Boolean = level < maxLevel && canBeGain(Dungeon.hero)

    open fun upgrade() {
        level += 1
    }

    open fun downgrade() {
        level -= 1
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

    //todo: refactor 
    abstract class Additional(maxLevel: Int = 1, level: Int = 1) : Perk(maxLevel, level) {
        final override fun upgrade() {
            onLose()
            super.upgrade()
            onGain()
        }

        final override fun downgrade() {
            onLose()
            super.downgrade()
            onGain()
        }
    }

    companion object {
        // in case run out of perks
        class LuckFromAuthor : Perk(1000) {
            override fun image(): Int = PerkImageSheet.LUCK_FROM_ME

            override fun onGain() {
                super.onGain()
                Buff.affect(Dungeon.hero, Relieve::class.java).prolong(100f) // 100* 0.3f
                Buff.prolong(Dungeon.hero, Lucky::class.java, 100f)
            }
        }

        fun RandomPositive(hero: Hero): Perk = KRandom.Chances(positives.filter {
            it.value > 0f && it.key.isAcquireAllowed(hero)
        })?.javaClass?.newInstance() ?: LuckFromAuthor()

        fun RandomPositives(hero: Hero, count: Int): List<Perk> {
            return RandomPositiveFor(hero, count)
//            val availables = positives.filter { it.value > 0f && it.key.isAcquireAllowed(hero) }
//            return if (availables.size > count)
//                KRandom.Chances(availables, count).map { it.javaClass.newInstance() }
//            else {
//                val perks = availables.keys.map { it.javaClass.newInstance() }.toMutableList()
//
//                for (i in 1..(count - perks.size)) perks.add(LuckFromAuthor())
//                perks
//            }
        }

        private fun RandomPositiveFor(hero: Hero, count: Int): List<Perk> {
            val availables = positives.filter { it.value > 0f && it.key.isAcquireAllowed(hero) }.toMutableMap()
            if (availables.size <= count) {
                val perks = availables.keys.map { it.javaClass.newInstance() }.toMutableList()
                for (i in 1..(count - perks.size)) perks.add(LuckFromAuthor())
                return perks
            }

            // alter probs
            val countmap = HashMap<Tag, Int>()
            for (t in Tag.values()) countmap[t] = 0
            for (p in hero.heroPerk.perks)
                for (t in p.tags)
                    countmap[t] = countmap[t]!! + 1

            for ((perk, prob) in availables) {
                val cnt = perk.tags.sumOf { countmap[it]!! }
                val fix = 3f - 2f * .85f.pow(cnt)
                availables[perk] = prob * fix
            }
            return KRandom.Chances(availables, count).map { it.javaClass.newInstance() }
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
                WandPerception() to 0f,
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
                LowWeightDexterous() to 1f,
                // ExtraDexterous() to 1f,
                ExtraEvasion() to 1f,
                CounterStrike() to 1f,
                ExtraDexterousGrowth() to 1f,
                EvasionTenacity() to 1f,
                Blur() to 1f,
                ExtraPerkChoice() to 1f,
                BrewEnhancedPotion() to 1f,
                Knowledgeable() to 0.75f, // this is not ready
                EfficientSearch() to 1f,
                ExtraStrengthPower() to 1f,
                FastRegeneration() to 1f,
                EfficientPotionOfHealing() to 0.25f,
                PressureIsPower() to 1f,
                PressureRelieve() to 1f,
                WandCharger() to 1f,
                WandArcane() to 1f,
                QuickZap() to 1f,
                StealthCaster() to 1f,
                ArcaneCrit() to 1.2f,
                WandPiercing() to 1.25f,
                CloseZap() to 1f,
                PreheatedZap() to 1f,
                ManaDrine() to 1f,
                ExplodeBrokenShot() to 1f,
                RangedShot() to 1f,
                FinishingShot() to 1f,
                ExtraStrength() to 0.75f,
                ExtraRuneRegularly() to 0.8f,
                BaredAngry() to 1f,
                BaredSwiftness() to 1f,
                BaredStealth() to 1f,
                BaredRelieve() to 1f,
                ExtraMagicalResistance() to 1f,
                QuickLearner() to 1f,
//                LevelPerception() to 1f
                Maniac() to 1f,
                PolearmMaster() to 0.8f,
                Ease() to 1f,
                EnchantmentExtraDamage() to 1f,
                FastMoveOnKilling() to 1f,
        )
    }
}