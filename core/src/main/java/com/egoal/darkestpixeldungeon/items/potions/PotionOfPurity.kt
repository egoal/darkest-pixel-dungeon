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

import com.egoal.darkestpixeldungeon.actors.blobs.ConfusionGas
import com.egoal.darkestpixeldungeon.actors.blobs.ParalyticGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.GasesImmunity
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.StenchGas
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.BArray
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder

class PotionOfPurity : Potion() {
    init {
        initials = 9
    }

    override fun canBeReinforced(): Boolean = !reinforced

    override fun shatter(cell: Int) {
        PathFinder.buildDistanceMap(cell, BArray.not(Level.losBlocking, null), DISTANCE)

        var procceed = false

        val blobs = arrayOf<Blob?>(Dungeon.level.blobs[ToxicGas::class.java], Dungeon.level.blobs[ParalyticGas::class.java],
                Dungeon.level.blobs[ConfusionGas::class.java], Dungeon.level.blobs[StenchGas::class.java],
                Dungeon.level.blobs[VenomGas::class.java])

        for (blob in blobs) {
            if (blob == null) continue

            for (i in 0 until Dungeon.level.length()) {
                if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                    val value = blob.cur[i]
                    if (value > 0) {
                        blob.cur[i] = 0
                        blob.volume -= value
                        procceed = true

                        if (Dungeon.visible[i]) CellEmitter.get(i).burst(Speck.factory(Speck.DISCOVER), 1)
                    }
                }
            }
        }

        val heroAffected = PathFinder.distance[Dungeon.hero.pos] < Integer.MAX_VALUE

        if (procceed) {
            if (Dungeon.visible[cell]) {
                splash(cell)
                Sample.INSTANCE.play(Assets.SND_SHATTER)
            }

            setKnown()

            if (heroAffected) {
                GLog.p(Messages.get(this, "freshness"))
            }
        } else {
            super.shatter(cell)

            if (heroAffected) {
                GLog.i(Messages.get(this, "freshness"))
                setKnown()
            }

        }
    }

    override fun apply(hero: Hero) {
        GLog.w(Messages.get(this, "no_smell"))
        Buff.prolong(hero, GasesImmunity::class.java, GasesImmunity.DURATION * if (reinforced) 2 else 1)
        setKnown()
    }

    override fun price(): Int {
        return if (isKnown) (40 * quantity * if (reinforced) 1.5f else 1f).toInt() else super.price()
    }

    companion object {
        private const val DISTANCE = 5
    }
}
