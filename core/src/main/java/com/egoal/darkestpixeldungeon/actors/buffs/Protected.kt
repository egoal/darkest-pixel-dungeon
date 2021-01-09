package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.items.unclassified.BrokenSeal

class Protected : Buff() {
    override fun act(): Boolean {
        if (target.isAlive) {
            if (target.SHLD > shieldCap())
                target.SHLD -= 1

            spend(STEP)

        } else diactivate()

        return true
    }

    private fun shieldCap() = target.buff(BrokenSeal.WarriorShield::class.java)?.maxShield() ?: 0

    companion object {
        private const val STEP = 3f
    }
}