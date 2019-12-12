package com.egoal.darkestpixeldungeon.actors.buffs


/**
 * Created by 93942 on 5/9/2018.
 */

class Dementage : Corruption() {
    override fun act(): Boolean {
        spend(TICK)
        return true
    }
}
