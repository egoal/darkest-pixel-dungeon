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
package com.egoal.darkestpixeldungeon.items.food

import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.buffs.Barkskin
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.buffs.Drowsy
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.actors.buffs.Slow
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random

class FrozenCarpaccio : Food(Hunger.STARVING - Hunger.HUNGRY, 1) {
    init {
        image = ItemSpriteSheet.CARPACCIO
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_EAT) effect(hero)
    }

    override fun price(): Int = 10 * quantity

    companion object {

        fun effect(hero: Hero) {
            when (Random.Int(5)) {
                0 -> {
                    GLog.i(Messages.get(FrozenCarpaccio::class.java, "invis"))
                    Buff.affect(hero, Invisibility::class.java, Invisibility.DURATION)
                }
                1 -> {
                    GLog.i(Messages.get(FrozenCarpaccio::class.java, "hard"))
                    Buff.affect(hero, Barkskin::class.java).level(hero.HT / 4)
                }
                2 -> {
                    GLog.i(Messages.get(FrozenCarpaccio::class.java, "refresh"))
                    Buff.detach(hero, Poison::class.java)
                    Buff.detach(hero, Cripple::class.java)
                    Buff.detach(hero, Weakness::class.java)
                    Buff.detach(hero, Bleeding::class.java)
                    Buff.detach(hero, Drowsy::class.java)
                    Buff.detach(hero, Slow::class.java)
                    Buff.detach(hero, Vertigo::class.java)
                }
                3 -> {
                    GLog.i(Messages.get(FrozenCarpaccio::class.java, "better"))
                    if (hero.HP < hero.HT) {
                        hero.HP = Math.min(hero.HP + hero.HT / 4, hero.HT)
                        hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1)
                    }
                }
            }
        }

        fun cook(ingredient: MysteryMeat): Food = FrozenCarpaccio().apply { quantity = ingredient.quantity() }
    }
}
