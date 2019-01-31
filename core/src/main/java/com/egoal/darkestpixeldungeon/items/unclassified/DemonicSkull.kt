package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.MaskOfMadness
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog

import java.util.ArrayList

/**
 * Created by 93942 on 6/18/2018.
 */

class DemonicSkull : Item() {

    init {
        image = ItemSpriteSheet.DEMONIC_SKULL

        levelKnown = true
        cursedKnown = levelKnown
        unique = true
    }

    override fun isUpgradable(): Boolean = false

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        
        hero.belongings.getItem(UnholyBlood::class.java)?.run { 
            actions.add(AC_SMEAR)
        }

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action === AC_SMEAR) {
            detach(hero.belongings.backpack)
            hero.belongings.getItem(UnholyBlood::class.java)!!.detach(hero.belongings.backpack)

            val mom = MaskOfMadness()
            mom.identify().collect()

            GLog.w(Messages.get(Dungeon.hero, "you_now_have", mom.name()))
        }
    }

    companion object {

        private val AC_SMEAR = "SMEAR"
    }
}
