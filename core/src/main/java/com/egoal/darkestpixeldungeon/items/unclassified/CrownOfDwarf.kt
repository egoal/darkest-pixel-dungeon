package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

/**
 * Created by 93942 on 9/24/2018.
 */

class CrownOfDwarf : Item() {
    init {
        image = ItemSpriteSheet.DPD_DWARF_CROWN
        identify()
    }

    override fun isUpgradable() = false

    override fun price() = 750 * quantity()
}
