package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

/**
 * Created by 93942 on 8/3/2018.
 */

//* check in Char::takeDamage
class LifeLink : FlavourBuff() {

    var linker = 0

    init {
        type = Buff.buffType.POSITIVE
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LINKER, linker)

    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        linker = bundle.getInt(LINKER)
    }

    override fun icon(): Int {
        return BuffIndicator.LIFE_LINK
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns())
    }

    companion object {

        private val LINKER = "linker"
    }
}
