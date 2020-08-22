package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class ShortSpear : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.SHORTSPEAR

        tier = 1
        RCH = 2
    }

    // 1+ x => 2+ x
    override fun min(lvl: Int): Int = tier + 1 + lvl

    // 8+ 2x => 8+ x
    override fun max(lvl: Int): Int = 4 * (tier + 1) + lvl * tier

}