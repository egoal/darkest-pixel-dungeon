package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.watabou.utils.Random

class ExtraShield : Ability() {
    override fun onInitialize(belonger: Mob) {
        belonger.SHLD += belonger.HT / 2
    }
}

class ExtraHealth : Ability() {
    override fun onInitialize(belonger: Mob) {
        belonger.HT += (belonger.HT * Random.Float(0.2f, 0.5f)).toInt() + 1
        belonger.HP = belonger.HT
    }
}

class ExtraAttackSpeed : Ability() {
    override fun onAttack(belonger: Mob, damage: Damage) {
        belonger.spend(-.2f) // extra attack speed is enough for a double strike.
    }
}
