package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import java.util.ArrayList

class Gourd : Item() {
    init {
        image = ItemSpriteSheet.GOURD

        defaultAction = AC_DRINK
        unique = true
    }

    override fun isIdentified(): Boolean = true
    override fun isUpgradable(): Boolean = false

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_DRINK) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_DRINK) {
        }
    }


    companion object {
        private const val AC_DRINK = "drink"
    }
}
