package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

/**
 * Created by 93942 on 10/20/2018.
 */

// see Char::checkHit
class Shock : FlavourBuff() {

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun icon(): Int {
        return BuffIndicator.SHOCK
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns())
    }
}
