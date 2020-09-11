package com.egoal.darkestpixeldungeon.actors.buffs

import android.widget.GridLayout

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import kotlin.math.ceil
import kotlin.math.max

/**
 * Created by 93942 on 9/6/2018.
 */

class Mending : Buff() {
    var recoveryValue = 0

    init {
        type = buffType.POSITIVE
    }

    fun set(value: Int): Mending {
        recoveryValue = value
        return this
    }

    override fun attachTo(target: Char): Boolean {
        if (super.attachTo(target) && !target.immunizedBuffs().contains(Mending::class.java)) {
            if (target is Hero)
                GLog.p(Messages.get(this, "start_mending"))

            return true
        }

        return false
    }

    override fun act(): Boolean {
        val v = ceil(recoveryValue / 4f).toInt()
        recoveryValue -= v
        if (v <= 0.1) {
            detach()
        } else {
            target.recoverHP(max(v, 1), this)
        }

        spend(1f)

        return true
    }

    override fun icon(): Int = BuffIndicator.MENDING

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", recoveryValue)

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(RECOVERY_VALUE, recoveryValue)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        recoveryValue = bundle.getInt(RECOVERY_VALUE)
    }

    companion object {
        private const val RECOVERY_VALUE = "recovery_value"
    }
}
