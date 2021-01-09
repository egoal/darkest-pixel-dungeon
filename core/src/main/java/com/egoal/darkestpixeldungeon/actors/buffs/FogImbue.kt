package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.WhiteFog
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

class FogImbue : Buff() {
    private var left: Float = 0f

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEFT, left)

    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        left = bundle.getFloat(LEFT)
    }

    fun set(duration: Float) {
        this.left = duration
    }


    override fun act(): Boolean {
        GameScene.add(Blob.seed(target.pos, 50, WhiteFog::class.java))

        spend(Actor.TICK)
        left -= Actor.TICK
        if (left <= 0)
            detach()

        return true
    }

    override fun icon(): Int = BuffIndicator.IMMUNITY

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns(left))

    init {
        immunities.add(WhiteFog::class.java)
    }

    companion object {
        const val DURATION = 30f

        private const val LEFT = "left"
    }
}
