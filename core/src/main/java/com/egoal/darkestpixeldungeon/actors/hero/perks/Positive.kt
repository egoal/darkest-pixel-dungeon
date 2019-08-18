package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRecharging
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random
import kotlin.math.min
import kotlin.math.round

class Drunkard : Perk()

class GoodAppetite : Perk() {
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
    private fun extraHT(): Int = 2 * level

    fun upgradeHero(hero: Hero) {
        val dht = extraHT()
        hero.HT += dht
        hero.HP += dht
    }
}

class Keen : Perk(3) {
    fun baseAwareness(): Float = 0.95f - 0.1f * level
}

class WandPerception : Perk(2) {
    fun onWandUsed(wand: Wand) {
        if (level == 1) wand.levelKnown = true
        else wand.identify()
    }

    override fun description(): String = M.L(this, "desc_$level")
}

class NightVision : Perk()

class Telepath : Perk()

class Fearless : Perk()

class Assassin : Perk()

class IntendedTransportation : Perk()

class Optimistic : Perk(2) {
    fun resistChance(): Float = 0.05f + 0.1f * level // 0.15-> 0.25

    override fun canBeGain(hero: Hero): Boolean = hero.heroClass != HeroClass.SORCERESS
}

class Discount : Perk(2) {
    fun buyPrice(item: Item): Int = (item.sellPrice() * ratio()).toInt()

    fun ratio(): Float = 1f - 0.25f * level
}

class VampiricCrit : Perk(5) {
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
    fun procCrit(dmg: Damage) {
        dmg.addFeature(Damage.Feature.PURE)
    }
}

class ExtraCritProbability : Perk(5) {
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
    fun procCrit(dmg: Damage) {
        dmg.value += round(dmg.value * extraCritRatio()).toInt()
    }

    private fun extraCritRatio(): Float = 0.2f + 0.3f * level
}

class LowHealthDexterous : Perk(3) {
    fun evasionFactor(hero: Hero): Float {
        if (hero.HP > hero.HT * 0.3) return 1f
        return Math.pow(1.25, level.toDouble()).toFloat()
    }
}

class ExtraDexterous : Perk(5) {
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

class ExtraDexterousGrowth : Perk(5) {
    private fun extraDex(): Float = level * 0.5f

    fun upgradeHero(hero: Hero) {
        hero.defSkill += extraDex()
    }
}

class LowHealthRegeneration : Perk(5) {
    fun extraRegeneration(hero: Hero): Float {
        if (hero.HP > hero.HT * 0.25) return 0f

        return 0.1f + 0.3f * level
    }
}

class ExtraPerkChoice : Perk()

class BrewEnhancedPotion : Perk(3) {
    fun affectPotion(p: Potion) {
        if (p.canBeReinforced() && Random.Float() < (0.05f + 0.15f * level))
            p.reinforce()
    }
}

class Knowledgeable : Perk(3) {
    fun affectItem(item: Item) {
        if (item.isIdentified) return

        if (Random.Float() < identifyChance()) {
            item.identify()
            GLog.w(M.L(this, "identity"))
        } else if (!item.cursedKnown && Random.Float() < identifyChance() * 2f) {
            item.cursedKnown = true
            GLog.w(M.L(this, "know-curse"))
        }
    }

    private fun identifyChance(): Float = 0.05f + 0.2f * level
}

class EfficientSearch : Perk()

class ExtraStrengthPower : Perk(3) {
    fun affectDamage(dmg: Damage, exStr: Int) {
        for (i in 1..exStr)
            dmg.value += Random.Int(1, exStr)
    }
}

class FastRegeneration : Perk(5) {
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

class EfficientPotionOfHealing : Perk()

class PressureIsPower : Perk() {
    fun affectDamage(dmg: Damage, hero: Hero, pressure: Pressure) {
        val p = pressure.pressure / Pressure.MAX_PRESSURE
        if (p < 0.2f) return

        val r = p / 2f + p * p / 2f + 0.8f
        dmg.value = round(dmg.value * r).toInt()
    }
}

class WandCharger : Perk(3) {
    fun factor(): Float = 2f - Math.pow(0.9, level.toDouble()).toFloat()
}

class WandArcane : Perk(3) {
    fun factor(): Float = 2f - Math.pow(0.85, level.toDouble()).toFloat()
}
