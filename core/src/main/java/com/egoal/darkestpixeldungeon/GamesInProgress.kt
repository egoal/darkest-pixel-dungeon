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
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.watabou.noosa.Game

import java.io.IOException
import java.util.HashMap

object GamesInProgress {
    private const val MAX_SLOT = 6 // enough to compatible with pre 0.6.0 saves

    private val progresses = arrayOfNulls<Info?>(MAX_SLOT)

    private fun gameFile(slot: Int) = "slot$slot.dat"
    private fun depthFile(slot: Int, depth: Int) = "slot$slot-depth$depth.dat"
    private fun backupGameFile(slot: Int) = "backup_slot$slot.dat"
    private fun backupDepthFile(slot: Int) = "backup_level_slot$slot.dat"

    // current
    var curSlot: Int = 0
    val curGameFile get() = gameFile(curSlot)
    fun curDepthFile(depth: Int) = depthFile(curSlot, depth)
    val curBackupGameFile get() = backupGameFile(curSlot)
    val curBackupDepthFile get() = backupDepthFile(curSlot)

    fun reloadAll(): Array<Info?> {
        for (i in 0 until MAX_SLOT) {
            if (progresses[i] == null)
                progresses[i] = load(i)
        }

        // load old saves
        //todo: remove this in later version

        return progresses
    }

    private fun load(slot: Int): Info? {
        var info: Info?

        try {
            val bundle = Dungeon.gameBundle(gameFile(slot))
            info = Info()
            Dungeon.preview(info, bundle)
        } catch (e: IOException) {
            info = null
        }

        return info
    }

    operator fun get(slot: Int) = progresses[slot]

    operator fun set(slot: Int, info: Info?) {
        progresses[slot] = info
    }

    fun delete(slot: Int, deleteLevels: Boolean, deleteBackup: Boolean) {
        progresses[slot] = null

        Game.instance.deleteFile(gameFile(slot))

        if (deleteLevels) {
            var depth = 0
            while (Game.instance.deleteFile(depthFile(slot, depth))) {
                depth++
            }
        }

        if (deleteBackup) {
            Game.instance.deleteFile(backupGameFile(slot))
            Game.instance.deleteFile(backupDepthFile(slot))
        }
    }

    // old
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

    fun set(cl: HeroClass, depth: Int, level: Int, challenge: Challenge?) {
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
        var name: String = "Unnamed"

        var heroClass = HeroClass.ROGUE
        var subClass = HeroSubClass.NONE

        var depth = 0
        var level = 0
        var armorTier = 0

        var challenge: Challenge? = null

        val isChallenged: Boolean get() = challenge != null
    }
}
