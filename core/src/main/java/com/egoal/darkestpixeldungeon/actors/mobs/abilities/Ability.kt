package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

open class Ability : Bundlable {
    fun prefix(): String = M.L(this, "prefix")

    // call only once, when the mob was created.
    open fun onInitialize(belonger: Mob) {}

    /**
     * call: 1. after initialization, 2. each time restored
     */
    open fun onReady(belonger: Mob) {}

    open fun procGivenDamage(belonger: Mob, damage: Damage) {}

    open fun onAttack(belonger: Mob, damage: Damage) {}

    open fun onDefend(belonger: Mob, damage: Damage) {}

    /**
     * return false to refuse death
     */
    open fun onDying(belonger: Mob): Boolean = true

    override fun storeInBundle(bundle: Bundle) {}
    override fun restoreFromBundle(bundle: Bundle) {}
}