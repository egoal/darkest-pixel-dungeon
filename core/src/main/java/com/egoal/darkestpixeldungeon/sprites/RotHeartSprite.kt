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

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.Assets
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.particles.Emitter

class RotHeartSprite : MobSprite() {
    private var cloud: Emitter? = null

    init {
        texture(Assets.ROT_HEART)

        val frames = TextureFilm(texture, 16, 16)

        idle = Animation(1, true)
        idle.frames(frames, 0)

        run = Animation(1, true)
        run.frames(frames, 0)

        attack = Animation(1, false)
        attack!!.frames(frames, 0)

        die = Animation(8, false)
        die.frames(frames, 1, 2, 3, 4, 5, 6, 7, 7, 7)

        play(idle)
    }

    override fun link(ch: Char) {
        super.link(ch)

        if (cloud == null) {
            cloud = emitter()
            cloud!!.pour(Speck.factory(Speck.TOXIC), 0.7f)
        }
    }

    override fun turnTo(from: Int, to: Int) {
        //do nothing
    }

    override fun update() {
        super.update()

        cloud?.visible = visible
    }

    override fun die() {
        super.die()

        cloud?.on = false
    }
}
