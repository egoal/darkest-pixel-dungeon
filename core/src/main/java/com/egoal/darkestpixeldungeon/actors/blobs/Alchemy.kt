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
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.utils.Bundle

class Alchemy : Blob() {
    private var pos: Int = 0

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        try {
            pos = cur.indices.first { cur[it] > 0 }
        } catch (e: NoSuchElementException) {

        }
    }

    override fun evolve() {
        off[pos] = cur[pos]
        volume = off[pos]
        area.union(pos % Dungeon.level.width(), pos / Dungeon.level.width())

        if (Dungeon.visible[pos]) {
            Journal.add(Journal.Feature.ALCHEMY)
        }
    }

    override fun seed(level: Level, cell: Int, amount: Int) {
        super.seed(level, cell, amount)

        cur[pos] = 0
        pos = cell
        cur[pos] = amount
        volume = cur[pos]

        area.setEmpty()
        area.union(cell % level.width(), cell / level.width())
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)
        emitter.start(Speck.factory(Speck.BUBBLE), 0.4f, 0)
    }
}
