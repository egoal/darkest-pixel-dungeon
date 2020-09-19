package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.round

class FastRecovery : Buff() {
    init {
        type = buffType.POSITIVE
    }

    var speed = 1
    var limit = 0.5f

    override fun act(): Boolean {
        target.HP += speed

        if (target.HP >= round(target.HT * limit))
            detach()
        spend(TICK)

        return true
    }

    override fun icon(): Int = BuffIndicator.FAST_RECOVERY
    override fun toString(): String = M.L(this, "name")
    override fun desc(): String = M.L(this, "desc", speed, round(limit * 100f).toInt())

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(SPEED, speed)
        bundle.put(LIMIT, limit)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        speed = bundle.getInt(SPEED)
        limit = bundle.getFloat(LIMIT)
    }

    companion object {
        private const val SPEED = "speed"
        private const val LIMIT = "limit"
    }
}