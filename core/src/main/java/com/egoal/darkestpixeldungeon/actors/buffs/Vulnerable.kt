package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.round

/**
 * Created by 93942 on 8/3/2018.
 */

// check in Char::takeDamage
open class Vulnerable(var ratio: Float = 1f, var dmgType: Damage.Type = Damage.Type.NORMAL) : FlavourBuff() {
    init {
        type = buffType.NEGATIVE
    }

    override fun icon(): Int = BuffIndicator.VULERABLE

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, if (ratio < 1f) "desc_1" else "desc_0", dmgType.toString(), ratio, dispTurns())

    open fun procDamage(dmg: Damage) {
        if (dmg.type == dmgType) dmg.value = round(dmg.value * ratio).toInt()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(RATIO, ratio)
        bundle.put(DAMAGE_TYPE, dmgType)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        ratio = bundle.getFloat(RATIO)
        dmgType = bundle.getEnum(DAMAGE_TYPE, Damage.Type::class.java)
    }

    companion object {
        const val DURATION = 10f

        private const val RATIO = "ratio"
        private const val DAMAGE_TYPE = "type"
    }

}
