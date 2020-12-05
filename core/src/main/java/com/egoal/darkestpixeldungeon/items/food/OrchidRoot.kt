package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Relieve
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class OrchidRoot : Food(enery = 10f, hornValue = 0) {
    init {
        image = ItemSpriteSheet.ROOT
    }

    override fun onEat(hero: Hero) {
        super.onEat(hero)

        Buff.affect(hero, Relieve::class.java).prolong(60f) // [2, 6] + 60 * 0.3
    }
}