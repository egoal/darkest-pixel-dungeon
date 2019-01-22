package com.egoal.darkestpixeldungeon.items

import com.egoal.darkestpixeldungeon.sprites.ItemSprite
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
