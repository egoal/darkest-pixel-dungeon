package com.egoal.darkestpixeldungeon.items.bags

import com.egoal.darkestpixeldungeon.actors.hero.Belongings
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.special.UrnOfShadow
import com.egoal.darkestpixeldungeon.items.special.StrengthOffering
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class SkillTree : Bag() {
    init {
        image = ItemSpriteSheet.SKILL_TREE

        size = Belongings.BACKPACK_SIZE + 2
    }

    // todo:
    override fun canHold(item: Item): Boolean = (item is UrnOfShadow || item is StrengthOffering)
            && super.canHold(item)

    override fun price(): Int = 0
}