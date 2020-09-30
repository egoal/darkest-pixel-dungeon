package com.egoal.darkestpixeldungeon.actors.buffs

import android.util.Log

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

import javax.microedition.khronos.opengles.GL

/**
 * Created by 93942 on 8/2/2018.
 */

//* buff checked in Char::checkHit
class MustDodge : FlavourBuff() {
    init {
        type = buffType.POSITIVE
    }

    override fun icon(): Int = BuffIndicator.MUST_DODGE

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns())
}
