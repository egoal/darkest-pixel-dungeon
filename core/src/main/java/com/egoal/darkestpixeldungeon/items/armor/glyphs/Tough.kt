package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.Bundle

class Tough : Armor.Glyph() {
    private var energy_ = ENERGY_CAP

    fun resist(buff: Buff): Boolean {
        if (buff.type != Buff.buffType.NEGATIVE) return false

        return if (energy_ >= ENERGY_CAP) {
            energy_ = 0
            true
        } else false
    }

    override fun proc(armor: Armor, damage: Damage): Damage {
        if (energy_ < ENERGY_CAP && damage.value > 0) energy_ += 1

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = GREEN

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ENERGY, energy_)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        energy_ = bundle.getInt(ENERGY)
    }

    companion object {
        private val GREEN = ItemSprite.Glowing(0x0AD02D)

        private const val ENERGY_CAP = 10
        private const val ENERGY = "energy"
    }
}