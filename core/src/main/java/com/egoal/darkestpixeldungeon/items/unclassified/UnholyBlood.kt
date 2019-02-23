package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import java.util.ArrayList

/**
 * Created by 93942 on 7/24/2018.
 */

class UnholyBlood : Item() {
    init {
        image = ItemSpriteSheet.UNHOLY_BLOOD
        unique = true
    }

    override fun isUpgradable() = false

    override fun actions(hero: Hero): ArrayList<String>{
        val actions = super.actions(hero)
        hero.belongings.getItem(DewVial::class.java)?.run{
            actions.add(AC_IRRIGATE)
        }
        
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        
        if(action== AC_IRRIGATE){
            detach(hero.belongings.backpack)
            hero.belongings.getItem(DewVial::class.java)!!.apply { 
                rune = BloodRune()
                updateQuickslot()
            }
            
            GLog.w(Messages.get(this, "irrigate"))
        }
    }
    
    companion object {
        private const val AC_IRRIGATE = "irrigate"
    }
}
