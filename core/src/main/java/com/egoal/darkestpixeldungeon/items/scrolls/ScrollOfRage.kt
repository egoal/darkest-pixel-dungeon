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

import com.egoal.darkestpixeldungeon.actors.buffs.Amok
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.mobs.Mimic
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample

class ScrollOfRage : Scroll() {

    init {
        initials = 6
    }

    override fun doRead() {
        val hero = Item.curUser

        for (mob in Dungeon.level.mobs) {
            if (Dungeon.level.distance(mob.pos, hero.pos) <= 16)
                mob.beckon(hero.pos)
        }
        Dungeon.level.mobs.filter { Level.fieldOfView[it.pos] }
                .forEach { Buff.prolong(it, Amok::class.java, 5f) }

        for (heap in Dungeon.level.heaps.values()) {
            if (heap.type === Heap.Type.MIMIC) {
                Mimic.SpawnAt(heap.pos, heap.items)?.let {
                    it.beckon(hero.pos)
                    heap.destroy()
                }
            }
        } 

        GLog.w(Messages.get(this, "roar"))
        setKnown()

        Item.curUser.sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.3f, 3)
        Sample.INSTANCE.play(Assets.SND_CHALLENGE)
        Invisibility.dispel()

        readAnimation()
    }

    override fun price(): Int = if (isKnown) 30 * quantity else super.price()
}
