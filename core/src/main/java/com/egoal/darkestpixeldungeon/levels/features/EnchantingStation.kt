package com.egoal.darkestpixeldungeon.levels.features

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndEnchanting

object EnchantingStation {
    fun Operate(hero: Hero) {
        GameScene.show(WndEnchanting())
    }

    fun CanTransform(src: Item, tgt: Item): String? = when {
        !src.isIdentified || !tgt.isIdentified -> Messages.get(this, "unidentified")
        src.cursed || tgt.cursed -> Messages.get(this, "cursed")
        src is Weapon -> when {
            src.inscription == null -> Messages.get(this, "no_enchantment")
            tgt !is Weapon -> Messages.get(this, "wrong_type")
            else -> null
        }
        src is Armor -> when {
            src.glyph == null -> Messages.get(this, "no_enchantment")
            tgt !is Armor -> Messages.get(this, "wrong_type")
            else -> null
        }
        else -> "bad operation." // never be here
    }

    fun Transform(src: Item, tgt: Item): Boolean {
        GLog.p(Messages.get(this, "transformed", src.name(), tgt.name()))

        return when (src) {
            is Weapon -> {
                (tgt as Weapon).inscribe(src.inscription)
                true
            }
            is Armor -> {
                (tgt as Armor).inscribe(src.glyph)
                src.checkSeal()?.let { brokenSeal -> tgt.affixSeal(brokenSeal) }
                true
            }
            else -> false
        }
    }

}