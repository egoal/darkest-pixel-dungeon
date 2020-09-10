package com.egoal.darkestpixeldungeon.items.books

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.actors.hero.perks.RavenousAppetite
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndSelectPerk

class TomeOfRetrain : Book() {
    init {
        image = ItemSpriteSheet.TOME_BLUE

        unique = true
    }

    override val isIdentified: Boolean
        get() = true
    
    override fun price(): Int = 100

    override fun doRead(hero: Hero) {
        //todo: refactor
        val perks = hero.heroPerk.perks.filter { it !is RavenousAppetite }
        if (perks.isEmpty()) {
            GLog.w(M.L(Book::class.java, "cannot_understand"))
            return
        }

        GameScene.show(object : WndSelectPerk(M.L(TomeOfRetrain::class.java, "select_perk"), perks) {
            override fun onPerkSelected(perk: Perk) {
                hero.heroPerk.downgrade(perk)
                hero.reservedPerks += 1

                detach(hero.belongings.backpack)
            }
        })
    }
}