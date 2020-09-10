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

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Heap
import com.watabou.utils.Random

open class Fire : Blob() {

    override fun evolve() {

        val flamable = Level.flamable
        var cell: Int
        var fire: Int

        var observe = false

        for (i in area.left - 1..area.right) {
            for (j in area.top - 1..area.bottom) {
                cell = i + j * Dungeon.level.width()
                if (cur[cell] > 0) {

                    burn(cell)

                    fire = cur[cell] - 1
                    if (flamable[cell] && Random.Int(fire + 1) == 0) {

                        val oldTile = Dungeon.level.map[cell]
                        Dungeon.level.destroy(cell)

                        observe = true
                        GameScene.updateMap(cell)
                        if (Dungeon.visible[cell]) {
                            GameScene.discoverTile(cell, oldTile)
                        }
                    }

                } else {

                    // expand,
                    // since the outer box must be wall, no need to do border check.
                    if (flamable[cell] && (cur[cell - 1] > 0 || cur[cell + 1] > 0
                                    || cur[cell - Dungeon.level.width()] > 0
                                    || cur[cell + Dungeon.level.width()] > 0)) {
                        fire = 4
                        burn(cell)
                        area.union(i, j)
                    } else {
                        fire = 0
                    }

                }

                off[cell] = fire
                volume += fire
            }
        }

        if (observe) {
            Dungeon.observe()
        }
    }

    protected open fun burn(pos: Int) {
        val ch = Actor.findChar(pos)
        if (ch != null) {
            Buff.affect(ch, Burning::class.java).reignite(ch)
        }

        val heap = Dungeon.level.heaps.get(pos)
        heap?.burn()

        val plant = Dungeon.level.plants.get(pos)
        plant?.wither()
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)
        emitter.start(FlameParticle.FACTORY, 0.03f, 0)
    }

    override fun tileDesc(): String? {
        return Messages.get(this, "desc")
    }
}
