package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Unstable
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.Image

/**
 * Created by 93942 on 4/23/2018.
 */

class SorceressWand : MeleeWeapon() {

    init {
        image = ItemSpriteSheet.DPD_SORCERESS_WAND
        tier = 1
        DLY = 1f
        unique = true

        // give enchantment
        enchant(Unstable::class.java, 30)
    }

    override fun min(lvl: Int): Int = super.min(lvl) + 1

    // 1~7
    override fun max(lvl: Int): Int = 4 * tier + 3 + lvl * (tier + 1)

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) - 1
}
