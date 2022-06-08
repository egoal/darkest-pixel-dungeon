package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.watabou.utils.Random

class ShieldAbility : Ability() {
    override fun onInitialize(belonger: Mob) {
        belonger.SHLD += belonger.HT / 2
    }
}

class HealthAbility : Ability() {
    override fun onInitialize(belonger: Mob) {
        belonger.HT += (belonger.HT * Random.Float(0.2f, 0.5f)).toInt()
    }
}

class CritAbility : Ability() {
    override fun onReady(belonger: Mob) {
        belonger.criticalChance += 0.1f
        belonger.criticalRatio += 0.25f
    }
}