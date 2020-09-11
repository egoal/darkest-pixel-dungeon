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
package com.egoal.darkestpixeldungeon.levels.traps

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList

class SummoningTrap : Trap() {

    init {
        color = TrapSprite.TEAL
        shape = TrapSprite.WAVES
    }

    override fun activate() {

        if (Dungeon.bossLevel()) {
            return
        }

        var nMobs = 1
        if (Random.Int(2) == 0) {
            nMobs++
            if (Random.Int(2) == 0) {
                nMobs++
            }
        }

        val candidates = ArrayList<Int>()

        for (i in PathFinder.NEIGHBOURS8.indices) {
            val p = pos + PathFinder.NEIGHBOURS8[i]
            if (Actor.findChar(p) == null && (Level.passable[p] || Level.avoid[p])) {
                candidates.add(p)
            }
        }

        val respawnPoints = ArrayList<Int>()

        while (nMobs > 0 && candidates.size > 0) {
            val index = Random.index(candidates)

            respawnPoints.add(candidates.removeAt(index))
            nMobs--
        }

        val mobs = ArrayList<Mob>()

        for (point in respawnPoints) {
            val mob = Bestiary.mob(Dungeon.depth)
            mob.state = mob.WANDERING
            mob.pos = point
            GameScene.add(mob, DELAY)
            mobs.add(mob)
        }

        //important to process the visuals and pressing of cells last, so spawned
        // mobs have a chance to occupy cells first
        for (mob in mobs) {
            ScrollOfTeleportation.appear(mob, mob.pos)
        }

    }

    companion object {

        private val DELAY = 2f
    }
}
