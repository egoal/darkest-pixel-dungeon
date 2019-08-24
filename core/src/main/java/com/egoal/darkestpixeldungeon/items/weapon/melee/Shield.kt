package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Random
import kotlin.math.round

abstract class Shield : MeleeWeapon() {
    override fun info(): String = if (isIdentified)
        super.info() + "\n\n" + M.L(Shield::class.java, "block", def(0), def(level()))
    else super.info()

    protected abstract fun def(level: Int): Int

    protected fun checkDefend(dmg: Damage): Boolean {
        if (dmg.from is Char && dmg.to is Char) {
            val attacker = dmg.from as Char
            val defender = dmg.to as Char
            return Random.Float(attacker.attackSkill(defender).toFloat()) <
                    Random.Float(defender.defenseSkill(attacker).toFloat())
        }

        return false
    }

    override fun defendDamage(dmg: Damage): Damage {
        return defendValue(dmg, if (checkDefend(dmg)) def(level()) else def(0))
    }

    protected fun defendValue(dmg: Damage, defValue: Int): Damage {
        var value = defValue
        if (dmg.to is Hero) {
            val burden = STRReq() - (dmg.to as Hero).STR()
            if (burden > 0) value -= 2 * burden
        }

        if (value > 0) {
            if (dmg.isFeatured(Damage.Feature.RANGED))
                value += round(value / 5f).toInt()

            if (dmg.type == Damage.Type.NORMAL)
                dmg.value -= value
            else if (dmg.type == Damage.Type.MAGICAL)
                dmg.value -= value * 2 / 3
        }

        return dmg
    }
}