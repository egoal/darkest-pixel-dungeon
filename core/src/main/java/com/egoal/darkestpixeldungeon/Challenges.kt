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

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.armor.ClassArmor
import com.egoal.darkestpixeldungeon.items.armor.ClothArmor
import com.egoal.darkestpixeldungeon.items.artifacts.HornOfPlenty
import com.egoal.darkestpixeldungeon.items.food.Blandfruit
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.food.OverpricedRation
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop
import com.egoal.darkestpixeldungeon.messages.M

enum class Challenge {
    LowPressure,
    Gifted,
    BruteCourage,
    Immortality,
    GreedIsGood,
    ;

    fun title(): String = M.L(this, "${name.toLowerCase()}.name")
    fun desc(): String = M.L(this, "${name.toLowerCase()}.desc")
    open fun affect(hero: Hero) {}
}

object Challenges {

    const val NO_FOOD = 1 shl 0
    const val NO_ARMOR = 1 shl 1
    const val NO_HEALING = 1 shl 2
    const val NO_HERBALISM = 1 shl 3
    const val SWARM_INTELLIGENCE = 1 shl 4
    const val THE_LONG_NIGHT = 1 shl 5
    const val NO_SCROLLS = 1 shl 6

    const val MAX_VALUE = (1 shl 7) - 1

    val NAME_IDS = arrayOf("no_food", "no_armor", "no_healing",
            "no_herbalism", "swarm_intelligence", "the-long-night",
            "no_scrolls")

    val MASKS = intArrayOf(NO_FOOD, NO_ARMOR, NO_HEALING,
            NO_HERBALISM, SWARM_INTELLIGENCE, THE_LONG_NIGHT,
            NO_SCROLLS)

    fun isForbidden(item: Item): Boolean {
        if (Dungeon.isChallenged(NO_FOOD))
            if (item is Food && item !is OverpricedRation)
                return true
            else if (item is HornOfPlenty)
                return true

        if (Dungeon.isChallenged(NO_ARMOR))
            if (item is Armor && !(item is ClothArmor || item is ClassArmor))
                return true

        if (Dungeon.isChallenged(NO_HEALING))
            if (item is PotionOfHealing) return true
            else if (item is Blandfruit && item.potionAttrib is PotionOfHealing) return true

        if (Dungeon.isChallenged(NO_HERBALISM))
            if (item is Dewdrop) return true

        return false
    }

}