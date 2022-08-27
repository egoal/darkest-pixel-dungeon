package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Lucky : FlavourBuff() {
    init {
        type = buffType.POSITIVE
    }

    override fun icon(): Int = BuffIndicator.BLESS

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns())
}