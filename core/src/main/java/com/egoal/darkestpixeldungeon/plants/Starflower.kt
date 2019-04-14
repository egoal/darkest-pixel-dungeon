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

import com.egoal.darkestpixeldungeon.actors.buffs.Bless
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.potions.PotionOfExperience
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class Starflower : Plant(11) {
    
    override fun activate() {
        Actor.findChar(pos)?.let {
            Buff.prolong(it, Bless::class.java, 30f)
        }

        if (Random.Int(5) == 0) {
            Dungeon.level.drop(Seed(), pos).sprite.drop()
        }
    }

    class Seed : Plant.Seed(plantClass = Starflower::class.java, 
            alchemyClass = PotionOfExperience::class.java) {
        init {
            image = ItemSpriteSheet.SEED_STARFLOWER

            
        }
    }
}
