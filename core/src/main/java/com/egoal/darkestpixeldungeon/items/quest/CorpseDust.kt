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
package com.egoal.darkestpixeldungeon.items.quest

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.Wraith
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.ArrayList

class CorpseDust : Item() {

    init {
        image = ItemSpriteSheet.DUST

        cursed = true
        cursedKnown = true

        unique = true
    }

    //yup, no dropping this one
    override fun actions(hero: Hero): ArrayList<String> = ArrayList()

    override fun isUpgradable(): Boolean = false

    override fun isIdentified(): Boolean = true

    override fun doPickUp(hero: Hero): Boolean {
        if (super.doPickUp(hero)) {
            GLog.n(Messages.get(this, "picked"))
            Buff.affect(hero, DustGhostSpawner::class.java)
            return true
        }
        return false
    }

    override fun onDetach() {
        Dungeon.hero.buff(DustGhostSpawner::class.java)?.dispel()
    }

    class DustGhostSpawner : Buff() {

        private var spawnPower = 0

        override fun act(): Boolean {
            spawnPower++
            // +1: we include the wraith we're trying to spawn
            val wraiths = Dungeon.level.mobs.count { it is Wraith } + 1

            val powerNeeded = Math.min(25, wraiths * wraiths)

            if (powerNeeded <= spawnPower) {
                spawnPower -= powerNeeded
                var pos: Int
                do {
                    pos = Random.Int(Dungeon.level.length())
                } while (!Dungeon.visible[pos] || !Level.passable[pos] || Actor
                                .findChar(pos) != null)
                Wraith.spawnAt(pos)
                Sample.INSTANCE.play(Assets.SND_CURSED)
            }

            spend(Actor.TICK)
            return true
        }

        fun dispel() {
            detach()

            Dungeon.level.mobs.filter { it is Wraith }.forEach { it.die(null) }
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(SPAWNPOWER, spawnPower)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            spawnPower = bundle.getInt(SPAWNPOWER)
        }

        companion object {
            private const val SPAWNPOWER = "spawnpower"
        }
    }

}
