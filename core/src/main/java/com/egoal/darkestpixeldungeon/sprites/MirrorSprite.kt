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
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.MirrorImage
import com.watabou.noosa.TextureFilm

class MirrorSprite : MobSprite() {
    init {
        texture(Dungeon.hero.heroClass.spritesheet())
        updateArmor(0)
        idle()
    }

    override fun link(ch: Char) {
        super.link(ch)
        updateArmor((ch as MirrorImage).tier)
    }

    fun updateArmor(tier: Int) {
        val film = TextureFilm(HeroSprite.tiers(), tier, FRAME_WIDTH, FRAME_HEIGHT)

        idle = Animation(1, true)
        idle.frames(film, 0, 0, 0, 1, 0, 0, 1, 1)

        run = Animation(20, true)
        run.frames(film, 2, 3, 4, 5, 6, 7)

        die = Animation(20, false)
        die.frames(film, 0)

        attack = Animation(15, false)
        attack.frames(film, 13, 14, 15, 0)

        idle()
    }

    companion object {
        private const val FRAME_WIDTH = 12
        private const val FRAME_HEIGHT = 15
    }
}
