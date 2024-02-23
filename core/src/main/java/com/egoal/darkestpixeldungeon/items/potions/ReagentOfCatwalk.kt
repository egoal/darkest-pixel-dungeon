package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Catwalk
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class ReagentOfCatwalk : Reagent(true) {
    init {
        image = ItemSpriteSheet.REAGENT_CATWALK
    }

    override fun drink(hero: Hero) {
        super.drink(hero)
        Buff.prolong(hero, Catwalk::class.java, 30f)
    }
}