package com.egoal.darkestpixeldungeon.items.bags

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Belongings
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.specials.Special
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class SkillTree : Bag() {
    init {
        image = ItemSpriteSheet.SKILL_TREE

        size = Belongings.BACKPACK_SIZE + 2
    }

    override fun canHold(item: Item): Boolean = super.canHold(item) && item is Special

    override fun price(): Int = 0

    inner class Updater : Buff() {
        init {
            actPriority = -1 // before hero.
        }

        override fun act(): Boolean {
            items.forEach { (it as Special).tick() }
            spend(1f)
            return true
        }
    }
}