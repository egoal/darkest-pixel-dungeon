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
package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.artifacts.ChaliceOfBlood
import com.egoal.darkestpixeldungeon.Dungeon

class Regeneration : Buff() {
    private var dreg = 0f

    override fun act(): Boolean {
        if (target.isAlive) {

            dreg += if (target is Hero) (target as Hero).regenerateSpeed() else 0.1f

            if (target.HP < target.HT) {
                val lock = target.buff(LockedFloor::class.java)
                if (target.HP > 0 && (lock == null || lock.regenOn())) {
                    target.HP += dreg.toInt()
                    dreg -= dreg.toInt()
                    if (target.HP >= target.HT) {
                        target.HP = target.HT
                        (target as Hero).resting = false
                    }
                }
            } else dreg = 0f

            spend(1f)
        } else
            diactivate()

        return true
    }
}
