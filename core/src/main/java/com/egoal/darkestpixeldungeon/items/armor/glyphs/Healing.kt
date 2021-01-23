package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

// see Hero:regenerationSpeed
class Healing : Armor.Glyph() {
    override fun proc(armor: Armor, damage: Damage): Damage = damage

    override fun glowing(): ItemSprite.Glowing = TEAL

    fun speed(armor: Armor) = 0.15f + 0.03f * armor.level()

    companion object {
        private val TEAL = ItemSprite.Glowing(0x239a1d)
    }
}