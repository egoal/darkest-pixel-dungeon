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

        val cnt = if (hero.heroPerk.get(ExtraPerkChoice::class.java) == null) 3 else 5
        GameScene.show(WndSelectPerk.CreateWithRandomPositives(
                M.L(WndSelectPerk::class.java, "select"), cnt))
    }
}