package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class Kusarigama : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.KUSARIGAMA

        tier = 4
        RCH = 3
        DLY = 1.5f
    }
}