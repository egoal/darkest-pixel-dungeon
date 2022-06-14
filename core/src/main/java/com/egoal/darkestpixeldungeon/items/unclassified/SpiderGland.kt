package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.Web
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class SpiderGland : Item() {
    init {
        image = ItemSpriteSheet.GLAND

        stackable = true
        usesTargeting = true

        defaultAction = AC_THROW
    }

    override val isUpgradable: Boolean
        get() = false

    override val isIdentified: Boolean
        get() = true

    override fun random(): Item = this.apply {
        quantity = if (Random.Float() < .3f) 2 else 1
    }

    override fun onThrow(cell: Int) {
        if (Level.pit[cell]) super.onThrow(cell)
        else {
            Dungeon.level.press(cell, null)
            GameScene.add(Blob.seed(cell, Random.Int(12, 20), Web::class.java))
            GameScene.add(Blob.seed(cell, 30, ToxicGas::class.java))
        }
    }
}