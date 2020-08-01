package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class InvisibleBlade : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.INVISIBLE_BLADE

        tier = 3

        ACC = 1.25f
    }

    override fun max(lvl: Int): Int = 5 * tier + lvl * tier  // 20+ 4x lvl -> 15+ 3x lvl

    override fun proc(dmg: Damage): Damage {
        if (Random.Float() < 0.2f + 0.04f * level()) dmg.addFeature(Damage.Feature.PURE)

        return super.proc(dmg)
    }
}