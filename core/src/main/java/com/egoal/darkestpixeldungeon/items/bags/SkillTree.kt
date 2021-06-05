package com.egoal.darkestpixeldungeon.items.bags

import com.egoal.darkestpixeldungeon.actors.hero.Belongings
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.Astrolabe
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class SkillTree : Bag() {
    init {
        image = ItemSpriteSheet.SKILL_TREE

        size = Belongings.BACKPACK_SIZE
    }

    // todo:
    override fun grab(item: Item): Boolean = item is Astrolabe

    override fun price(): Int = 0
}