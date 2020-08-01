package com.egoal.darkestpixeldungeon.items.books

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndSelectPerk

class TomeOfUpgrade : Book() {
    init {
        image = ItemSpriteSheet.TOME_YELLOW

        unique = true
    }

    override fun isIdentified(): Boolean = true

    override fun price(): Int = 100

    override fun doRead(hero: Hero) {
        //todo: refactor
        val perks = hero.heroPerk.perks.filter { it.upgradable() }
        if (perks.isEmpty()) {
            GLog.w(M.L(Book::class.java, "cannot_understand"))
            return
        }

        GameScene.show(object : WndSelectPerk(M.L(TomeOfUpgrade::class.java, "select_perk"), perks) {
            override fun onPerkSelected(perk: Perk) {
                PerkGain.Show(hero, perk)
                hero.heroPerk.add(perk)
                hero.perkGained += 1

                detach(hero.belongings.backpack)
            }
        })
    }
}