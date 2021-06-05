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
package com.egoal.darkestpixeldungeon.items.bags

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

import java.util.ArrayList

open class Bag : Item(), MutableIterable<Item> {

    var owner: Char? = null

    var items = ArrayList<Item>()

    var size = 1

    override val isUpgradable: Boolean
        get() = false

    override val isIdentified: Boolean
        get() = true

    init {
        image = 11

        defaultAction = AC_OPEN

        unique = true
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_OPEN) {

            GameScene.show(WndBag(this, null, WndBag.Mode.ALL, null))

        }
    }

    override fun collect(container: Bag): Boolean {
        // collect items into self first
        for (item in container.items.toTypedArray()) {
            if (canHold(item)) {
                item.detachAll(container)
                if (!item.collect(this))
                    item.collect(container)
            }
        }

        if (super.collect(container)) {

            owner = container.owner

            Badges.validateAllBagsBought(this)

            return true
        } else {
            return false
        }
    }

    public override fun onDetach() {
        this.owner = null
        for (item in items)
            Dungeon.quickslot.clearItem(item)
        updateQuickslot()
    }

    fun clear() {
        items.clear()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ITEMS, items)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        for (item in bundle.getCollection(ITEMS)) {
            if (item != null) (item as Item).collect(this)
        }
    }

    operator fun contains(item: Item): Boolean {
        for (i in items) {
            if (i === item) {
                return true
            } else if (i is Bag && i.contains(item)) {
                return true
            }
        }
        return false
    }

    open fun canHold(item: Item): Boolean = items.size < size

    override fun iterator(): MutableIterator<Item> {
        return ItemIterator()
    }

    private inner class ItemIterator : MutableIterator<Item> {

        private var index = 0
        private var nested: MutableIterator<Item>? = null

        override fun hasNext(): Boolean {
            return if (nested != null) {
                nested!!.hasNext() || index < items.size
            } else {
                index < items.size
            }
        }

        override fun next(): Item {
            if (nested != null && nested!!.hasNext()) {

                return nested!!.next()

            } else {

                nested = null

                val item = items[index++]
                if (item is Bag) {
                    nested = item.iterator()
                }

                return item
            }
        }

        override fun remove() {
            if (nested != null) {
                nested!!.remove()
            } else {
                items.removeAt(index)
            }
        }
    }

    companion object {
        const val AC_OPEN = "OPEN"
        private const val ITEMS = "inventory"
    }
}
