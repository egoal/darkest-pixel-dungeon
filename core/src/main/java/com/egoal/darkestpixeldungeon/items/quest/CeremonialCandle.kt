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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.actors.mobs.NewbornElemental
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList


class CeremonialCandle : Item() {
    init {
        image = ItemSpriteSheet.CANDLE

        defaultAction = AC_THROW

        unique = true
        stackable = true
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    override fun doDrop(hero: Hero) {
        super.doDrop(hero)
        checkCandles()
    }

    override fun onThrow(cell: Int) {
        super.onThrow(cell)
        checkCandles()
    }

    companion object {
        //generated with the wandmaker quest
        var ritualPos: Int = 0

        private fun checkCandles() {
            val heaps = PathFinder.NEIGHBOURS4.map { ritualPos + it }.map { Dungeon.level.heaps.get(it) }
            if (heaps.all { it?.peek() is CeremonialCandle }) {
                // yes, summon
                heaps.forEach { it.pickUp() }
                val elemental = NewbornElemental()
                if (Actor.findChar(ritualPos) != null) {
                    val candidates = PathFinder.NEIGHBOURS8.map { ritualPos + it }.filter {
                        (Level.passable[it] || Level.avoid[it]) && Actor.findChar(it) == null
                    }
                    elemental.pos = if (candidates.isNotEmpty()) candidates.random() else ritualPos
                } else
                    elemental.pos = ritualPos
                elemental.state = elemental.HUNTING
                GameScene.add(elemental, 1f)
                PathFinder.NEIGHBOURS9.forEach {
                    CellEmitter.get(ritualPos + it).burst(ElmoParticle.FACTORY, 10)
                }
                Sample.INSTANCE.play(Assets.SND_BURNING)
            }
        }
    }
}
