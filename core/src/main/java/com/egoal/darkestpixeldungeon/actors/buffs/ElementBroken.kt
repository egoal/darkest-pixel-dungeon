package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.round

class ElementBroken : FlavourBuff(), Char.IIncomingDamageProc {
    private val ratios = FloatArray(Damage.Element.ELEMENT_COUNT)

    init {
        type = buffType.NEGATIVE
    }

    fun add(element: Int, ratio: Float): ElementBroken {
        var ele = element
        var i = 0
        while (ele != 1) {
            ele = ele shr 1
            i++
        }
        if (i < Damage.Element.ELEMENT_COUNT) ratios[i] = ratio
        return this
    }

    override fun icon(): Int = BuffIndicator.ELEMENT_BROKEN

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String {
        val elestr = ratios.indices.filter { ratios[it] > 0 }.joinToString { Damage.Element.names[it] }
        return M.L(this, "desc", elestr, dispTurns())
    }

    override fun procIncommingDamage(damage: Damage) {
        var r = 1f
        for (i in 0 until Damage.Element.ELEMENT_COUNT) {
            val ele = 0x01 shl i
            if (ratios[i] != 0f && damage.hasElement(ele)) r *= ratios[i]
        }

        damage.value = round(damage.value * r).toInt()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("ratio", ratios)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        bundle.getFloatArray("ratio").copyInto(ratios)
    }
}