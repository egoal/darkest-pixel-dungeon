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
package com.egoal.darkestpixeldungeon.levels.features

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.items.unclassified.ExtractionFlask
import com.egoal.darkestpixeldungeon.items.food.Blandfruit
import com.egoal.darkestpixeldungeon.windows.WndAlchemy
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat
import com.egoal.darkestpixeldungeon.items.food.StewedMeat
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfToxicGas
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Unstable
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.plants.Sorrowmoss
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random
import java.lang.RuntimeException

object AlchemyPot {
    fun Operate(hero: Hero, pos: Int) {
        GameScene.show(WndAlchemy())
    }

    const val MAX_INPUTS = 3

    //todo: refactor all this, perhaps in version 0.4+
    // succeed? : result
    fun VerifyRefinement(items: List<Item>): Pair<Boolean, Item?> {
        return when (items.size) {
            // seed to potion.
            3 -> if (items.all { it is Plant.Seed }) Pair(true, KGenerator.POTION.generate()) else Pair(false, null)
            2 -> {
                val pr = SplitTwoItem({ it is Plant.Seed }, { it is Blandfruit }, items[0], items[1])
                if (pr != null)
                    return Pair(true, (pr.second as Blandfruit).cook(pr.first as Plant.Seed))

                return Pair(false, null)
            }
            1 -> {
                if (items[0] is MysteryMeat) return Pair(true, StewedMeat.Cook(items[0] as MysteryMeat))
                else Pair(false, null)
            }
            else -> Pair(false, null)
        }
    }

    fun OnCombined(items: List<Item>, result: Item) {
        GLog.w(Messages.get(this, "combined", result.name()))
        if (!result.doPickUp(Dungeon.hero))
            Dungeon.level.drop(result, Dungeon.hero.pos)

        if (result is Potion) {
            Statistics.PotionsCooked++
            Badges.validatePotionsCooked()
        }
    }

    private fun SplitTwoItem(checker0: (Item) -> Boolean, checker1: (Item) -> Boolean,
                             item0: Item, item1: Item): Pair<Item, Item>? = when {
        checker0(item0) && checker1(item1) -> Pair(item0, item1)
        checker0(item1) && checker1(item0) -> Pair(item1, item0)
        else -> null
    }
}
