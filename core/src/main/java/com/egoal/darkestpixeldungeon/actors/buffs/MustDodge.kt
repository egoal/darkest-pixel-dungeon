package com.egoal.darkestpixeldungeon.actors.buffs

import android.util.Log

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

import javax.microedition.khronos.opengles.GL

/**
 * Created by 93942 on 8/2/2018.
 */

//* buff checked in Char::checkHit
class MustDodge : FlavourBuff() {
    private var dodgeType = 0

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DODGE_TYPE, dodgeType)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        dodgeType = bundle.getInt(DODGE_TYPE)
    }

    fun addDodgeType(t: Damage.Type): MustDodge {
        dodgeType = dodgeType or type2int(t)
        return this
    }

    fun addDodgeTypeAll(): MustDodge {
        dodgeType = 0x07
        return this
    }

    private fun type2int(t: Damage.Type): Int {
        when (t) {
            Damage.Type.NORMAL -> return 0x01
            Damage.Type.MAGICAL -> return 0x02
            Damage.Type.MENTAL -> return 0x04
            else -> return 0x00
        }
    }

    // check type
    fun canDodge(dmg: Damage): Boolean {
        return dodgeType and type2int(dmg.type) != 0
    }

    override fun icon(): Int {
        return BuffIndicator.MUST_DODGE
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dodgeType, dispTurns())
    }

    companion object {

        private val DODGE_TYPE = "dodge_type"
    }
}
