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
package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Bundle
import kotlin.math.min

class SoulMark : FlavourBuff() {
    init {
        type = buffType.NEGATIVE
    }

    var level = 1

    fun affectHero(hero: Hero, value: Int) {
        hero.buff(Hunger::class.java).satisfy(value * (0.4f + 0.1f * level))
        val dhp = min(hero.HT - hero.HP, (value * (0.25f + 0.05 * level)).toInt())
        if (dhp > 0) {
            hero.HP += dhp
            hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1)
        }
    }

    override fun icon(): Int = BuffIndicator.CORRUPT

    override fun fx(on: Boolean) {
        if (on) target.sprite.add(CharSprite.State.MARKED)
        else target.sprite.remove(CharSprite.State.MARKED)
    }

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", level, dispTurns())

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEVEL, level)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        level = bundle.getInt(LEVEL)
    }

    companion object {
        const val DURATION = 20f

        private const val LEVEL = "level"
    }
}
