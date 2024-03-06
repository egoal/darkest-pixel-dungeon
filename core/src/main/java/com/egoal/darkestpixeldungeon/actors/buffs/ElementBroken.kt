package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.round

class ElementBroken : FlavourBuff(), Char.IIncomingDamageProc {
    private val ratios = FloatArray(Damage.Element.values().size) { 1f }

    init {
        type = buffType.NEGATIVE
    }

    fun add(element: Damage.Element, ratio: Float): ElementBroken {
        ratios[element.ordinal] = ratio
        return this
    }

    override fun icon(): Int = BuffIndicator.ELEMENT_BROKEN

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String {
        val elestr = ratios.indices.filter { ratios[it] > 0 }.joinToString { Damage.Element.values()[it].textName }
        return M.L(this, "desc", elestr, dispTurns())
    }

    override fun procIncommingDamage(damage: Damage) {
        damage.value = round(damage.value * ratios[damage.element.ordinal]).toInt()
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