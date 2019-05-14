package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class SwallowDart(number: Int = 1) : MissileWeapon(2) {
    init {
        image = ItemSpriteSheet.SWALLOW_DART

        quantity = number
        ACC = 1.25f
    }
}