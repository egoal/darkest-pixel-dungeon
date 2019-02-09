package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class CircletEmerald: Helmet() {
    init{
        image = ItemSpriteSheet.HELMET_EMERALD
    }

    override fun price(): Int = 80 
}