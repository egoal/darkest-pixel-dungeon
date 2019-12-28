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

import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.items.unclassified.Bomb
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder

class PotionOfLiquidFlame : Potion() {
    init {
        initials = 5
    }

    override fun canBeReinforced(): Boolean = !reinforced

    override fun shatter(cell: Int) {
        if (Dungeon.visible[cell]) {
            setKnown()

            splash(cell)
            Sample.INSTANCE.play(Assets.SND_SHATTER)
        }

        if (reinforced) {
            Bomb().explode(cell)
        } else {
            for (offset in PathFinder.NEIGHBOURS9) {
                if (Level.flamable[cell + offset]
                        || Actor.findChar(cell + offset) != null
                        || Dungeon.level.heaps.get(cell + offset) != null) {

                    GameScene.add(Blob.seed(cell + offset, 2, Fire::class.java))
                } else {
                    CellEmitter.get(cell + offset).burst(FlameParticle.FACTORY, 2)
                }
            }
        }
    }

    override fun price(): Int = if (isKnown) (30f * quantity.toFloat() * if (reinforced) 1.5f else 1f).toInt() else super.price()
}
