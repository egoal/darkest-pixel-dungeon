package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

private const val STR_LEVEL = "level"

abstract class Perk(val maxLevel: Int = 1, var level: Int = 1) : Bundlable {
    open fun description(): String = M.L(this, "desc")

    open fun image(): Int = 0

    open fun upgradable(): Boolean = level < maxLevel

    open fun upgrade() {
        assert(upgradable())
        level += 1
    }

    open fun onGain(){}

    open fun onLose(){}

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(STR_LEVEL, level)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        level = bundle.getInt(STR_LEVEL)
    }
}