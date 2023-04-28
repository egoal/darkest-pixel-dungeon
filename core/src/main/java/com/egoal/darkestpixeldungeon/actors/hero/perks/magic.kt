package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.messages.M
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

class ArcaneCrit : Perk(5) {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.ARCANE_CRIT

    fun affectDamage(hero: Hero, dmg: Damage) {
        if (!dmg.isFeatured(Damage.Feature.CRITICAL) && com.watabou.utils.Random.Float() < prob(hero)) {
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

class CloseZap : Perk() {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.CLOSE_ZAP

    fun procDamage(damage: Damage) {
        val dis = Dungeon.level.distance((damage.from as Char).pos, (damage.to as Char).pos)
        // 1.225, 1.15, 1.075, 1, 0.9, 0.8, ...
        var ratio = if (dis <= 4) 1.3f - 0.075f * dis else max(1.4f - dis * 0.1f, 0.1f)
        if (dis == 1 && com.watabou.utils.Random.Float() < 0.1f) {
            ratio *= 1.1f
            damage.addFeature(Damage.Feature.CRITICAL)
        }

        damage.value = round(damage.value * ratio).toInt()
    }
}

class ManaDrine : Perk() {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.MANA_DRINE
    fun affect(hero: Hero) {
        Buff.affect(hero, Recharging::class.java, 1f) // +0.25 for all wands
    }
}

class PreheatedZap : Perk() {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.PREHEATED_ZAP
}

class QuickZap : Perk() {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.WAND_QUICK_ZAP
}

class StealthCaster : Perk() {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.STEALTH_CASTER
}

class WandArcane : Perk(3) {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.WAND_ARCANE

    fun factor(): Float = 1f + 0.2f * level // 2f - 0.8f.pow(level)
}

class WandCharger : Perk(3) {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.WAND_CHARGE

    fun factor(): Float = 2f - 0.8f.pow(level)
}

class WandPerception : Perk(2) {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.WAND_PERCEPTION

    fun onWandUsed(wand: Wand) {
        if (level == 1) wand.levelKnown = true
        else wand.identify()
    }

    override fun description(): String = M.L(this, "desc_$level")
}

class WandPiercing : Perk(3) {
    init {
        addTags(Tag.Wand)
    }
    override fun image(): Int = PerkImageSheet.WAND_PIERCING

    fun onHit(char: Char) {
        char.magicalResistance -= ratio()// fixme:
    }

    override fun description(): String = M.L(this, "desc", (ratio() * 100).toInt())

    private fun ratio() = 0.05f + 0.04f * level
}