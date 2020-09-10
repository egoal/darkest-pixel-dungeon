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
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.watabou.utils.Bundle

import java.util.ArrayList

class PinCushion : Buff() {

    private var items = ArrayList<MissileWeapon>()

    fun stick(projectile: MissileWeapon) {
        for (item in items) {
            if (item.isSimilar(projectile)) {
                item.quantity(item.quantity() + projectile.quantity())
                return
            }
        }
        items.add(projectile)
    }

    override fun detach() {
        for (item in items)
            Dungeon.level.drop(item, target.pos).sprite.drop()
        super.detach()
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(ITEMS, items)
        super.storeInBundle(bundle)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        items = ArrayList(bundle.getCollection(ITEMS) as Collection<*> as Collection<MissileWeapon>)
        super.restoreFromBundle(bundle)
    }

    companion object {

        private val ITEMS = "items"
    }
}
