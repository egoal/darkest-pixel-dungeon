package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.watabou.utils.Random
import java.util.ArrayList

/**
 * Created by 93942 on 2018/12/22.
 */
class DPDEmptyLevel: DPDSewerLevel() {
    init {
        viewDistance = 8
    }

    override fun createMobs() {}

    override fun chooseDiggers(): ArrayList<Digger> {
        return selectDiggers(Random.NormalIntRange(3, 5), 16)
    }
}