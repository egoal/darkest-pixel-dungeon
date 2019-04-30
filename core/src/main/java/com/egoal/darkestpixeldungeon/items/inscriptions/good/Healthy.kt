package com.egoal.darkestpixeldungeon.items.inscriptions.good

import com.egoal.darkestpixeldungeon.items.inscriptions.Inscription
import com.egoal.darkestpixeldungeon.sprites.ItemSprite


class Healthy : Inscription() {

    private var ratio = 0.05

    override fun mhpFix(mhp: Int): Int {
        return mhp + extra(mhp).toInt()
    }

    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0xff0000) // red

    private fun extra(mhp: Int) = mhp * ratio
}
