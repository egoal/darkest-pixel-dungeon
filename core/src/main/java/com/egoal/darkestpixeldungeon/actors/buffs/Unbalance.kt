package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

// see Char::checkHit
class Unbalance : FlavourBuff() {
    init {
        type = buffType.NEGATIVE
    }

    override fun icon(): Int = BuffIndicator.UNBALANCE

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns())

}