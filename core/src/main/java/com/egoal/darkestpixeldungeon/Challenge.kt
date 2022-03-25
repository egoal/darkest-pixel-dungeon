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

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.VampiricBite
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder
import com.egoal.darkestpixeldungeon.items.bags.WandHolster
import com.egoal.darkestpixeldungeon.items.unclassified.GoldenClaw
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.noosa.Game
import com.watabou.utils.Bundle
import java.io.IOException

enum class Challenge {
    LowPressure,
    Gifted,

    //    BruteCourage,
    Immortality {
        override fun live(hero: Hero) {
            Buff.affect(hero, VampiricBite::class.java)
        }
    },
    GreedIsGood {
        override fun affect(hero: Hero) {
            val e = GoldenClaw.Evil()
            if (!e.doPickUp(hero)) Dungeon.level.drop(e, hero.pos)
        }
    },
    CastingMaster {
        override fun affect(hero: Hero) {
            ScrollHolder().identify().collect()
            Dungeon.limitedDrops.scrollBag.drop()

            PotionBandolier().identify().collect()
            Dungeon.limitedDrops.potionBag.drop()

            WandHolster().identify().collect()
            Dungeon.limitedDrops.wandBag.drop()
        }
    },
    Faith,
    // Loner,
    ;

    fun title(): String = M.L(this, "${name.toLowerCase()}.name")
    fun desc(): String = M.L(this, "${name.toLowerCase()}.desc")

    // the first time selected
    open fun affect(hero: Hero) {
        live(hero)
    }

    // each time the hero rise,
    open fun live(hero: Hero) {}

    companion object {
        private const val CHALLENGE_FILE = "challenge.dat"
        private const val CHALLENGE = "challenge"

        private val challengePassed = hashSetOf<Challenge>()

        fun IsChallengePassed(ch: Challenge) = challengePassed.contains(ch)

        fun PassChallenge(ch: Challenge) {
            if (challengePassed.add(ch)) Save()
        }

        private fun Save() {
            val bundle = Bundle()
            bundle.put(CHALLENGE, challengePassed.map { it.toString() }.toTypedArray())

            val fout = Game.instance.openFileOutput(CHALLENGE_FILE, Game.MODE_PRIVATE)
            Bundle.write(bundle, fout)
            fout.close()
        }

        fun Load() {
            try {
                val fin = Game.instance.openFileInput(CHALLENGE_FILE)
                val bundle = Bundle.read(fin)
                fin.close()

                if (bundle.contains(CHALLENGE)) {
                    challengePassed.clear()
                    challengePassed.addAll(bundle.getStringArray(CHALLENGE).map { valueOf(it) })
                }
            } catch (e: IOException) {
            }
        }
    }
}
