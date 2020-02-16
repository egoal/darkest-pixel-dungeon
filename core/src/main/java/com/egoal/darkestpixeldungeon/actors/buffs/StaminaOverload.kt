package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.min

class StaminaOverload(private var supply: Int = 0) : Buff() {
    init {
        type = buffType.POSITIVE
    }

    var fromBundle = false //fixme: 

    override fun attachTo(target: Char): Boolean = if (super.attachTo(target)) {
        if (fromBundle) {
            target.HT += supply
            target.HP += supply
        }
        true
    } else false

    override fun act(): Boolean {
        if (supply <= 0) detach()

        supply--
        target.HT--
        target.HP = min(target.HT, target.HP)

        spend(1f)

        return true
    }

    override fun icon(): Int = BuffIndicator.MENDING

    override fun toString(): String = Messages.get(this, "name")

    override fun desc(): String = Messages.get(this, "desc", supply)

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(SUPPLY, supply)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        supply = bundle.getInt(SUPPLY)
        fromBundle = supply > 0
    }

    companion object {
        private const val SUPPLY = "supply"
    }

}