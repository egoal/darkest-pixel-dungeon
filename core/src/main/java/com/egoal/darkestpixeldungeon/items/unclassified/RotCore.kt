package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.Web
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class RotCore : Item() {
    init {
        image = ItemSpriteSheet.ROT_CORE
        stackable = true
        usesTargeting = true

        defaultAction = AC_THROW
    }

    override val isUpgradable: Boolean
        get() = false

    override val isIdentified: Boolean
        get() = true

    override fun onThrow(cell: Int) {
        if (Level.pit[cell]) super.onThrow(cell)
        else {
            Dungeon.level.press(cell, null)
            Actor.findChar(cell)?.let {
                if (it.SHLD > 0) {
                    it.sprite.showStatus(CharSprite.NEGATIVE, M.L(this, "shield"))
                    it.SHLD = 0
                }
                Buff.affect(it, Poison::class.java).set(Random.Float(5f, 8f))
            }
            GameScene.add(Blob.seed(cell, Random.Int(12, 20), Web::class.java))
            GameScene.add(Blob.seed(cell, 30, ToxicGas::class.java))
        }
    }
}