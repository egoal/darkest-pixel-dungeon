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

import android.util.Log
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.features.Luminary
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.max
import kotlin.math.min

class Light : Buff() {
    var duration = 0f
    lateinit var luminary: Luminary

    fun prolong(dt: Float) {
        duration = max(dt, duration)
    }

    override fun onAdd() {
        if (Dungeon.level != null) {
            luminary = TorchLight(target.pos, target.id())
            Dungeon.level.addLuminary(luminary)
            Dungeon.level.updateLightMap()
        }
    }

    override fun attachTo(target: Char): Boolean {
        return if (super.attachTo(target)) {
            Dungeon.observe()
            true
        } else {
            false
        }
    }

    override fun act(): Boolean {
        duration -= Actor.TICK
        if (duration <= 0) detach()
        else {
            luminary.pos = target.pos
        }

        spend(Actor.TICK)
        return true
    }

    override fun detach() {
        super.detach()
        Dungeon.level.removeLuminary(luminary)
        Dungeon.observe()
    }

    override fun icon(): Int {
        return BuffIndicator.LIGHT
    }

    override fun fx(on: Boolean) {
        if (on) target.sprite.add(CharSprite.State.ILLUMINATED)
        else target.sprite.remove(CharSprite.State.ILLUMINATED)
    }

    override fun toString(): String = Messages.get(this, "name")

    override fun desc(): String = Messages.get(this, "desc", dispTurns(duration))

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DURATIONSTR, duration)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        duration = bundle.getFloat(DURATIONSTR)
    }

    companion object {
        const val DURATION = 300f // default duration

        private const val DURATIONSTR = "duration"
        private const val DISTANCE = 5
    }

    class TorchLight(pos: Int, val id: Int) : Luminary(pos) {
        override fun light(level: Level) {
            val pt = level.cellToPoint(pos)
            val sx = max(0, pt.x - DISTANCE)
            val ex = min(level.width() - 1, pt.x + DISTANCE)
            val sy = max(0, pt.y - DISTANCE)
            val ey = min(level.height() - 1, pt.y + DISTANCE)

            for (y in sy..ey) Level.lighted.fill(true, level.xy2cell(sx, y), level.xy2cell(ex, y))
        }

        // no visuals: halo is handled by sprite.
        override fun createVisual(): LightVisual? = null
    }

}
