package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRecharging
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import kotlin.math.min

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
}

class StrongConstitution : Perk(10) {
    private fun extraHT(): Int = level

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

class Optimistic : Perk(2) {
    fun resistChance(): Float = 0.05f + 0.1f * level // 0.15-> 0.25
}

class Discount : Perk(2) {
    fun buyPrice(item: Item): Int = (item.sellPrice() * ratio()).toInt()

    fun ratio(): Float = 1f - 0.25f * level
}

class VampiricCrit : Perk(10) {
    fun procCrit(dmg: Damage): Damage {
        assert(dmg.isFeatured(Damage.Feature.CRITICAL))

        val hero = dmg.from as Hero
        val eff = min(hero.HT - hero.HP, ((0.1f + 0.2f * level) * dmg.value).toInt())

        hero.apply {
            HP += eff
            sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 1)
            sprite.showStatus(CharSprite.POSITIVE, "$eff")
        }
        return dmg
    }
}

class PureCrit : Perk() {
    fun procCrit(dmg: Damage): Damage = dmg.apply { addFeature(Damage.Feature.PURE) }
}

class ExtraCritProbability : Perk(10) {
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

class HardCrit : Perk(10) {
    fun extraCritRatio(): Float = 0.2f + 0.3f * level
}