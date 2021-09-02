package com.egoal.darkestpixeldungeon.items.bags

import com.egoal.darkestpixeldungeon.Dungeon
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

    class Updater : Buff() {
        lateinit var skills: SkillTree

        init {
            actPriority = -1 // before hero.
        }

        override fun act(): Boolean {
            if (!::skills.isInitialized)
                skills = Dungeon.hero.belongings.getItem(SkillTree::class.java)!!

            for (i in skills.items) (i as Special).tick()

            spend(1f)
            return true
        }
    }
}