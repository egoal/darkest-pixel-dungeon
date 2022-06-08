package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Protection
import com.egoal.darkestpixeldungeon.items.unclassified.BrokenSeal

class Protected : Buff() {
    var _par = 0f

    override fun act(): Boolean {
        if (!target.isAlive)
            diactivate()
        else {
            if (target.SHLD > shieldCap())
                target.SHLD -= 1
            else {
                _par += shieldReg()
                while (_par >= 1) {
                    --_par
                    ++target.SHLD
                }
            }

            spend(STEP)
        }

        return true
    }

    private fun shieldCap(): Int {
        val hero = target as Hero

        var shld = hero.MSHLD
        if (hero.belongings.armor?.glyph is Protection) {
            shld += (hero.belongings.armor!!.glyph as Protection).Shield(hero.belongings.armor!!)
        }

        shld += (target.buff(BrokenSeal.WarriorShield::class.java)?.maxShield() ?: 0)

        return shld
    }

    private fun shieldReg() = 0.1f + if (target.buff(BrokenSeal.WarriorShield::class.java) != null) 0.1f else 0f

    companion object {
        private const val STEP = 3f
    }
}