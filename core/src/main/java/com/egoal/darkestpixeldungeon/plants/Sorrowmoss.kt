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
package com.egoal.darkestpixeldungeon.plants

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.PoisonParticle
import com.egoal.darkestpixeldungeon.items.potions.PotionOfToxicGas
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class Sorrowmoss : Plant(2) {

    override fun activate() {
        Actor.findChar(pos)?.let {
            Buff.affect(it, Poison::class.java).set(Poison.durationFactor(it) * (4 + Dungeon.depth / 2))
        }

        if (Dungeon.visible[pos]) {
            CellEmitter.center(pos).burst(PoisonParticle.SPLASH, 3)
        }
    }

    class Seed : Plant.Seed(plantClass = Sorrowmoss::class.java,
            alchemyClass = PotionOfToxicGas::class.java) {
        init {
            image = ItemSpriteSheet.SEED_SORROWMOSS

            
        }
    }
}
