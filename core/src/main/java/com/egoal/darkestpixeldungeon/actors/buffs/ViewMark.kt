package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

/**
 * Created by 93942 on 5/12/2018.
 */

class ViewMark : FlavourBuff() {

    var observer = 0

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(OBSERVER, observer)

    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        observer = bundle.getInt(OBSERVER)
    }

    override fun icon(): Int {
        return BuffIndicator.STARE
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc")
    }

    companion object {

        private val OBSERVER = "observer"
    }

}
