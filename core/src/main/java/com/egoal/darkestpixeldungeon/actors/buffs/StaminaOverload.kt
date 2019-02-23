package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.min

class StaminaOverload(private var supply: Int = 1) : Buff() {
    init {
        type = buffType.POSITIVE
    }

    override fun attachTo(target: Char): Boolean = if (super.attachTo(target)) {
        target.HT += supply
        target.HP += supply
        true
    } else false

    override fun act(): Boolean {
        supply--
        target.HT--
        target.HP = min(target.HT, target.HP)
        if (supply <= 0)
            detach()

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
    }

    companion object {
        private const val SUPPLY = "supply"
    }

}