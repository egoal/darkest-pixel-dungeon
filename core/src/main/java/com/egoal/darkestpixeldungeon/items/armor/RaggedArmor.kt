package com.egoal.darkestpixeldungeon.items.armor

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import kotlin.math.max

//todo: do not use tier 0
class RaggedArmor : Armor(0) {
    init {
        image = ItemSpriteSheet.ARMOR_RAGGED

        bones = false
    }

    //todo: seal this
    override fun DRMax(lvl: Int): Int {
        var effectiveTier = 1
        effectiveTier += glyph?.tierDRAdjust() ?: 0
        effectiveTier = max(0, effectiveTier)

        return max(DRMin(lvl), effectiveTier * (1 + lvl))
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) - 3

    override fun MRES(lvl: Int): Float = (9f + lvl) / 100f
}