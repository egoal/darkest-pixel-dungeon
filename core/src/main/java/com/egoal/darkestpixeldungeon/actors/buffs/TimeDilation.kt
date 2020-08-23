package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class TimeDilation : FlavourBuff() {
    init {
        type = buffType.POSITIVE
    }

    override fun icon(): Int = BuffIndicator.TIME_DILATION

    override fun attachTo(target: Char?): Boolean {
        if(super.attachTo(target)){
            GameScene.setColorLayer(0x4c006699)
            return true
        }

        return false
    }

    override fun detach() {
        super.detach()
        GameScene.resetColorLayer()
    }

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns())
}