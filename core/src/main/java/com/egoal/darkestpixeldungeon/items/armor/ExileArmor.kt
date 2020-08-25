package com.egoal.darkestpixeldungeon.items.armor

import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import kotlin.math.max

class ExileArmor : ClassArmor() {
    init {
        image = ItemSpriteSheet.ARMOR_EXILE
    }

    override fun doSpecial() {
        val hero = Item.curUser
        if (hero.HP > hero.HT * 3 / 4) {
            hero.sayShort(HeroLines.NOT_NOW)
            return
        }

        hero.HP -= hero.HP / 3
        hero.SHLD = max(hero.SHLD, (hero.HT - hero.HP) / 2)

        hero.spendAndNext(1f)
    }
}