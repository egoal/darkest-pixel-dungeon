package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class SpikeShield : Shield() {
    init {
        image = ItemSpriteSheet.SPIKE_SHIELD
        tier = 4
        DLY = 1.5f // slow
    }

    override fun def(level: Int): Int = 4 + 2 * level // like a round shield

    override fun defendDamage(dmg: Damage): Damage {
        val defend = checkDefend(dmg)
        val value = if (defend) def(level()) else def(0)
        if (defend) // if defend, then must be a Char
            (dmg.from as Char).takeDamage(proc(Damage(value, dmg.to, dmg.from)))

        return defendValue(dmg, value)
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) + 1
}