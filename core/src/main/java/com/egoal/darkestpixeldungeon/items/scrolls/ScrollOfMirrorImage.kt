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
package com.egoal.darkestpixeldungeon.items.scrolls

import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.MirrorImage
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList
import kotlin.math.min

class ScrollOfMirrorImage : Scroll() {
    init {
        initials = 4
    }

    override fun doRead() {
        val respawnPoints = PathFinder.NEIGHBOURS8
                .map { curUser.pos + it }
                .filter { Actor.findChar(it) == null && (Level.passable[it] || Level.avoid[it]) }
                .shuffled()

        repeat(min(NIMAGES, respawnPoints.size)) {
            val mob = MirrorImage().apply { duplicate(curUser) }
            GameScene.add(mob)
            ScrollOfTeleportation.appear(mob, respawnPoints[it])

            setKnown()
        }


        Sample.INSTANCE.play(Assets.SND_READ)
        Invisibility.dispel()

        readAnimation()
    }

    override fun price(): Int = if (isKnown) 30 * quantity else super.price()

    companion object {
        private const val NIMAGES = 3
    }
}
