package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.M

class Lucky : FlavourBuff() {
    init {
        type = buffType.POSITIVE
    }

    override fun toString(): String = M.L(this, "name")
}