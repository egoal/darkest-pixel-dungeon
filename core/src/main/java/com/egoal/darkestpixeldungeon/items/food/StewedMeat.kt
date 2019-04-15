package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class StewedMeat : Food(Hunger.STARVING - Hunger.HUNGRY, 1) {
    init {
        image = ItemSpriteSheet.STEWED
    }

    override fun price(): Int = 6 * quantity

    companion object {
        fun Cook(meat: MysteryMeat): Food = StewedMeat().apply {
            quantity = meat.quantity()
        }
    }
}