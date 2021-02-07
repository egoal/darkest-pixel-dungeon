package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class RangersHook : Artifact() {
    init {
        image = ItemSpriteSheet.RANGERS_HOOK


    }

    override fun passiveBuff(): ArtifactBuff = Recharge()

    inner class Recharge : ArtifactBuff() {}
}