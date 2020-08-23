package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Tipsy : FlavourBuff() {
    init {
        type = buffType.NEUTRAL
    }

    override fun icon(): Int = BuffIndicator.TIPSY

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns())
}