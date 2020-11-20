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
package com.egoal.darkestpixeldungeon.items.scrolls

import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.bags.Bag
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.helmets.Helmet
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.messages.Messages

class ScrollOfRemoveCurse : InventoryScroll() {

    init {
        initials = 8
        mode = WndBag.Mode.UNIDED_OR_CURSED
    }

    override fun onItemSelected(item: Item) {
        Flare(6, 32f).show(Item.curUser.sprite, 2f)

        val procced = uncurse(Item.curUser, item)

        Buff.detach(Item.curUser, Weakness::class.java)

        if (procced) {
            GLog.p(Messages.get(this, "cleansed"))
        } else {
            GLog.i(Messages.get(this, "not_cleansed"))
        }
    }

    override fun price(): Int = if (isKnown) 30 * quantity else super.price()

    companion object {

        private fun uncurseOne(hero: Hero, item: Item): Boolean {
            var procced = item.cursed

            when (item) {
                is Weapon -> if (item.hasCurseInscription()) {
                    item.inscribe(null)
                    item.cursed = false
                    procced = true
                }
                is Armor -> if (item.hasCurseGlyph()) {
                    item.inscribe(null)
                    item.cursed = false
                    procced = true
                }
                is Ring -> if (item.level() <= 0) item.upgrade(-item.level() * 2)
                is Bag -> for (bagitem in item.items.filter { it?.cursed ?: false }) {
                    bagitem.cursed = false
                    procced = true
                }
                is Helmet -> item.uncurse() //todo:
            }

            item.cursed = false
            item.cursedKnown = true

            return procced
        }

        fun uncurse(hero: Hero, vararg items: Item?): Boolean {
            val procced = items.filterNotNull().map { uncurseOne(hero, it) }.any { it }

            if (procced) hero.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)

            return procced
        }
    }
}
