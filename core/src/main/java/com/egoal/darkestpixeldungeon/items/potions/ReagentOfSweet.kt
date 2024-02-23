package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Relieve
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class ReagentOfSweet : Reagent(true) {
    init {
        image = ItemSpriteSheet.REAGENT_SWEET
    }

    override fun drink(hero: Hero) {
        super.drink(hero)

        hero.recoverSanity(Random.Float(5f, 9f))
        Buff.affect(hero, Relieve::class.java).prolong(20f)
    }
}