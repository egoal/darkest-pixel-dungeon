package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class InvisibleBlade : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.INVISIBLE_BLADE

        tier = 3

        ACC = 1.225f
    }

    override fun max(lvl: Int): Int = 5 * tier + lvl * (tier + 1) // scale unchanged.
}