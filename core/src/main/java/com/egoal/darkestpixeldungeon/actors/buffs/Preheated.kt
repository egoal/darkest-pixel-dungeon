package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import kotlin.math.round

class Preheated : FlavourBuff() {
    init {
        type = buffType.POSITIVE
    }

    override fun icon(): Int = BuffIndicator.MARK

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns())

    fun affectWandDamage(damage: Damage) {
        damage.addFeature(Damage.Feature.CRITICAL or Damage.Feature.ACCURATE)
        damage.scale(1.25f)

        detach()
    }
}