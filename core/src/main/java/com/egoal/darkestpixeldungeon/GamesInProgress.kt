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
package com.egoal.darkestpixeldungeon

import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.watabou.utils.Bundle

import java.io.IOException
import java.util.HashMap

object GamesInProgress {
    private val state = HashMap<HeroClass, Info?>()

    fun check(cl: HeroClass): Info? {
        if (state.containsKey(cl)) {
            return state[cl]
        } else {
            var info: Info?
            try {

                val bundle = Dungeon.gameBundle(Dungeon.gameFile(cl))
                info = Info()
                Dungeon.preview(info, bundle)

            } catch (e: IOException) {
                try {
                    val bundle = Dungeon.gameBundle(Dungeon.backupGameFile(cl))
                    info = Info(true)
                    Dungeon.preview(info, bundle)
                } catch (e: IOException) {
                    info = null
                }
            }

            state[cl] = info
            return info
        }
    }

    operator fun set(cl: HeroClass, depth: Int, level: Int, challenge: Challenge?) {
        val info = Info()
        info.depth = depth
        info.level = level
        info.challenge = challenge
        state[cl] = info
    }

    fun setUnknown(cl: HeroClass) {
        state.remove(cl)
    }

    fun delete(cl: HeroClass) {
        state[cl] = null
    }

    class Info(val isBackup: Boolean = false) {
        var depth: Int = 0
        var level: Int = 0
        var challenge: Challenge? = null

        val isChallenged: Boolean get() = challenge != null
    }
}
