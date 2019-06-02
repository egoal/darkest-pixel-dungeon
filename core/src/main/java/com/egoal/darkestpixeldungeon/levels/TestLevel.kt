package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.CryptDigger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.GardenDigger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.MagicWellDigger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.WandDigger

class TestLevel : SewerLevel() {
    override fun chooseDiggers(): ArrayList<Digger> {
        val diggers = selectDiggers(0, 10)
        // diggers.add(WandDigger())
        // diggers.add(CryptDigger())
        // diggers.add(GardenDigger())
//        diggers.add(MagicWellDigger())
//        diggers.add(MagicWellDigger())
        
        return diggers
    }
}