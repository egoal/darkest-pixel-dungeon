package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random
import kotlin.math.round

class LongSpear : MeleeWeapon() {
    var crippleRatio = .2f

    init {
        image = ItemSpriteSheet.LONG_SPEAR

        tier = 5
        DLY = 1.5f
        RCH = 2
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) - 1

    override fun max(lvl: Int): Int = round(super.max(lvl) * 1.2f).toInt()

    override fun proc(dmg: Damage): Damage {
        if (dmg.to is Mob) {
            val defender = dmg.to as Mob
            if (Random.Float() < crippleRatio) {
                Buff.prolong(defender, Cripple::class.java, 2f + level())
                crippleRatio = .2f
            } else crippleRatio += .05f
        }

        return super.proc(dmg)
    }
}