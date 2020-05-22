package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.max
import kotlin.math.min

class VampiricBite : Buff() {
    init {
        type = buffType.SILENT
    }

    private var killed = 0

    override fun icon(): Int = BuffIndicator.BLOOD_SUCK

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc")

    fun onEnemySlayed(c: Char) {
        if ((++killed) % 5 == 0) target.HT += 1

        target.HP = min(max(1, c.HT / 10) + target.HP, target.HT)
        target.sprite.emitter().start(Speck.factory(Speck.HEALING), .5f, 1)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(KILLCOUNT, killed)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        killed = bundle.getInt(KILLCOUNT)
    }

    companion object {
        private const val KILLCOUNT = "killed"
    }
}