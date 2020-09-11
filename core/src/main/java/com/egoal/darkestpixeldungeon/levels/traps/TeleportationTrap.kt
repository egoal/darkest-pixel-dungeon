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

import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample

class TeleportationTrap : Trap() {

    init {
        color = TrapSprite.TEAL
        shape = TrapSprite.DOTS
    }

    override fun activate() {

        CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3)
        Sample.INSTANCE.play(Assets.SND_TELEPORT)

        val ch = Actor.findChar(pos)
        if (ch is Hero) {
            ScrollOfTeleportation.teleportHero((ch as Hero?)!!)
        } else if (ch != null) {
            var count = 10
            var pos: Int
            do {
                pos = Dungeon.level.randomRespawnCell()
                if (count-- <= 0) {
                    break
                }
            } while (pos == -1)

            if (pos == -1 || Dungeon.bossLevel()) {

                GLog.w(Messages.get(ScrollOfTeleportation::class.java, "no_tele"))

            } else {
                ch.pos = pos
                if (ch is Mob && ch.state === ch.HUNTING)
                    ch.state = ch.WANDERING

                ch.sprite.place(ch.pos)
                ch.sprite.visible = Dungeon.visible[pos]

            }
        }

        val heap = Dungeon.level.heaps.get(pos)

        if (heap != null) {
            val cell = Dungeon.level.randomRespawnCell()

            val item = heap.pickUp()

            if (cell != -1) {
                Dungeon.level.drop(item, cell)
            }
        }
    }
}
