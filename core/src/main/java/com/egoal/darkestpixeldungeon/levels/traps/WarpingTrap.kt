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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.watabou.noosa.Game
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

import java.util.ArrayList

class WarpingTrap : Trap() {

    init {
        color = TrapSprite.TEAL
        shape = TrapSprite.STARS
    }

    override fun activate() {
        CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3)
        Sample.INSTANCE.play(Assets.SND_TELEPORT)

        if (Dungeon.depth <= 1 || Dungeon.bossLevel()) return

        //each depth has 1 more weight than the previous depth.
        val depths = (1 until Dungeon.depth).map { it.toFloat() }.toFloatArray()
        val depth = 1 + Math.max(Random.chances(depths), Random.chances(depths))

        Dungeon.level.heaps.get(pos)?.let { heap ->
            var dropped = Dungeon.droppedItems.get(depth)
            if (dropped == null) {
                dropped = ArrayList<Item>()
                Dungeon.droppedItems.put(depth, dropped)
            }
            dropped.addAll(heap.items)
            heap.destroy()
        }

        Actor.findChar(pos)?.let { ch ->
            if (ch === Dungeon.hero) {
                Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze::class.java)?.detach()
                for (gh in Dungeon.level.mobs.filterIsInstance<GhostHero>()) gh.destroy()

                InterlevelScene.mode = InterlevelScene.Mode.RETURN
                InterlevelScene.returnDepth = depth
                InterlevelScene.returnPos = -1
                Game.switchScene(InterlevelScene::class.java)
            } else {
                // just destroy the char
                ch.destroy()
                ch.sprite.killAndErase()
                Dungeon.level.mobs.remove(ch)
            }
        }
    }
}
