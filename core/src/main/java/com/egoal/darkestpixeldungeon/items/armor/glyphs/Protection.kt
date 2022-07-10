package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

// see Proctected::ShiledCap()
class Protection : Armor.Glyph() {
    fun Shield(armor: Armor): Int = armor.tier + armor.level() * 2

    override fun proc(armor: Armor, damage: Damage): Damage = damage

    override fun glowing(): ItemSprite.Glowing = TEAL

    companion object {
        private val TEAL = ItemSprite.Glowing(0xFFEEFF)
    }
}