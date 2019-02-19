package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Rage: FlavourBuff() {
    init {
        type = buffType.POSITIVE
    }

    override fun icon(): Int = BuffIndicator.FURY

    override fun toString(): String = Messages.get(this, "name")

    override fun heroMessage(): String = Messages.get(this, "heromsg")

    override fun desc(): String = Messages.get(this, "desc", dispTurns())
}