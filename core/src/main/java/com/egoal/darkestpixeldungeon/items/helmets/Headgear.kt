package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class Headgear : Helmet() {
    init {
        image = ItemSpriteSheet.HEADGEAR
    }

    override fun doEquip(hero: Hero): Boolean {
        return if (super.doEquip(hero)) {
            if (!cursed) hero.STR += 1
            true
        } else false
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        return if (super.doUnequip(hero, collect, single)) {
            if (!cursed) hero.STR -= 1
            true
        } else false
    }

    override fun uncurse() {
        super.uncurse()
        if (cursed) Dungeon.hero.STR += 1
    }
}