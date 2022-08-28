package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Protection
import com.egoal.darkestpixeldungeon.items.unclassified.BrokenSeal

class Protected : Buff() {
    var par_ = 0f

    override fun act(): Boolean {
        if (!target.isAlive)
            diactivate()
        else {
            if (target.SHLD >= shieldCap())
                target.SHLD -= 1
            else {
                par_ += shieldReg()
                while (par_ >= 1) {
                    --par_
                    ++target.SHLD
                }
            }

            spend(STEP)
        }

        return true
    }

    fun shieldCap(): Int {
        val hero = target as Hero
        return  hero.MSHLD+ (hero.belongings.armor?.SHLD()?:0)
    }

    private fun shieldReg(): Float {
        var reg = .1f
        target.buff(BrokenSeal.WarriorShield::class.java)?.let {
            reg += it.regShiled()
        }
        return reg
    }

    companion object {
        private const val STEP = 3f
    }
}