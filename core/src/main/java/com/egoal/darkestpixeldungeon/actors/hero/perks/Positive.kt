package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
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
import com.egoal.darkestpixeldungeon.items.unclassified.Rune
import com.egoal.darkestpixeldungeon.items.wands.Wand
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

class Discount : Perk(2) {
    override fun image(): Int = PerkImageSheet.DISCOUNT

    fun buyPrice(item: Item): Int = (item.sellPrice() * ratio()).toInt()

    fun ratio(): Float = 1f - 0.25f * level
}

class VampiricCrit : Perk(5) {
    override fun image(): Int = PerkImageSheet.CRIT_VAMP

    fun procCrit(dmg: Damage) {
        assert(dmg.isFeatured(Damage.Feature.CRITICAL))

        val hero = dmg.from as Hero
        val eff = min(hero.HT - hero.HP, ((0.1f + 0.15f * level) * dmg.value).toInt())

        if (eff > 0)
            hero.apply {
                HP += eff
                sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 1)
                sprite.showStatus(CharSprite.POSITIVE, "$eff")
            }
    }
}

class PureCrit : Perk() {
    override fun image(): Int = PerkImageSheet.CRIT_PURE

    fun procCrit(dmg: Damage) {
        dmg.addFeature(Damage.Feature.PURE)
    }
}

class ExtraCritProbability : Perk(5) {
    override fun image(): Int = PerkImageSheet.CRIT_PROB

    private fun extraProb(): Float = 0.01f + 0.05f * level

    override fun onGain() {
        Dungeon.hero.criticalChance += extraProb()
    }

    override fun onLose() {
        Dungeon.hero.criticalChance -= extraProb()
    }

    override fun upgrade() {
        onLose()
        super.upgrade()
        onGain()
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

    fun evasionFactor(hero: Hero): Float {
        if (hero.HP > hero.HT * 0.3) return 1f
        return Math.pow(1.25, level.toDouble()).toFloat()
    }
}

class ExtraDexterous : Perk(5) {
    override fun image(): Int = PerkImageSheet.DEX_EXTRA

    override fun onGain() {
        Dungeon.hero.defSkill += extraDef()
    }

    override fun onLose() {
        Dungeon.hero.defSkill -= extraDef()
    }

    override fun upgrade() {
        onLose()
        super.upgrade()
        onGain()
    }

    private fun extraDef(): Int = 3 * level
}

class ExtraEvasion : Perk(5) {
    override fun image(): Int = PerkImageSheet.DEX_EXTRA

    fun prob(): Float = 0.075f * level

    override fun description(): String = M.L(this, "desc", (prob() * 100).toInt())
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
        if (hero.HP > hero.HT * 0.25) return 0f

        return 0.1f + 0.3f * level
    }
}

class ExtraPerkChoice : Perk() {
    override fun image(): Int = PerkImageSheet.EXTRA_CHOICE
}

class BrewEnhancedPotion : Perk(3) {
    override fun image(): Int = PerkImageSheet.BREW_ENHANCED

    fun affectPotion(p: Potion) {
        if (p.canBeReinforced() && Random.Float() < (0.1f + 0.2f * level))
            p.reinforce()
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

class FastRegeneration : Perk(5) {
    override fun image(): Int = PerkImageSheet.FASTER_REG

    override fun onGain() {
        Dungeon.hero.regeneration += extraReg()
    }

    override fun onLose() {
        Dungeon.hero.regeneration -= extraReg()
    }

    override fun upgrade() {
        onLose()
        super.upgrade()
        onGain()
    }

    private fun extraReg(): Float = 0.1f * level
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

    fun factor(): Float = 2f - 0.85f.pow(level)
}

class WandArcane : Perk(3) {
    override fun image(): Int = PerkImageSheet.WAND_ARCANE

    fun factor(): Float = 2f - 0.8f.pow(level)
}

class QuickZap : Perk() {
    override fun image(): Int = PerkImageSheet.WAND_QUICK_ZAP
}

class StealthCaster : Perk()

class ArcaneCrit : Perk(5) {
    fun affectDamage(hero: Hero, dmg: Damage) {
        if (Random.Float() < prob(hero))
            dmg.value = round(dmg.value * 1.75f).toInt()
    }

    private fun prob(hero: Hero): Float {
        var prob = hero.criticalChance // base chance, not affected by rune, pressure etc.
        if (level > 1) prob += 0.09f * (level - 1)
        return prob
    }
}

class ExplodeBrokenShot : Perk() {
    override fun image(): Int = PerkImageSheet.SHOT_EXPLODE
}

class RangedShot : Perk() {
    fun affectDamage(dmg: Damage) {
        val dis = Dungeon.level.distance((dmg.from as Char).pos, (dmg.to as Char).pos)
        if (dis > 1) {
            dmg.value = round(dmg.value * (3f - 1.8f * 0.9f.pow(dis - 2))).toInt()
        }
    }
}

class FinishingShot : Perk() {
    fun onKilledChar(hero: Hero, ch: Char, weapon: MissileWeapon) {
        hero.spend(weapon.DLY * 2.5f)
    }
}

class ExtraStrength : Perk(3) {
    override fun image(): Int = PerkImageSheet.STRENGTH_EXTRA

    override fun onGain() {
        Dungeon.hero.STR++
    }

    override fun upgrade() {
        super.upgrade()
        Dungeon.hero.STR++
    }

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

class AngryBared : Perk() {
    override fun image(): Int = PerkImageSheet.BARED_ANGRY

    fun procGivenDamage(dmg: Damage, hero: Hero) {
        if (noArmor(hero)) dmg.value += dmg.value / 4
    }

    fun speedFactor(hero: Hero): Float = if (noArmor(hero)) 0.7f else 1f

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

class LevelPerception : Perk() {
    override fun image(): Int = PerkImageSheet.LEVEL_PERCEPTION
}

