package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class Tulwar : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.TULWAR

        tier = 2
    }

    // 15+ 3x => 12+ 3x
    override fun max(lvl: Int): Int = 4 * (tier + 1) + lvl * (tier + 1)

    override fun proc(dmg: Damage): Damage {
        if (dmg.isFeatured(Damage.Feature.CRITICAL)) {
            val defender = dmg.to as Char

            Buff.affect(defender, Bleeding::class.java).set(4 + 2 * level())
        }

        return super.proc(dmg)
    }
}