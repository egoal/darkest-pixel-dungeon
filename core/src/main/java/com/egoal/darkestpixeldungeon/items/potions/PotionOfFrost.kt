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
package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Frost
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.blobs.Freezing
import com.egoal.darkestpixeldungeon.utils.BArray
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder

class PotionOfFrost : Potion() {
    init {
        initials = 1
    }

    override fun canBeReinforced(): Boolean = !reinforced

    override fun shatter(cell: Int) {
        PathFinder.buildDistanceMap(cell, BArray.not(Level.losBlocking, null), DISTANCE)

        if (reinforced) {
            for (offset in PathFinder.NEIGHBOURS9) {
                Dungeon.level.findMob(cell + offset)?.let { Buff.prolong(it, Frost::class.java, Frost.DURATION) }

                if (Dungeon.level.distance(curUser.pos, cell) <= 1) {
                    Buff.prolong(curUser, Frost::class.java, Frost.DURATION)
                }
            }
        }

        val fire = Dungeon.level.blobs[Fire::class.java] as Fire?

        var visible = false
        for (i in 0 until Dungeon.level.length()) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                visible = Freezing.affect(i, fire) || visible
            }
        }

        if (visible) {
            splash(cell)
            Sample.INSTANCE.play(Assets.SND_SHATTER)

            setKnown()
        }
    }

    override fun price(): Int = if (isKnown) (quantity * if (reinforced) 45 else 30) else super.price()

    companion object {
        private const val DISTANCE = 2
    }
}
