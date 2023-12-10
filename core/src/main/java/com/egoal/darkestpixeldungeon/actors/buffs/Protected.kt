package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.items.artifacts.TorsoOfTheElder
import com.egoal.darkestpixeldungeon.items.specials.KnightCore
import com.egoal.darkestpixeldungeon.items.unclassified.BrokenSeal
import kotlin.math.min

class Protected : Buff() {
    var par_ = 0f

    override fun act(): Boolean {
        if (!target.isAlive)
            diactivate()
        else {
            val cap = shieldCap()

            if (target.SHLD > cap)
                target.SHLD -= 1
            else {
                par_ += shieldReg()
                while (par_ >= 1) {
                    --par_
                    target.SHLD = min(target.SHLD+ 1, cap)
                }
            }

            spend(STEP)
        }

        return true
    }

    fun shieldCap(): Int {
        val hero = target as Hero
        var m = hero.MSHLD + (hero.belongings.armor?.SHLD() ?: 0)
        if(hero.subClass== HeroSubClass.KNIGHTT) m += hero.belongings.getSpecial(KnightCore::class.java)!!.SHLD
        return m
    }

    private fun shieldReg(): Float {
        var reg = .1f
        if ((target as Hero).subClass == HeroSubClass.KNIGHTT) reg += .1f;

        target.buff(BrokenSeal.WarriorShield::class.java)?.let {
            reg += it.regShiled()
        }

        target.buff(TorsoOfTheElder.HealthChecker::class.java)?.let {
            reg += .05f
        }

        return reg
    }

    companion object {
        private const val STEP = 3f
    }
}