package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Decayed : FlavourBuff() {
    init {
        type = buffType.NEGATIVE
    }

    override fun icon(): Int = BuffIndicator.DECAYED

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns())

    override fun heroMessage(): String? = M.L(this, "heromsg")

    override fun fx(on: Boolean) {
        if (on) target.sprite.add(CharSprite.State.MARKED)
        else target.sprite.remove(CharSprite.State.MARKED)
    }
}