package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

/**
 * Created by 93942 on 10/30/2018.
 */

class SharpVision : FlavourBuff() {

    init {
        type = buffType.POSITIVE
    }

    override fun icon(): Int {
        return BuffIndicator.MIND_VISION
    }

    override fun toString(): String = M.L(this, "name")

    override fun detach() {
        super.detach()
        Dungeon.observe()
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns())
    }

    companion object {
        const val DURATION = 50f
    }
}
