package com.egoal.darkestpixeldungeon.items.books

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class TomeOfPerk : Book() {
    init {
        image = ItemSpriteSheet.MASTERY

        unique = true // consider: stolen/resurrect
    }

    override fun isIdentified(): Boolean = true

    override fun price(): Int = 100

    override fun doRead(hero: Hero) {
        detach(hero.belongings.backpack)

        hero.reservedPerks += 1
    }
}