package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.max

class Senile(var ratio: Float = 0f) : FlavourBuff() {
    init {
        type = buffType.NEGATIVE
    }

    override fun icon(): Int = BuffIndicator.CORRUPT

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", (ratio * 100).toInt(), dispTurns())

    override fun attachTo(target: Char): Boolean {
        val attached = super.attachTo(target)
        if (attached) {
            val r = target.HP.toFloat() / target.HT.toFloat()
            target.HT = max(1, (target.HT * (1f - ratio)).toInt())
            target.HP = max(1, (target.HT * r).toInt())
        }

        return attached
    }

    override fun detach() {
        super.detach()
        if (target.isAlive) {
            val r = target.HP.toFloat() / target.HT.toFloat()
            target.HT = max(1, (target.HT / (1f - ratio)).toInt())
            target.HP = max(1, (target.HT * r).toInt())
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(RATIO, ratio)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        ratio = bundle.getFloat(RATIO)
    }

    companion object {
        const val DURATION = 10f
        private const val RATIO = "ratio"
    }
}