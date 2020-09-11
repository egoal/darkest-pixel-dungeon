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
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.particles.WindParticle
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.features.Chasm
import com.watabou.utils.PathFinder

class PitfallTrap : Trap() {

    init {
        color = TrapSprite.RED
        shape = TrapSprite.DIAMOND
    }

    override fun activate() {
        val heap = Dungeon.level.heaps.get(pos)

        if (heap != null) {
            for (item in heap.items) {
                Dungeon.dropToChasm(item)
            }
            heap.sprite.kill()
            GameScene.discard(heap)
            Dungeon.level.heaps.remove(pos)
        }

        val ch = Actor.findChar(pos)

        if (ch === Dungeon.hero) {
            Chasm.HeroFall(pos)
        } else if (ch != null) {
            Chasm.MobFall((ch as Mob?)!!)
        }
    }

    override fun disarm() {
        super.disarm()

        // if making a pit here wouldn't block any paths, make a pit tile instead of a disarmed trap tile.
        if (!(Level.solid[pos - Dungeon.level.width()] && Level.solid[pos + Dungeon.level.width()]) &&
                !(Level.solid[pos - 1] && Level.solid[pos + 1])) {

            val c = Dungeon.level.map[pos - Dungeon.level.width()]

            if (c == Terrain.WALL || c == Terrain.WALL_DECO) {
                Level[pos] = Terrain.CHASM_WALL
            } else {
                Level[pos] = Terrain.CHASM_FLOOR
            }

            sprite.parent.add(WindParticle.Wind(pos))
            sprite.kill()
            GameScene.updateMap(pos)
        }
    }
}
