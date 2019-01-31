package com.egoal.darkestpixeldungeon.items.weapon.enchantments

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

/**
 * Created by 93942 on 10/13/2018.
 */

class Suppress : Weapon.Enchantment() {

    override fun proc(weapon: Weapon, damage: Damage): Damage {
        if (damage.to is Char) {
            val pm = 1f - (damage.to as Char).HP / (damage.to as Char).HT.toFloat()
            val level = Math.max(0, weapon.level())
            val ratio = 1f + Math.pow(pm.toDouble(), 3.0) * (if (level > 3) 1f else .5f)
            damage.value = (damage.value * ratio).toInt()
        }

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = GREY

    companion object {
        private val GREY = ItemSprite.Glowing(0x444444)
    }

}
