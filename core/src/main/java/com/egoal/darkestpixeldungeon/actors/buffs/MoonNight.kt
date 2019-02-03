package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class MoonNight : FlavourBuff() {
    init {
        type = buffType.NEUTRAL
    }

    override fun icon(): Int = BuffIndicator.MOON_NIGHT
    
    override fun toString(): String = Messages.get(this, "name")

    override fun desc(): String = Messages.get(this, "desc")

    override fun attachTo(target: Char?): Boolean {
        val attached = super.attachTo(target)
        if (attached) {
            Dungeon.observe()
            GameScene.updateFog()
        }
        
        return attached
    }

    override fun detach() {
        super.detach()
        Dungeon.observe()
        GameScene.updateFog()
    }

    companion object {
        const val DURATION = 100f
    }
}