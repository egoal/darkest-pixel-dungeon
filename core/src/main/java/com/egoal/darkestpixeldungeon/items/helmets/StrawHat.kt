package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class StrawHat : Helmet() {
    init {
        image = ItemSpriteSheet.STRAW_HAT
    }

    override fun desc(): String = super.desc() + "\n\n" + M.L(this, "effect-desc")

    override fun isIdentified(): Boolean = true

    override fun random(): Item {
        cursed = false
        return this
    }

    override fun price(): Int = 0
}