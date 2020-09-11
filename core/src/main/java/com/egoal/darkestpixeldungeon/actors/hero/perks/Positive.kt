package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRecharging
import com.egoal.darkestpixeldungeon.items.unclassified.DewVial
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.items.unclassified.Rune
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class Drunkard : Perk() {
    override fun image(): Int = PerkImageSheet.WINE_DRUNKARD

    override fun canBeGain(hero: Hero): Boolean = hero.heroClass != HeroClass.EXILE
}

class GoodAppetite : Perk() {
    override fun image(): Int = PerkImageSheet.APPETITE_GOOD

    fun onFoodEaten(hero: Hero, food: Food) {
        when (hero.heroClass) {
            HeroClass.WARRIOR -> {
                if (hero.HP < hero.HT) {
                    hero.HP = min(hero.HP + 5, hero.HT)
                    hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1)
                }
            }
            HeroClass.MAGE -> {
                Buff.affect(hero, Recharging::class.java, 4f)
                ScrollOfRecharging.charge(hero)
            }
        }
    }

    override fun canBeGain(hero: Hero): Boolean = hero.heroClass in listOf(HeroClass.WARRIOR, HeroClass.MAGE)
}

// see Hunger
class Dieting : Perk() {
    override fun image(): Int = PerkImageSheet.DIETING
}

class StrongConstitution : Perk(5) {
    override fun image(): Int = PerkImageSheet.STRONG_COSTITUION

    private fun extraHT(): Int = 2 * level

    fun upgradeHero(hero: Hero) {
        val dht = extraHT()
        hero.HT += dht
        hero.HP += dht
    }
}

class Keen : Perk(3) {
    override fun image(): Int = PerkImageSheet.KEEN

    fun baseAwareness(): Float = 0.95f - 0.1f * level
}

class WandPerception : Perk(2) {
    override fun image(): Int = PerkImageSheet.WAND_PERCEPTION

    fun onWandUsed(wand: Wand) {
        if (level == 1) wand.levelKnown = true
        else wand.identify()
    }

    override fun description(): String = M.L(this, "desc_$level")
}

class NightVision : Perk() {
    override fun image(): Int = PerkImageSheet.NIGHT_VISION
}

class Telepath : Perk() {
    override fun image(): Int = PerkImageSheet.TELEPATH
}

class Fearless : Perk() {
    override fun image(): Int = PerkImageSheet.FEARLESS

    override fun canBeGain(hero: Hero): Boolean {
        return hero.heroClass != HeroClass.WARRIOR
    }
}

class Assassin : Perk() {
    override fun image(): Int = PerkImageSheet.ASSASSIN
}

class IntendedTransportation : Perk() {
    override fun image(): Int = PerkImageSheet.TRANSPORTATION
}

class Optimistic : Perk(2) {
    override fun image(): Int = PerkImageSheet.OPTIMISTIC

    fun resistChance(): Float = 0.05f + 0.1f * level // 0.15-> 0.25

    override fun canBeGain(hero: Hero): Boolean = hero.heroClass != HeroClass.SORCERESS
}

// note: this perk can be negative level, i havnt abstract this, but it works for now.
class Discount : Perk(2) {
    override fun image(): Int = if (level > 0) PerkImageSheet.DISCOUNT else PerkImageSheet.DISCOUNT_NEG

    fun buyPrice(item: Item): Int = (item.sellPrice() * ratio()).toInt()

    fun ratio(): Float = 1f - 0.25f * level
}

class GreedyMidas : Perk() {
    override fun image(): Int = PerkImageSheet.GREEDY_MIDAS

    fun procGold(gold: Gold) = gold.apply {
        val p = Random.Float()
        val q = gold.quantity() * when {
            p < 0.33f -> 4.33f
            p < 0.001f -> 10f
            else -> 1f
        }
        quantity(round(q).toInt())
    }
}

class VampiricCrit : Perk(5) {
    override fun image(): Int = PerkImageSheet.CRIT_VAMP

    fun procCrit(dmg: Damage) {
        assert(dmg.isFeatured(Damage.Feature.CRITICAL))

        val hero = dmg.from as Hero
        val eff = min(hero.HT - hero.HP, ((0.1f + 0.15f * level) * dmg.value).toInt())

        if (eff > 0)
            hero.recoverHP(eff, this)
    }
}

class PureCrit : Perk() {
    override fun image(): Int = PerkImageSheet.CRIT_PURE

    fun procCrit(dmg: Damage) {
        dmg.addFeature(Damage.Feature.PURE)
    }
}

class ExtraCritProbability : Perk.Additional(5) {
    override fun image(): Int = PerkImageSheet.CRIT_PROB

    private fun extraProb(): Float = 0.01f + 0.05f * level

    override fun onGain() {
        Dungeon.hero.criticalChance += extraProb()
    }

    override fun onLose() {
        Dungeon.hero.criticalChance -= extraProb()
    }
}

class HardCrit : Perk(5) {
    override fun image(): Int = PerkImageSheet.CRIT_HARD

    fun procCrit(dmg: Damage) {
        dmg.value += round(dmg.value * extraCritRatio()).toInt()
    }

    private fun extraCritRatio(): Float = 0.05f + 0.25f * level
}

class LowHealthDexterous : Perk(3) {
    override fun image(): Int = PerkImageSheet.LOW_HEALTH_DEX

    fun extraEvasion(hero: Hero): Float {
        if (hero.HP > hero.HT * 0.3f) return 0f

        return 0.2f * 1.5f.pow(level)
    }
}

class ExtraDexterous : Perk.Additional(5) {
    override fun image(): Int = PerkImageSheet.DEX_EXTRA

    override fun onGain() {
        Dungeon.hero.defSkill += extraDef()
    }

    override fun onLose() {
        Dungeon.hero.defSkill -= extraDef()
    }

    private fun extraDef(): Int = 3 * level
}

class ExtraEvasion : Perk(4) {
    override fun image(): Int = PerkImageSheet.DEX_EXTRA

    fun prob(): Float = 0.075f * level

    override fun description(): String = M.L(this, "desc", (prob() * 100).toInt())
}

class LowWeightDexterous : Perk(1) {
    override fun image(): Int = PerkImageSheet.LOW_WEIGHT_DEX
}

// cs go!
class CounterStrike : Perk() {
    override fun image(): Int = PerkImageSheet.COUNTER_STRIKE

    fun procEvasionDamage(dmg: Damage) {
        if (dmg.type == Damage.Type.NORMAL && !dmg.isFeatured(Damage.Feature.RANGED)) {
            Buff.affect(dmg.to as Hero, SeeThrough::class.java, 1.1f).enemyid = (dmg.from as Actor).id()
        }
    }
}

class ExtraDexterousGrowth : Perk(5) {
    override fun image(): Int = PerkImageSheet.DEX_GROWTH

    private fun extraDex(): Float = level * 0.5f

    fun upgradeHero(hero: Hero) {
        hero.defSkill += extraDex()
    }
}

class LowHealthRegeneration : Perk(5) {
    override fun image(): Int = PerkImageSheet.LOW_HEALTH_REG

    fun extraRegeneration(hero: Hero): Float {
        if (hero.HP > hero.HT * 0.3f) return 0f

        return 1f * level // todo:
    }
}

class ExtraPerkChoice : Perk() {
    override fun image(): Int = PerkImageSheet.EXTRA_CHOICE
}

class BrewEnhancedPotion : Perk() {
    override fun image(): Int = PerkImageSheet.BREW_ENHANCED
    private var nextProb = 0.3f

    fun affectPotion(p: Potion) {
        if (p.canBeReinforced()) {
            if (Random.Float() < nextProb) {
                p.reinforce()
                nextProb = 0.3f
            } else nextProb += 0.1f
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("nextprob", nextProb)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        nextProb = bundle.getFloat("nextprob")
    }
}

class Knowledgeable : Perk(3) {
    override fun image(): Int = PerkImageSheet.KNOWLEDGE

    fun affectItem(item: Item) {
        if (item.isIdentified) return

        if (Random.Float() < identifyChance()) {
            item.identify()
            GLog.w(M.L(this, "identity"))
        } else if (!item.cursedKnown && Random.Float() < identifyChance()) {
            item.cursedKnown = true
            GLog.w(M.L(this, "know-curse"))
        }
    }

    private fun identifyChance(): Float = 0.05f + 0.2f * level
}

class EfficientSearch : Perk() {
    override fun image(): Int = PerkImageSheet.SEARCH_EFFICIENT
}

class ExtraStrengthPower : Perk(3) {
    override fun image(): Int = PerkImageSheet.STRENGTH_POWER

    fun affectDamage(dmg: Damage, exStr: Int) {
        for (i in 1..level)
            dmg.value += Random.Int(1, exStr)
    }
}

class FastRegeneration : Perk.Additional(5) {
    override fun image(): Int = PerkImageSheet.FASTER_REG

    override fun onGain() {
        Dungeon.hero.regeneration += extraReg()
    }

    override fun onLose() {
        Dungeon.hero.regeneration -= extraReg()
    }

    private fun extraReg(): Float = 0.15f * level
}

class EfficientPotionOfHealing : Perk() {
    override fun image(): Int = PerkImageSheet.POTION_EFF_HEALING
}

class PressureIsPower : Perk() {
    override fun image(): Int = PerkImageSheet.PRESSURE_POWER

    fun affectDamage(dmg: Damage, hero: Hero, pressure: Pressure) {
        val p = pressure.pressure / Pressure.MAX_PRESSURE
        if (p < 0.2f) return

        val r = p / 2f + p * p / 2f + 0.8f
        dmg.value = round(dmg.value * r).toInt()
    }
}

class PressureRelieve : Perk(2) {
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

class WandCharger : Perk(3) {
    override fun image(): Int = PerkImageSheet.WAND_CHARGE

    fun factor(): Float = 2f - 0.8f.pow(level)
}

class WandArcane : Perk(3) {
    override fun image(): Int = PerkImageSheet.WAND_ARCANE

    fun factor(): Float = 1f + 0.2f * level // 2f - 0.8f.pow(level)
}

class QuickZap : Perk() {
    override fun image(): Int = PerkImageSheet.WAND_QUICK_ZAP
}

class StealthCaster : Perk() {
    override fun image(): Int = PerkImageSheet.STEALTH_CASTER
}

class ArcaneCrit : Perk(5) {
    override fun image(): Int = PerkImageSheet.ARCANE_CRIT

    fun affectDamage(hero: Hero, dmg: Damage) {
        if (!dmg.isFeatured(Damage.Feature.CRITICAL) && Random.Float() < prob(hero)) {
            dmg.value = round(dmg.value * 1.75f).toInt()
            dmg.addFeature(Damage.Feature.CRITICAL)
        }
    }

    private fun prob(hero: Hero): Float {
        var prob = hero.criticalChance // base chance, not affected by rune, pressure etc.
        if (level > 1) prob += 0.09f * (level - 1)
        return prob
    }
}

class WandPiercing : Perk() {
    override fun image(): Int = PerkImageSheet.WAND_PIERCING

    fun onHit(char: Char) {
        char.magicalResistance -= 0.15f // fixme:
    }
}

class ExplodeBrokenShot : Perk() {
    override fun image(): Int = PerkImageSheet.SHOT_EXPLODE
}

class RangedShot : Perk() {
    override fun image(): Int = PerkImageSheet.RANGED_SHOT

    fun affectDamage(dmg: Damage) {
        val dis = Dungeon.level.distance((dmg.from as Char).pos, (dmg.to as Char).pos)
        if (dis > 1) {
            dmg.value = round(dmg.value * (3f - 1.8f * 0.9f.pow(dis - 2))).toInt()
        }
    }
}

class FinishingShot : Perk() {
    override fun image(): Int = PerkImageSheet.FINISHING_SHOT

    fun onKilledChar(hero: Hero, ch: Char, weapon: MissileWeapon) {
        Buff.prolong(hero, TimeDilation::class.java, 0.5f + weapon.DLY / 2f)
    }
}

class ExtraStrength : Perk.Additional(3) {
    override fun image(): Int = PerkImageSheet.STRENGTH_EXTRA

    override fun onLose() {
        super.onLose()
        Dungeon.hero.STR -= str()
    }

    override fun onGain() {
        Dungeon.hero.STR += str()
    }

    private fun str(): Int = level

    override fun description(): String = M.L(this, "desc", level)
}

abstract class TimingPerk(private val timing: Class<out Timing>,
                          maxlevel: Int = 1, level: Int = 1) : Perk(maxlevel, level) {
    override fun onGain() {
        Buff.affect(Dungeon.hero, timing)
    }

    override fun upgrade() {
        Dungeon.hero.buff(timing)!!.upgrade()
        super.upgrade()
    }

    override fun onLose() {
        Buff.detach(Dungeon.hero, timing)
    }

    abstract class Timing(protected var time: Float) : Buff() {
        fun upgrade() {}

        abstract fun trigger()

        override fun act(): Boolean {
            trigger()
            spend(time)
            return true
        }
    }
}

class ExtraRuneRegularly : TimingPerk(GainRune::class.java) {
    override fun image(): Int = PerkImageSheet.RUNE_EXTRA

    class GainRune : TimingPerk.Timing(Statistics.ClockTime.TimePerDay() / 2f) {
        override fun trigger() {
            val rune = Generator.RUNE.generate() as Rune
            GameScene.effect(Flare(7, 32f).color(rune.glowing()?.color ?: 0x66ff66, true).show(
                    Dungeon.hero.sprite.parent, DungeonTilemap.tileCenterToWorld(Dungeon.hero.pos), 2f))

            val dewVial = Dungeon.hero.belongings.getItem(DewVial::class.java)
            if (dewVial == null || dewVial.rune != null) {
                Dungeon.level.drop(rune, Dungeon.hero.pos)
            } else {
                dewVial.rune = rune
                GLog.w(M.L(ExtraRuneRegularly::class.java, "generated", rune.name()))
            }
        }
    }
}

class BaredAngry : Perk() {
    override fun image(): Int = PerkImageSheet.BARED_ANGRY

    fun procGivenDamage(dmg: Damage, hero: Hero) {
        if (noArmor(hero)) dmg.value += dmg.value / 4
    }

    fun speedFactor(hero: Hero): Float = if (noArmor(hero)) 0.7f else 1f

    private fun noArmor(hero: Hero): Boolean = hero.belongings.armor == null
}

class BaredSwiftness : Perk() {
    override fun image(): Int = PerkImageSheet.BARED_SWIFTNESS

    fun speedFactor(hero: Hero): Float = if (noArmor(hero)) 1.2f else 1f

    fun evasionProb(hero: Hero): Float = if (noArmor(hero)) 0.12f else 0f

    private fun noArmor(hero: Hero): Boolean = hero.belongings.armor == null
}

class QuickLearner : Perk(3) {
    override fun image(): Int = PerkImageSheet.EXP_EXTRA

    private var dexp = 0f

    override fun description(): String = M.L(this, "desc", (ratio() * 100).toInt())

    fun extraExp(exp: Int): Int {
        dexp += exp * ratio()
        val e = dexp.toInt()
        dexp -= e

        return e
    }

    private fun ratio(): Float = 0.05f + level * 0.1f

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("dexp", dexp)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        dexp = bundle.getFloat("dexp")
    }
}

class ExtraMagicalResistance : Perk(3) {
    override fun image(): Int = PerkImageSheet.MAGICAL_RESISTANCE

    fun ratio(): Float = 0.05f + level * 0.15f

    override fun description(): String = M.L(this, "desc", (ratio() * 100).toInt())
}

class LevelPerception : Perk() {
    override fun image(): Int = PerkImageSheet.LEVEL_PERCEPTION
}

class Maniac : Perk() {
    override fun image(): Int = PerkImageSheet.MANIAC

    fun speedFactor(hero: Hero): Float {
        val n = min(hero.visibleEnemies(), 8)
        return if (n <= 1) 1f else (0.5f + 0.5f * 0.8f.pow(n)) // no bonus when 1 v 1
    }
}

class PolearmMaster : Perk(2) {
    override fun image(): Int = PerkImageSheet.POLEARM

    fun proc(damage: Damage, weapon: MeleeWeapon) {
        val defender = damage.to as Char
        val ratio = 0.1f + weapon.tier * 0.05f * level // 0.15 ~ 0.35 => 0.2 ~ 0.6

        if (Random.Float() < ratio) {
            val duration = 1f + weapon.tier + weapon.DLY
            Buff.prolong(defender, Cripple::class.java, duration)
        }
    }

    // exile wont get this, while other class can never reach level 2
    // note: i havnt fix the perk upgrade bug, so level 2 may still be possible
    override fun canBeGain(hero: Hero): Boolean = hero.heroClass != HeroClass.EXILE
            && hero.heroPerk.get(PolearmMaster::class.java) == null
}
