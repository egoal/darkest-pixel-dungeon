package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

/**
 * Created by 93942 on 9/24/2018.
 */

class CrownOfDwarf : Helmet() {
    init {
        image = ItemSpriteSheet.DWARF_CROWN
    }

    override fun isIdentified(): Boolean = true

    override fun price() = 500
}
