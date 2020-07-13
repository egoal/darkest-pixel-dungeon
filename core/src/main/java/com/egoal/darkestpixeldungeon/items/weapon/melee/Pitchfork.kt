package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random
import kotlin.math.min

class Pitchfork : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.PITCHFORK

        tier = 4
        DLY = 1.5f
        RCH = 2
    }

    // x1.33
    override fun max(lvl: Int): Int = (tier + 1) * 20 / 3 + lvl * (tier + 1) * 4 / 3

    override fun proc(dmg: Damage): Damage {
        if (Random.Int(5) == 0)
            Buff.affect(dmg.to as Char, Bleeding::class.java).set(min(10, dmg.value / 4))

        return super.proc(dmg)
    }
}