package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSprite

class Peaceful : Armor.Glyph() {
    override fun proc(armor: Armor, damage: Damage): Damage {
        return damage
    }

    override fun glowing(): ItemSprite.Glowing = TEAL

    companion object {
        private val TEAL = ItemSprite.Glowing(0x239a1d)
    }
}