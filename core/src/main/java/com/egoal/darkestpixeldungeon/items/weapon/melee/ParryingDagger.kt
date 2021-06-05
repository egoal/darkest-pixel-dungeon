package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class ParryingDagger : Dagger() {
    init {
        image = ItemSpriteSheet.BLOCK_DAGGER
    }

    // 1 ~ 7, from 0 ~ 9
    override fun min(lvl: Int): Int = lvl + 1

    override fun max(lvl: Int): Int = 3 * (tier + 1) + 1 + lvl * (tier + 1)

    override fun defendDamage(dmg: Damage): Damage {
        if (dmg.type == Damage.Type.NORMAL)
            dmg.value -= 1
        return super.defendDamage(dmg)
    }
}