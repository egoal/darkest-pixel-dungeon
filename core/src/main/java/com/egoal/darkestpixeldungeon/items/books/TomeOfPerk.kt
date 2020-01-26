package com.egoal.darkestpixeldungeon.items.books

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.ExtraPerkChoice
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.windows.WndSelectPerk

class TomeOfPerk : Book() {
    init {
        image = ItemSpriteSheet.MASTERY
    }

    override fun isIdentified(): Boolean = true

    override fun price(): Int = 0

    override fun doRead(hero: Hero) {
        detach(hero.belongings.backpack)

        hero.reservedPerks += 1
    }
}