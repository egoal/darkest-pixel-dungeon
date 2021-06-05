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
package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.items.KindofMisc
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.bags.Bag
import com.egoal.darkestpixeldungeon.items.helmets.Helmet
import com.egoal.darkestpixeldungeon.items.keys.Key
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KindOfWeapon
import com.egoal.darkestpixeldungeon.items.bags.Backpack
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Belongings(private val owner: Hero) : MutableIterable<Item> {
    val backpack = Backpack()

    var weapon: KindOfWeapon? = null
    var armor: Armor? = null
    var helmet: Helmet? = null
    var misc1: KindofMisc? = null
    var misc2: KindofMisc? = null
    var misc3: KindofMisc? = null

    var ironKeys = IntArray(26)
    var specialKeys = IntArray(26) //golden or boss keys

    init {
        backpack.owner = owner
    }

    fun storeInBundle(bundle: Bundle) {
        backpack.storeInBundle(bundle)

        bundle.put(WEAPON, weapon)
        bundle.put(ARMOR, armor)
        bundle.put(HELMET, helmet)
        bundle.put(MISC1, misc1)
        bundle.put(MISC2, misc2)
        bundle.put(MISC3, misc3)

        bundle.put(IRON_KEYS, ironKeys)
        bundle.put(SPECIAL_KEYS, specialKeys)
    }

    fun restoreFromBundle(bundle: Bundle) {
        if (bundle.contains(IRON_KEYS)) ironKeys = bundle.getIntArray(IRON_KEYS)
        if (bundle.contains(SPECIAL_KEYS)) specialKeys = bundle.getIntArray(SPECIAL_KEYS)

        backpack.clear()
        backpack.restoreFromBundle(bundle)

        weapon = bundle.get(WEAPON) as KindOfWeapon?
        weapon?.activate(owner)

        armor = bundle.get(ARMOR) as Armor?
        armor?.activate(owner)

        helmet = bundle.get(HELMET) as Helmet?
        helmet?.activate(owner)

        misc1 = bundle.get(MISC1) as KindofMisc?
        misc1?.activate(owner)

        misc2 = bundle.get(MISC2) as KindofMisc?
        misc2?.activate(owner)

        misc3 = bundle.get(MISC3) as KindofMisc?
        misc3?.activate(owner)
    }

    fun <T : Item> getItem(itemClass: Class<T>): T? = this.find { itemClass.isInstance(it) } as T?

    fun identify() {
        for (item in this) item.identify()
    }

    // when "you know all about this level" triggered
    fun observe() {
        if (weapon != null) {
            weapon!!.identify()
            Badges.validateItemLevelAquired(weapon!!)
        }
        if (armor != null) {
            armor!!.identify()
            Badges.validateItemLevelAquired(armor!!)
        }
        if (misc1 != null) {
            misc1!!.identify()
            Badges.validateItemLevelAquired(misc1!!)
        }
        if (misc2 != null) {
            misc2!!.identify()
            Badges.validateItemLevelAquired(misc2!!)
        }
        if (misc3 != null) {
            misc3!!.identify()
            Badges.validateItemLevelAquired(misc3!!)
        }

        for (item in backpack) item.cursedKnown = true
    }

    fun uncurseEquipped() {
        ScrollOfRemoveCurse.uncurse(owner, *equippedItems())
    }

    fun randomUnequipped(): Item? = Random.element(backpack.items)

    fun equippedItems(): Array<Item?> = arrayOf(weapon, armor, helmet, misc1, misc2, misc3)

    fun miscs() = sequence {
        yield(misc1)
        yield(misc2)
        yield(misc3)
    }

    fun resurrect(depth: Int) {
        for (item in backpack.items.toTypedArray()) {
            if (item is Key) {
                if (item.depth == depth) {
                    item.detachAll(backpack)
                }
            } else if (item.unique) {
                item.detachAll(backpack)
                //you keep the bag itself, not its contents.
                if (item is Bag) item.clear()
                item.collect()
            } else if (!item.isEquipped(owner)) {
                item.detachAll(backpack)
            }
        }

        if (weapon != null) {
            weapon!!.cursed = false
            weapon!!.activate(owner)
        }

        if (armor != null) {
            armor!!.cursed = false
            armor!!.activate(owner)
        }

        if (misc1 != null) {
            misc1!!.cursed = false
            misc1!!.activate(owner)
        }
        if (misc2 != null) {
            misc2!!.cursed = false
            misc2!!.activate(owner)
        }
        if (misc3 != null) {
            misc3!!.cursed = false
            misc3!!.activate(owner)
        }
    }

    fun charge(charge: Float): Int {
        val count = 0

        for (charger in owner.buffs(Wand.Charger::class.java))
            charger.gainCharge(charge)

        return count
    }

    override fun iterator(): MutableIterator<Item> = TheIterator()

    private inner class TheIterator : MutableIterator<Item> {

        private var index = 0

        private val backpackIterator = backpack.iterator()

        private val equipped = equippedItems() // arrayOf<Item?>(weapon, armor, helmet, misc1, misc2, misc3)
        private val backpackIndex = equipped.size

        override fun hasNext(): Boolean {
            for (i in index until backpackIndex) {
                if (equipped[i] != null) {
                    return true
                }
            }

            return backpackIterator.hasNext()
        }

        override fun next(): Item {
            while (index < backpackIndex) {
                val item = equipped[index++]
                if (item != null) {
                    return item
                }
            }

            return backpackIterator.next()
        }

        override fun remove() {
            if (index < backpackIndex) equipped[index] = null

            when (index) {
                0 -> weapon = null
                1 -> armor = null
                2 -> helmet = null
                3 -> misc1 = null
                4 -> misc2 = null
                5 -> misc3 = null
                else -> backpackIterator.remove()
            }
        }
    }

    companion object {
        const val BACKPACK_SIZE = 28

        private const val WEAPON = "weapon"
        private const val ARMOR = "armor"
        private const val HELMET = "helmet"
        private const val MISC1 = "misc1"
        private const val MISC2 = "misc2"
        private const val MISC3 = "misc3"

        private const val IRON_KEYS = "ironKeys"
        private const val SPECIAL_KEYS = "specialKeys"
    }
}
