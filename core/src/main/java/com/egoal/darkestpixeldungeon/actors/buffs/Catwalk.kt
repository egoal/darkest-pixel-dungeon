package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Catwalk : FlavourBuff() {
    override fun icon(): Int = BuffIndicator.CATWALK

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns())

    override fun attachTo(target: Char): Boolean {
        return if (super.attachTo(target)) {
            target.defSkill += 3f
            true
        } else false
    }

    override fun detach() {
        target.defSkill -= 3f
        super.detach()
    }
}