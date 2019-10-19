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
package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import kotlin.math.max

open class MeleeWeapon : Weapon() {
    var tier: Int = 0

    override fun min(lvl: Int): Int = tier + lvl

    override fun max(lvl: Int): Int = 5 * (tier + 1) + lvl * (tier + 1)

    override fun upgrade(): Item = upgrade(false)

    fun safeUpgrade(): Item = upgrade(enchantment != null)

    override fun STRReq(lvl: Int): Int {
        var lvl = Math.max(0, lvl)
        //strength req decreases at +1,+3,+6,+10,etc.
        return 8 + tier * 2 - (Math.sqrt((8 * lvl + 1).toDouble()) - 1).toInt() / 2
    }

    override fun info(): String {
        var info = desc()

        if (levelKnown) {
            info += "\n\n" + M.L(MeleeWeapon::class.java, "stats_known", tier,
                    imbue.damageFactor(min()), imbue.damageFactor(max()), STRReq())
            if (STRReq() > Dungeon.hero.STR()) {
                info += " " + M.L(Weapon::class.java, "too_heavy")
            } else if (Dungeon.hero.STR() > STRReq()) {
                info += " " + M.L(Weapon::class.java, "excess_str", Dungeon.hero.STR() - STRReq())
            }
        } else {
            info += "\n\n" + M.L(MeleeWeapon::class.java, "stats_unknown", tier,
                    min(0), max(0), STRReq(0))
            if (STRReq(0) > Dungeon.hero.STR()) {
                info += " " + M.L(MeleeWeapon::class.java, "probably_too_heavy")
            }
        }

        val stats_desc = M.L(this, "stats_desc")
        if (stats_desc != "") info += "\n\n" + stats_desc

        when (imbue) {
            Imbue.LIGHT -> info += "\n\n" + M.L(Weapon::class.java, "lighter")
            Imbue.HEAVY -> info += "\n\n" + M.L(Weapon::class.java, "heavier")
        }

        if (enchantment != null && (cursedKnown || !enchantment.curse())) {
            info += "\n\n" + M.L(Weapon::class.java, "enchanted", enchantment
                    .name())
            info += " " + M.L(enchantment, "desc")
        }

        if (cursed && isEquipped(Dungeon.hero)) {
            info += "\n\n" + M.L(Weapon::class.java, "cursed_worn")
        } else if (cursedKnown && cursed) {
            info += "\n\n" + M.L(Weapon::class.java, "cursed")
        }

        return info
    }

    override fun price(): Int {
        var price = 20 * tier
        if (hasGoodEnchant()) {
            price = price * 3 / 2
        }
        if (cursedKnown && (cursed || hasCurseEnchant())) {
            price /= 2
        }
        if (levelKnown && level() > 0) {
            price *= level() + 1
        }

        return max(1, price)
    }

}
