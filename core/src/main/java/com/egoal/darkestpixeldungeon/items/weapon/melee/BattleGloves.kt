package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import kotlin.math.pow

/**
 * Created by 93942 on 8/16/2018.
 */

class BattleGloves : MeleeWeapon() {

    init {
        image = ItemSpriteSheet.DPD_BATTLE_GLOVES

        tier = 1
        DLY = 0.85f  // faster speed
        // ACC		=	1.2f;	// 20% boost to accuracy
    }

    // 3+2*x, lower base damage
    override fun max(lvl: Int): Int = 3 * tier + lvl * (tier + 1)

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) - 2

    override fun speedFactor(hero: Hero): Float = super.speedFactor(hero) * 0.9f.pow(level())
}
