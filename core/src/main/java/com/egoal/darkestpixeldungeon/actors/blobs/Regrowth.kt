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
package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Roots
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.particles.LeafParticle
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.scenes.GameScene

class Regrowth : Blob() {

    override fun evolve() {
        super.evolve()

        if (volume > 0) {
            var cell: Int
            for (i in area.left until area.right) {
                for (j in area.top until area.bottom) {
                    cell = i + j * Dungeon.level.width()
                    if (off[cell] > 0) {
                        val c = Dungeon.level.map[cell]
                        var c1 = c
                        if (c == Terrain.EMPTY || c == Terrain.EMBERS || c == Terrain
                                        .EMPTY_DECO) {
                            c1 = if (cur[cell] > 9) Terrain.HIGH_GRASS else Terrain.GRASS
                        } else if (c == Terrain.GRASS && cur[cell] > 9 && Dungeon.level.plants.get(cell) == null) {
                            c1 = Terrain.HIGH_GRASS
                        }

                        if (c1 != c) {
                            Level[cell] = c1
                            GameScene.updateMap(cell)
                        }

                        val ch = Actor.findChar(cell)
                        if (ch != null && off[cell] > 1) {
                            Buff.prolong(ch, Roots::class.java, Actor.TICK)
                        }
                    }
                }
            }
            Dungeon.observe()
        }
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)

        emitter.start(LeafParticle.LEVEL_SPECIFIC, 0.2f, 0)
    }
}
