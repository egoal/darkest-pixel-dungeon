package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

/**
 * Created by 93942 on 8/16/2018.
 */

class CrystalsSwords : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.DPD_CRYSTALS_SWORDS

        tier = 3
    }

    // 16 + 3*lvl
    override fun max(lvl: Int): Int = 4 * (tier + 1) + lvl * tier

    override fun giveDamage(hero: Hero, target: Char): Damage {
        val dmg = super.giveDamage(hero, target)

        // chance to deal 2 times damage
        val c = .15f + .35f * (1f - Math.pow(.7, level() / 3.0).toFloat())

        if (Random.Float() < c) {
            dmg.value *= 2
            dmg.addFeature(Damage.Feature.CRITICAL)
        }

        return dmg
    }

}
