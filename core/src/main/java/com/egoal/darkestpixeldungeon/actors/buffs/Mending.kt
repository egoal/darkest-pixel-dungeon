package com.egoal.darkestpixeldungeon.actors.buffs

import android.widget.GridLayout

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle

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
        val v = Math.ceil((recoveryValue / 4f).toDouble()).toInt()
        recoveryValue -= v
        if (v <= 0.1) {
            detach()
        } else {
            target.HP = Math.min(target.HT, target.HP + Math.max(v, 1))
            target.sprite.emitter().start(Speck.factory(Speck.HEALING), .4f, 4)
        }

        spend(1f)

        return true
    }

    override fun icon(): Int {
        return BuffIndicator.MENDING
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", recoveryValue)
    }

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
