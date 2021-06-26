package com.egoal.darkestpixeldungeon.items.weapon.inscriptions

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.watabou.utils.Random
import kotlin.math.max

class Heavy : Inscription(11) {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        // tier 5 STR 18 level +3:
        // add about 1~(16-10+3) = 1~9 points, which is more powerful than +4
        val str = max(1, weapon.STRReq() - 10 + weapon.level())
        damage.value += Random.Int(1, str)
        return damage
    }
}