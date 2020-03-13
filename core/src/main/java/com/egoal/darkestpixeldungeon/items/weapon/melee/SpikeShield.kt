package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class SpikeShield : Shield() {
    init {
        image = ItemSpriteSheet.SPIKE_SHIELD
        tier = 4
        DLY = 1.5f // slow
    }

    override fun max(lvl: Int): Int = 3 * (tier + 1) + lvl * tier

    override fun def(level: Int): Int = 4 + 2 * level // like a round shield

    override fun defendValue(dmg: Damage, defValue: Int): Damage {
        if (dmg.from is Char && Dungeon.level.adjacent((dmg.from as Char).pos, (dmg.to as Char).pos)) {
            val damage = proc(Damage(defValue, dmg.to, dmg.from))
            val defender = dmg.from as Char
            defender.takeDamage(damage)
        }
        return super.defendValue(dmg, defValue)
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) + 1
}