package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

/**
 * Created by 93942 on 8/3/2018.
 */

// check in Char::takeDamage
class Vulnerable(var ratio: Float = 1f) : FlavourBuff() {
    init {
        type = buffType.NEGATIVE
    }

    override fun icon(): Int = BuffIndicator.VULERABLE

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, if (ratio < 1f) "desc_1" else "desc_0", ratio, dispTurns())

    companion object {
        const val DURATION = 10f
    }

}
