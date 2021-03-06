/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.items.bags

import com.egoal.darkestpixeldungeon.actors.hero.Belongings
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.wands.Wand

class WandHolster : Bag() {

    init {
        image = ItemSpriteSheet.HOLSTER

        size = Belongings.BACKPACK_SIZE
    }

    override fun canHold(item: Item): Boolean = item is Wand && super.canHold(item)

    override fun collect(container: Bag): Boolean {
        if (super.collect(container)) {
            if (owner != null) {
                for (item in items) {
                    (item as Wand).charge(owner!!, HOLSTER_SCALE_FACTOR)
                }
            }
            return true
        } else {
            return false
        }
    }

    override fun onDetach() {
        super.onDetach()
        for (item in items) {
            (item as Wand).stopCharging()
        }
    }

    override fun price(): Int = 50

    companion object {
        const val HOLSTER_SCALE_FACTOR = 0.85f
    }
}
