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
package com.egoal.darkestpixeldungeon.sprites

import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.watabou.noosa.tweeners.AlphaTweener
import com.watabou.noosa.tweeners.ScaleTweener
import com.watabou.utils.PointF
import com.watabou.utils.Random

open class MobSprite : CharSprite() {
    override fun update() {
        sleeping = hasChar && (ch as Mob).state === (ch as Mob).SLEEPING
        super.update()
    }

    override fun onComplete(anim: Animation) {
        super.onComplete(anim)

        if (anim === die) {
            parent.add(object : AlphaTweener(this, 0f, FADE_TIME) {
                override fun onComplete() {
                    this@MobSprite.killAndErase()
                    parent.erase(this)
                }
            })
        }
    }

    fun fall() {
        origin.set(width / 2, height - DungeonTilemap.SIZE / 2)
        angularSpeed = (if (Random.Int(2) == 0) -720 else 720).toFloat()

        parent.add(object : ScaleTweener(this, PointF(0f, 0f), FALL_TIME) {
            override fun onComplete() {
                this@MobSprite.killAndErase()
                parent.erase(this)
            }

            override fun updateValues(progress: Float) {
                super.updateValues(progress)
                am = 1 - progress
            }
        })
    }

    companion object {
        private const val FADE_TIME = 3f
        private const val FALL_TIME = 1f
    }
}
