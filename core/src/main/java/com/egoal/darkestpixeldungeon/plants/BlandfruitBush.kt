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
import com.egoal.darkestpixeldungeon.items.food.Blandfruit
import com.egoal.darkestpixeldungeon.items.potions.PotionOfInvisibility
import com.egoal.darkestpixeldungeon.items.potions.PotionOfPhysique
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class BlandfruitBush : Plant(8) {

    override fun activate() {
        Dungeon.level.drop(Blandfruit(), pos).sprite.drop()
    }

    class Seed : Plant.Seed() {
        init {
            image = ItemSpriteSheet.SEED_BLANDFRUIT
        }

        override fun price(): Int = 20 * quantity
    }
}
