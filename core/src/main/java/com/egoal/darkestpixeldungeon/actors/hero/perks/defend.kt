package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.pow
import kotlin.math.round

class Blur : Perk() {
    init {
        addTags(Tag.Evade, Tag.Viability)
    }

    override fun image(): Int = PerkImageSheet.BLUR

    override fun onGain() {
        Buff.affect(Dungeon.hero, Counter::class.java)
    }

    override fun onLose() {
        Buff.detach(Dungeon.hero, Counter::class.java)
    }

    //todo:
    class Counter : Buff() {
        private val moments = floatArrayOf(-1f, -1f, -1f)

        fun onEvade() {
            if (Dungeon.hero.buff(Tenacity::class.java) != null) return
            val i = moments.withIndex().minByOrNull { it.value }!!.index
            moments[i] = 5f

            if (moments.all { it > 0f }) {
                moments.fill(-1f)
                prolong(Dungeon.hero, Tenacity::class.java, 20f)
            }
        }

        override fun act(): Boolean {
            for (i in moments.indices) if (moments[i] > 0f) moments[i] -= TICK

            spend(Actor.TICK)
            return true
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put("moments", moments)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            val mom = bundle.getFloatArray("moments")
            for (i in moments.indices) moments[i] = mom[i]
        }
    }
}

class CounterStrike : Perk() {
    init {
        addTags(Tag.Evade, Tag.Melee)
    }
    override fun image(): Int = PerkImageSheet.COUNTER_STRIKE

    fun procEvasionDamage(dmg: Damage) {
        if (dmg.type == Damage.Type.NORMAL && !dmg.isFeatured(Damage.Feature.RANGED)) {
            Buff.affect(dmg.to as Hero, SeeThrough::class.java, 1.1f).enemyid = (dmg.from as Actor).id()
        }
    }
}

class EvasionTenacity : Perk(3) {
    init {
        addTags(Tag.Evade, Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.EVASION_TENACITY

    fun procEvasionDamage(dmg: Damage) {
        val hero = dmg.to as Hero
        if (hero.SHLD < hero.HT) hero.SHLD += level * 2 + 1
    }

    override fun onGain() {
        Dungeon.hero.MSHLD += 3
    }

    override fun onLose() {
        Dungeon.hero.MSHLD -= 3
    }
}

class ExtraDexterous : Perk.Additional(5) {
    init {
        addTags(Tag.Evade)
    }
    override fun image(): Int = PerkImageSheet.DEX_EXTRA

    override fun onGain() {
        Dungeon.hero.defSkill += extraDef()
    }

    override fun onLose() {
        Dungeon.hero.defSkill -= extraDef()
    }

    private fun extraDef(): Int = 3 * level
}

class ExtraDexterousGrowth : Perk(5) {
    init {
        addTags(Tag.Evade)
    }
    override fun image(): Int = PerkImageSheet.DEX_GROWTH

    private fun extraDex(): Float = level * 0.5f

    fun upgradeHero(hero: Hero) {
        hero.defSkill += extraDex()
    }
}

class ExtraEvasion : Perk(4) {
    init {
        addTags(Tag.Evade)
    }
    override fun image(): Int = PerkImageSheet.DEX_EXTRA

    fun prob(): Float = 0.075f * level

    override fun description(): String = M.L(this, "desc", (prob() * 100).toInt())
}

class ExtraMagicalResistance : Perk(3) {
    init {
        addTags(Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.MAGICAL_RESISTANCE

    fun ratio(): Float = 0.05f + level * 0.15f

    override fun description(): String = M.L(this, "desc", (ratio() * 100).toInt())
}

class FastRegeneration : Perk.Additional(5) {
    init {
        addTags(Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.FASTER_REG

    override fun onGain() {
        Dungeon.hero.regeneration += extraReg()
    }

    override fun onLose() {
        Dungeon.hero.regeneration -= extraReg()
    }

    private fun extraReg(): Float = 0.25f * level
}

class Fearless : Perk() {
    override fun image(): Int = PerkImageSheet.FEARLESS

    override fun canBeGain(hero: Hero): Boolean {
        return hero.heroClass != HeroClass.WARRIOR
    }
}

class LowHealthDexterous : Perk(3) {
    init {
        addTags(Tag.Evade, Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.LOW_HEALTH_DEX

    fun extraEvasion(hero: Hero): Float {
        if (hero.HP > hero.HT * 0.3f) return 0f

        return 0.2f * 1.5f.pow(level)
    }
}

class LowHealthRegeneration : Perk(5) {
    init {
        addTags(Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.LOW_HEALTH_REG

    //    fun extraRegeneration(hero: Hero): Float {
//        if (hero.HP > hero.HT * 0.3f) return 0f
//
//        return 1f * level // todo:
//    }
    fun onDamageTaken(hero: Hero) {
        if (hero.HP <= hero.HT * trigger())
            Buff.affect(hero, FastRecovery::class.java).apply {
                limit = limit()
                speed = speed()
            }
    }

    private fun trigger() = 0.2f + level * 0.05f
    private fun speed() = level * 1
    private fun limit() = 0.4f + level * 0.1f
}

class LowWeightDexterous : Perk(1) {
    init {
        addTags(Tag.Evade)
    }
    override fun image(): Int = PerkImageSheet.LOW_WEIGHT_DEX
}

class Optimistic : Perk(2) {
    init {
        addTags(Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.OPTIMISTIC

    fun resistChance(): Float = 0.1f * level // 0.1-> 0.2

    override fun canBeGain(hero: Hero): Boolean = hero.heroClass != HeroClass.SORCERESS
}

class PressureRelieve : Perk(2) {
    init {
        addTags(Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.PRESSURE_RELIEVE

    fun affectDamage(dmg: Damage, hero: Hero, pressure: Pressure) {
        if (dmg.type != Damage.Type.NORMAL) return

        val p = pressure.pressure / Pressure.MAX_PRESSURE
        if (p < 0.2f) return

        // 1: -0.05, 2: +0.05
        val r = 1f - (p - 0.15f + 0.1f * level) * (p - 0.15f + 0.1f * level) * 0.8f
        dmg.value = round(dmg.value * r).toInt()
    }
}

class BaredRelieve : Perk() {
    init {
        addTags(Tag.Bare, Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.BARED_RELIEVE

    fun onDamageTaken(hero: Hero, damage: Damage) {
        if (hero.belongings.armor == null && !damage.isFeatured(Damage.Feature.CRITICAL) &&
                damage.value > 1 && Random.Int(4) == 0) {
            hero.recoverSanity(Random.Float(1f, 1f + (1f - hero.HP.toFloat() / hero.HT) * 4f)) // 1 -> 5
            hero.say(M.L(this, "line_${Random.Int(3)}"))
        }
    }
}

class StrongConstitution : Perk(5) {
    init {
        addTags(Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.STRONG_COSTITUION

    private fun extraHT(): Int = 2 * level

    fun upgradeHero(hero: Hero) {
        val dht = extraHT()
        hero.HT += dht
        hero.HP += dht
    }
}
