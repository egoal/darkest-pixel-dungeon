package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

/**
 * Created by 93942 on 9/25/2018.
 */

// check in Char::takeDamage
class Ignorant : Buff() {
    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun icon(): Int {
        return BuffIndicator.IGNORANT
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc")
    }
}
