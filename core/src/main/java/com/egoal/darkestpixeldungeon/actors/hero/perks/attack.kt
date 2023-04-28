package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.buffs.TimeDilation
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class Assassin : Perk() {
    override fun image(): Int = PerkImageSheet.ASSASSIN
}

class BaredAngry : Perk() {
    init {
        addTags(Tag.Bare, Tag.Crit)
    }
    override fun image(): Int = PerkImageSheet.BARED_ANGRY

    fun procGivenDamage(dmg: Damage, hero: Hero) {
        if (noArmor(hero)) dmg.value += dmg.value / 4
    }

    fun speedFactor(hero: Hero): Float = if (noArmor(hero)) 0.7f else 1f

    private fun noArmor(hero: Hero): Boolean = hero.belongings.armor == null
}

class BaredSwiftness : Perk() {
    init {
        addTags(Tag.Bare, Tag.Evade)
    }
    override fun image(): Int = PerkImageSheet.BARED_SWIFTNESS

    fun speedFactor(hero: Hero): Float = if (noArmor(hero)) 1.2f else 1f

    fun evasionProb(hero: Hero): Float = if (noArmor(hero)) 0.12f else 0f

    private fun noArmor(hero: Hero): Boolean = hero.belongings.armor == null
}

class BaredStealth: Perk(){
    init {
        addTags(Tag.Bare, Tag.Evade, Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.BARED_STEALTH
}

class ExplodeBrokenShot : Perk() {
    init {
        addTags(Tag.Ranged)
    }
    override fun image(): Int = PerkImageSheet.SHOT_EXPLODE
}

class ExtraCritProbability : Perk.Additional(5) {
    init {
        addTags(Tag.Melee, Tag.Crit)
    }
    override fun image(): Int = PerkImageSheet.CRIT_PROB

    private fun extraProb(): Float = 0.01f + 0.05f * level

    override fun onGain() {
        Dungeon.hero.criticalChance += extraProb()
    }

    override fun onLose() {
        Dungeon.hero.criticalChance -= extraProb()
    }
}

class ExtraStrengthPower : Perk(3) {
    init {
        addTags(Tag.Melee)
    }
    override fun image(): Int = PerkImageSheet.STRENGTH_POWER

    fun affectDamage(dmg: Damage, exStr: Int) {
        for (i in 1..level)
            dmg.value += com.watabou.utils.Random.Int(1, exStr)
    }
}

class FinishingShot : Perk() {
    init {
        addTags(Tag.Ranged)
    }
    override fun image(): Int = PerkImageSheet.FINISHING_SHOT

    fun onKilledChar(hero: Hero, ch: Char, weapon: MissileWeapon) {
        Buff.prolong(hero, TimeDilation::class.java, 0.5f + weapon.DLY / 2f)
    }
}

class HardCrit : Perk(5) {
    init {
        addTags(Tag.Crit)
    }
    override fun image(): Int = PerkImageSheet.CRIT_HARD

    fun procCrit(dmg: Damage) {
        dmg.value += round(dmg.value * extraCritRatio()).toInt()
    }

    private fun extraCritRatio(): Float = 0.05f + 0.25f * level
}

class Maniac : Perk() {
    init {
        addTags(Tag.Melee)
    }
    override fun image(): Int = PerkImageSheet.MANIAC

    fun speedFactor(hero: Hero): Float {
        val n = min(hero.visibleEnemies(), 8)
        return if (n <= 1) 1f else (0.5f + 0.5f * 0.8f.pow(n)) // no bonus when 1 v 1
    }
}

class PolearmMaster : Perk(2) {
    override fun image(): Int = PerkImageSheet.POLEARM

    fun proc(damage: Damage, weapon: MeleeWeapon) {
        val attacker = damage.from as Char
        val defender = damage.to as Char
        val ratio = 0.1f + weapon.tier * 0.05f * level // 0.15 ~ 0.35 => 0.2 ~ 0.6


        if (Dungeon.level.adjacent(defender.pos, attacker.pos) && com.watabou.utils.Random.Float() < ratio) {
            val duration = 1f + weapon.tier + weapon.DLY
            Buff.prolong(defender, Cripple::class.java, duration)

            // knock back
            val opp = defender.pos + (defender.pos - attacker.pos)
            val shot = Ballistica(defender.pos, opp, Ballistica.MAGIC_BOLT)

            WandOfBlastWave.throwChar(defender, shot, 1)
        } else if (com.watabou.utils.Random.Float() < ratio) {
            val duration = 1f + weapon.tier + weapon.DLY
            Buff.prolong(defender, Cripple::class.java, duration)
        }
    }

    fun accFactor() = 1f + 0.1f * level

    // exile wont get this, while other class can never reach level 2
    // note: i havnt fix the perk upgrade bug, so level 2 may still be possible
    override fun canBeGain(hero: Hero): Boolean = hero.heroClass != HeroClass.EXILE
            && hero.heroPerk.get(PolearmMaster::class.java) == null
}

class PressureIsPower : Perk() {
    init {
        addTags(Tag.Melee)
    }
    override fun image(): Int = PerkImageSheet.PRESSURE_POWER

    fun affectDamage(dmg: Damage, hero: Hero, pressure: Pressure) {
        val p = pressure.pressure / Pressure.MAX_PRESSURE
        if (p < 0.2f) return

        val r = p / 2f + p * p / 2f + 0.8f
        dmg.value = round(dmg.value * r).toInt()
    }
}

class PureCrit : Perk() {
    init {
        addTags(Tag.Crit)
    }
    override fun image(): Int = PerkImageSheet.CRIT_PURE

    fun procCrit(dmg: Damage) {
        dmg.addFeature(Damage.Feature.PURE)
    }
}

class RangedShot : Perk() {
    init {
        addTags(Tag.Ranged)
    }
    override fun image(): Int = PerkImageSheet.RANGED_SHOT

    fun affectDamage(dmg: Damage) {
        val dis = Dungeon.level.distance((dmg.from as Char).pos, (dmg.to as Char).pos)
        if (dis > 1) {
            dmg.value = round(dmg.value * (3f - 1.8f * 0.9f.pow(dis - 2))).toInt()
        }
    }
}

class VampiricCrit : Perk(5) {
    init {
        addTags(Tag.Crit, Tag.Melee, Tag.Viability)
    }
    override fun image(): Int = PerkImageSheet.CRIT_VAMP

    fun procCrit(dmg: Damage) {
        assert(dmg.isFeatured(Damage.Feature.CRITICAL))

        val hero = dmg.from as Hero
        val eff = min(hero.HT - hero.HP, ((0.1f + 0.15f * level) * dmg.value).toInt())

        if (eff > 0)
            hero.recoverHP(eff, this)
    }
}