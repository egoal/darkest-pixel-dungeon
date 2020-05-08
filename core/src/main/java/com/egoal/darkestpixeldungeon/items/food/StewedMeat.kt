package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

// may change the derivation
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

class SkewerMeat : Food(Hunger.STARVING - Hunger.HUNGRY, 1) {
    init {
        image = ItemSpriteSheet.SKEWER
    }

    override fun price(): Int = 8 * quantity

    override fun onEat(hero: Hero) {
        super.onEat(hero)
        hero.recoverSanity(Random.Float(1f, 3f))
    }
}