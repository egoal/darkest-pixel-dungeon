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
package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Light
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.particles.Emitter

import java.util.ArrayList

class Torch : Item() {
    init {
        image = ItemSpriteSheet.TORCH

        stackable = true

        defaultAction = AC_LIGHT
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_LIGHT) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_LIGHT) {

            hero.spend(TIME_TO_LIGHT)
            hero.busy()

            hero.sprite.operate(hero.pos)

            detach(hero.belongings.backpack)
            Buff.affect(hero, Light::class.java, Light.DURATION)

            val emitter = hero.sprite.centerEmitter()
            emitter.start(FlameParticle.FACTORY, 0.2f, 3)
        }
    }

    override fun isUpgradable(): Boolean = false

    override fun isIdentified(): Boolean = true 

    override fun price(): Int = 20 * quantity

    companion object {
        private const val AC_LIGHT = "LIGHT"

        private const val TIME_TO_LIGHT = .5f
    }

}
