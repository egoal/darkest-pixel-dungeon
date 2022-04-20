package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.Mob

open class Ability {
    open fun onReady(belonger: Mob) {}

    open fun procGivenDamage(belonger: Mob, damage: Damage) {}

    open fun onAttack(belonger: Mob, damage: Damage) {}

    open fun onDefend(belonger: Mob, damage: Damage) {}

    /**
     * @return false to refuse death
     */
    open fun onDying(belonger: Mob): Boolean = true
}