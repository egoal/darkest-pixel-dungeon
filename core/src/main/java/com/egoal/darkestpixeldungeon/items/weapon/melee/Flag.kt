package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class Flag : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.FLAG
        tier = 3
        RCH = 2
        DLY = 1.5f
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) + 1

    override fun max(lvl: Int): Int = 20 + 5 * lvl

    override fun defendDamage(dmg: Damage): Damage {
        val l = level() + 1
        val def = if (dmg.isFeatured(Damage.Feature.RANGED)) 2 * l else l
        dmg.value -= Random.IntRange(1, def)

        return super.defendDamage(dmg)
    }
}