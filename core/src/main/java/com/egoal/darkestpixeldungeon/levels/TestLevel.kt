package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.MagicWellDigger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.WandDigger

class TestLevel : SewerLevel() {
    override fun chooseDiggers(): ArrayList<Digger> {
        val diggers = selectDiggers(0, 8)
        // diggers.add(WandDigger())
        diggers.add(MagicWellDigger())
        
        return diggers
    }
}