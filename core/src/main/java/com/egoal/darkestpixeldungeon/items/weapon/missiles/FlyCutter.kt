package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class FlyCutter(number: Int = 1) : MissileWeapon(3) {
    init {
        image = ItemSpriteSheet.FLY_CUTTER

        quantity = number
    }
}