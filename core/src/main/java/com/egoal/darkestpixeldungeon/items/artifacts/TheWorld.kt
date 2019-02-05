package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import java.util.ArrayList

class TheWorld : Artifact() {
    init {
        image = ItemSpriteSheet.ARTIFACT_HOURGLASS

        levelCap = 5

        chargeCap = 10 + level() * 2
        charge = chargeCap
        partialCharge = 0f

        defaultAction = AC_ACTIVATE
    }
    
    var sandBags = 0

    override fun actions(hero: Hero): ArrayList<String> {
        val actions =  super.actions(hero)
        if(isEquipped(hero) && charge> 0 && !cursed)
            actions.add(AC_ACTIVATE)
        
        return actions
    }
    

    companion object {
        private const val AC_ACTIVATE = "activate"
    }

}