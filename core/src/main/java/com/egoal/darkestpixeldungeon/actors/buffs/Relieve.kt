package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.max

class Relieve : Buff() {
    init {
        type = buffType.POSITIVE
    }

    private var left_ = 0f

    fun prolong(duration: Float) {
        left_ = max(left_, duration)
    }

    override fun icon(): Int = BuffIndicator.RELIEVE

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", left_.toInt())

    override fun act(): Boolean {
        if (target is Hero) (target as Hero).recoverSanity(0.3f)

        spend(1f)
        left_ -= 1f
        if (left_ <= 0f) detach()

        return true
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEFT, left_)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        left_ = bundle.getFloat(LEFT)
    }

    companion object {
        private const val LEFT = "left"
    }
}