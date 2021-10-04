package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Vorpal
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class ButchersKnife : MeleeWeapon() {
    val vorpal = Vorpal()
    init {
        image = ItemSpriteSheet.BUTCHERS_KNIFE

        tier = 2
    }

    // 15+ 3x -> 9+ 3x
    override fun max(lvl: Int): Int = 3 * (tier + 1) + lvl * (tier + 1)

    override fun proc(dmg: Damage): Damage {
        vorpal.proc(this, dmg)
        return super.proc(dmg).apply {
            if (dmg.from is Hero) {
                val ex = (dmg.from as Hero).lvl
                value += Random.IntRange(1, ex)
            }
        }
    }
}