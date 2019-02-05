package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class StewedMeat : Food() {
    init {
        image = ItemSpriteSheet.STEWED
        energy = Hunger.STARVING - Hunger.HUNGRY
        hornValue = 1
    }

    override fun price(): Int = 6 * quantity

    companion object {
        fun Cook(meat: MysteryMeat): Food = StewedMeat().apply {
            quantity = meat.quantity()
        }
    }
}