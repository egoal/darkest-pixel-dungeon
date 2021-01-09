/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle

class ToxicImbue : Buff() {
    protected var left: Float = 0.toFloat()

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
        GameScene.add(Blob.seed(target.pos, 50, ToxicGas::class.java))

        spend(Actor.TICK)
        left -= Actor.TICK
        if (left <= 0)
            detach()

        return true
    }

    override fun icon(): Int {
        return BuffIndicator.IMMUNITY
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns(left))
    }

    init {
        immunities.add(ToxicGas::class.java)
        immunities.add(Poison::class.java)
    }

    companion object {
        const val DURATION = 30f

        private const val LEFT = "left"
    }
}
