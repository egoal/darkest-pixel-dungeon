package com.egoal.darkestpixeldungeon.items.bags

import com.egoal.darkestpixeldungeon.actors.hero.Belongings
import com.egoal.darkestpixeldungeon.items.Item

class Backpack : Bag() {
    init {
        size = Belongings.BACKPACK_SIZE
    }

    override fun canHold(item: Item): Boolean = (item is Bag) || items.count { it !is Bag } < size //ignore bag
}