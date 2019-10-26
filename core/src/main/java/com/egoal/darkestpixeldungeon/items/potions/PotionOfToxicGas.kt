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

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class PotionOfToxicGas : Potion() {

    init {
        initials = 11
    }

    override fun canBeReinforced(): Boolean = !reinforced

    override fun shatter(cell: Int) {
        if (Dungeon.visible[cell]) {
            setKnown()

            splash(cell)
            Sample.INSTANCE.play(Assets.SND_SHATTER)
        }

        if (reinforced) {
            reinforced_shatter(cell)
        } else
            GameScene.add(Blob.seed(cell, 1000, ToxicGas::class.java))
    }

    private fun reinforced_shatter(cell: Int) {
        for (offset in PathFinder.NEIGHBOURS9) {
            val mob = Dungeon.level.findMobAt(cell + offset)
            if (mob != null) {
                reinforced_affect(mob)
            }
        }
        if (Dungeon.level.distance(curUser.pos, cell) <= 1) {
            reinforced_affect(curUser)
        }
    }

    private fun reinforced_affect(c: Char) {
        val p = Buff.affect(c, Poison::class.java)
        p.set(Random.Int(6, 10).toFloat())
        p.addDamage(Dungeon.depth / 2 + Random.Int(1, 4))
    }

    override fun price(): Int {
        return if (isKnown) (30f * quantity.toFloat() * if (reinforced) 1.5f else 1f).toInt() else super.price()
    }

}
