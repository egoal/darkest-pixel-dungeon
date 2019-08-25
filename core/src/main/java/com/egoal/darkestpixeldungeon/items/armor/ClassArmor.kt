/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.items.armor

import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.items.unclassified.BrokenSeal
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle

import java.util.ArrayList

abstract class ClassArmor : Armor(6) {
    private var armorTier: Int = 0

    init {
        levelKnown = true
        cursedKnown = true
        defaultAction = AC_SPECIAL

        bones = false
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ARMOR_TIER, armorTier)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        armorTier = bundle.getInt(ARMOR_TIER)
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.remove(AC_DETACH)
        if (hero.HP >= 3 && isEquipped(hero)) actions.add(AC_SPECIAL)

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_SPECIAL) {
            if (hero.HP < 3) {
                GLog.w(Messages.get(this, "low_hp"))
            } else if (!isEquipped(hero)) {
                GLog.w(Messages.get(this, "not_equipped"))
            } else {
                curUser = hero
                Invisibility.dispel()
                doSpecial()
            }
        }
    }

    abstract fun doSpecial()

    override fun STRReq(lvl: Int): Int {
        var lvl = Math.max(0, lvl)
        var effectiveTier = armorTier.toFloat()
        if (glyph != null) effectiveTier += glyph.tierSTRAdjust()
        effectiveTier = Math.max(0f, effectiveTier)

        //strength req decreases at +1,+3,+6,+10,etc.
        return 8 + Math.round(effectiveTier * 2) - (Math.sqrt((8 * lvl + 1).toDouble()) - 1).toInt() / 2
    }

    override fun DRMax(lvl: Int): Int {
        var effectiveTier = armorTier
        if (glyph != null) effectiveTier += glyph.tierDRAdjust()
        effectiveTier = Math.max(0, effectiveTier)

        return effectiveTier * (2 + lvl)
    }

    override fun isIdentified(): Boolean = true

    override fun price(): Int = 0

    companion object {
        private const val AC_SPECIAL = "SPECIAL"
        private const val ARMOR_TIER = "armortier"

        fun upgrade(owner: Hero, armor: Armor): ClassArmor {
            val classArmor: ClassArmor = when (owner.heroClass) {
                HeroClass.WARRIOR -> WarriorArmor().apply {
                    val seal = armor.checkSeal()
                    if (seal != null) affixSeal(seal)
                }
                HeroClass.ROGUE -> RogueArmor()
                HeroClass.MAGE -> MageArmor()
                HeroClass.HUNTRESS -> HuntressArmor()
                HeroClass.SORCERESS -> SorceressArmor()
            }

            classArmor.level(armor.level())
            classArmor.armorTier = armor.tier
            classArmor.inscribe(armor.glyph)

            return classArmor
        }
    }

}
