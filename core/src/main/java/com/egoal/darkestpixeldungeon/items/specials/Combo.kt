package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class Combo: Special() {
    init {
        image = ItemSpriteSheet.NULLWARN
    }

    private var comboTime = 0f
    private var focusCount = 0
    private var adrenaline = 0

    override fun tick() {
        comboTime -= 1f
        // update state
        updateQuickslot()
    }
}