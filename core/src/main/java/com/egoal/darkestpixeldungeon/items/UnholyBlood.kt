package com.egoal.darkestpixeldungeon.items

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

/**
 * Created by 93942 on 7/24/2018.
 */

class UnholyBlood : Item() {
    init {
        image = ItemSpriteSheet.DPD_UNHOLY_BLOOD
        unique = true
    }

    override fun isUpgradable() = false
}
