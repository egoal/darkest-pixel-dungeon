package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.*

abstract class TimingPerk(private val timing: Class<out Timing>,
                          maxlevel: Int = 1, level: Int = 1) : Perk(maxlevel, level) {
    override fun onGain() {
        Buff.affect(Dungeon.hero, timing)
    }

    override fun upgrade() {
        Dungeon.hero.buff(timing)!!.upgrade()
        super.upgrade()
    }

    override fun onLose() {
        Buff.detach(Dungeon.hero, timing)
    }

    abstract class Timing(protected var time: Float) : Buff() {
        fun upgrade() {}

        abstract fun trigger()

        override fun act(): Boolean {
            trigger()
            spend(time)
            return true
        }
    }
}
