package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

/**
 * Created by 93942 on 7/24/2018.
 */

class UnholyBlood : Item() {
    init {
        image = ItemSpriteSheet.UNHOLY_BLOOD
        unique = true
    }

    override fun isUpgradable() = false
}
