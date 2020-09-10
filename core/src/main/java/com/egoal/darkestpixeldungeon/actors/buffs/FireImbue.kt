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
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class FireImbue : Buff() {
    private var left: Float = 0f
    init {
        immunities.add(Burning::class.java)
    }

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
        if (Dungeon.level.map[target.pos] == Terrain.GRASS) {
            Level.set(target.pos, Terrain.EMBERS)
            GameScene.updateMap(target.pos)
        }

        spend(TICK)
        left -= TICK
        if (left <= 0)
            detach()

        return true
    }

    fun proc(enemy: Char) {
        if (Random.Int(2) == 0)
            Buff.affect(enemy, Burning::class.java).reignite(enemy)

        enemy.sprite.emitter().burst(FlameParticle.FACTORY, 2)
    }

    override fun icon(): Int = BuffIndicator.FIRE

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", dispTurns(left))

    companion object {

        val DURATION = 30f

        private val LEFT = "left"
    }
}
