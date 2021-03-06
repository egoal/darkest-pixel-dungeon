package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator

class Doom : Buff(), Char.IIncomingDamageProc {
    init {
        type = buffType.NEGATIVE
    }

    override fun procIncommingDamage(damage: Damage) { damage.value *= 2 }

    override fun fx(on: Boolean) {
        if (on) target.sprite.add(CharSprite.State.DARKENED)
        else if (target.invisible == 0) target.sprite.remove(CharSprite.State.DARKENED)
    }

    override fun icon(): Int = BuffIndicator.CORRUPT

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc")
}