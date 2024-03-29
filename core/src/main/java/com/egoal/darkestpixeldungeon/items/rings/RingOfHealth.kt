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
package com.egoal.darkestpixeldungeon.items.rings

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.IHeroUpgradeListener
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.utils.Bundle
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

class RingOfHealth : Ring(), IHeroUpgradeListener {
    private var dhp = 0
    private fun ratio() = 1.25f.pow(level() * 0.3f) - 1f

    override fun doEquip(hero: Hero): Boolean {
        return if (super.doEquip(hero)) {
            attach(hero)
            true
        } else false
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        return if (super.doUnequip(hero, collect, single)) {
            detach(hero)
            true
        } else false
    }

    override fun upgrade(): Item {
        if (!Dungeon.isHeroNull && isEquipped(Dungeon.hero)) {
            detach(Dungeon.hero)
            super.upgrade()
            attach(Dungeon.hero)
        } else super.upgrade()

        return this
    }

    private fun detach(hero: Hero) {
        modHT(hero, -dhp)
    }

    private fun attach(hero: Hero) {
        modHT(hero, round(hero.HT * ratio()).toInt())
    }

    private fun modHT(hero: Hero, dht: Int) {
        dhp += dht

        val r = hero.HP.toFloat() / hero.HT
        hero.HT += dht
        hero.HP = max(1, round(hero.HT * r).toInt())
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("DHP", dhp)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        dhp = bundle.getInt("DHP")
    }

    override fun buff(): RingBuff = Health()

    inner class Health : Ring.RingBuff()

    override fun onHeroUpgraded(hero: Hero) {
        if (isEquipped(hero)) {
            detach(hero)
            attach(hero)
        }
    }
}

