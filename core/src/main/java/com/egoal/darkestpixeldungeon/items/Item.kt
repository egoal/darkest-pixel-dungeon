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
package com.egoal.darkestpixeldungeon.items

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Combo
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.Knowledgeable
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.bags.Bag
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Boomerang
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.MissileSprite
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.particles.Emitter
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.Callback

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

open class Item : Bundlable {
    var defaultAction: String = ""
    var usesTargeting: Boolean = false

    protected var name: String = M.L(this, "name")
    var image = 0

    var stackable = false
    var quantity = 1

    private var level = 0

    var levelKnown = false
    var cursed = false
    var cursedKnown = false

    var everPicked = false // if is the first time get picked
    var unique = false // Unique items persist through revival
    var bones = false // whether an item can be included in heroes remains

    open val isUpgradable: Boolean
        get() = true

    open val isIdentified: Boolean
        get() = levelKnown && cursedKnown

    open fun actions(hero: Hero): ArrayList<String> = arrayListOf(AC_DROP, AC_THROW)

    open fun doPickUp(hero: Hero): Boolean {
        if (collect(hero.belongings.backpack)) {
            if (!everPicked) onFirstPick(hero)
            everPicked = true

            GameScene.pickUp(this)
            Sample.INSTANCE.play(Assets.SND_ITEM)
            hero.spendAndNext(TIME_TO_PICK_UP)
            return true
        } else {
            return false
        }
    }

    protected open fun onFirstPick(hero: Hero) {
        hero.heroPerk.get(Knowledgeable::class.java)?.affectItem(this)
    }

    open fun doDrop(hero: Hero) {
        hero.spendAndNext(TIME_TO_DROP)
        Dungeon.level.drop(detachAll(hero.belongings.backpack), hero.pos).sprite.drop(hero.pos)
    }

    // resets an item's properties, to ensure consistency between runs
    open fun reset() {
        // resets the name incase the language has changed.
        name = M.L(this, "name")
    }

    open fun doThrow(hero: Hero) {
        GameScene.selectCell(thrower)
    }

    open fun execute(hero: Hero, action: String) {
        curUser = hero
        curItem = this

        hero.buff(Combo::class.java)?.detach()

        if (action == AC_DROP) {
            doDrop(hero)
        } else if (action == AC_THROW) {
            doThrow(hero)
        }
    }

    fun execute(hero: Hero) {
        execute(hero, defaultAction)
    }

    protected open fun onThrow(cell: Int) {
        val heap = Dungeon.level.drop(this, cell)
        if (!heap.empty()) {
            heap.sprite.drop(cell)
        }
    }

    open fun collect(container: Bag): Boolean {
        val items = container.items

        if (items.contains(this))
            return true

        for (item in items) {
            if (item is Bag && item.canHold(this)) {
                return collect(item)
            }
        }

        if (stackable) {
            for (item in items) {
                if (isSimilar(item)) {
                    item.quantity += quantity
                    item.updateQuickslot()
                    return true
                }
            }
        }

        if (!container.canHold(this)) {
            GLog.n(Messages.get(Item::class.java, "pack_full", name()))
            return false
        }

        if (!Dungeon.isHeroNull && Dungeon.hero.isAlive) {
            Badges.validateItemLevelAquired(this)
        }
        items.add(this)

        if (stackable || this is Boomerang)
            Dungeon.quickslot.replaceSimilar(this)

        updateQuickslot()
        Collections.sort(items, itemComparator)
        return true
    }

    fun collect(): Boolean {
        return collect(Dungeon.hero.belongings.backpack)
    }

    fun detach(container: Bag): Item? {
        if (quantity <= 0) {
            return null
        } else if (quantity == 1) {
            if (stackable || this is Boomerang) {
                Dungeon.quickslot.convertToPlaceholder(this)
            }

            return detachAll(container)
        } else {
            quantity--
            updateQuickslot()

            try {
                //pssh, who needs copy constructors?
                val detached = javaClass.newInstance()
                val copy = Bundle()
                this.storeInBundle(copy)
                detached.restoreFromBundle(copy)
                detached.quantity(1)

                detached.onDetach()
                return detached
            } catch (e: Exception) {
                DarkestPixelDungeon.reportException(e)
                return null
            }

        }
    }

    fun detachAll(container: Bag): Item {
        Dungeon.quickslot.clearItem(this)
        updateQuickslot()

        for (item in container.items) {
            if (item === this) {
                container.items.remove(this)
                item.onDetach()
                return this
            } else if (item is Bag) {
                if (item.contains(this)) {
                    return detachAll(item)
                }
            }
        }

        return this
    }

    open fun isSimilar(item: Item): Boolean = javaClass == item.javaClass

    protected open fun onDetach() {}

    fun level(): Int = level

    open fun level(value: Int) {
        level = value

        updateQuickslot()
    }

    open fun upgrade(): Item {
        cursed = false
        this.level++

        updateQuickslot()

        return this
    }

    fun upgrade(n: Int): Item {
        for (i in 0 until n) {
            upgrade()
        }

        return this
    }

    open fun degrade(): Item {
        this.level--

        return this
    }

    fun degrade(n: Int): Item {
        for (i in 0 until n) {
            degrade()
        }

        return this
    }

    open fun visiblyUpgraded(): Int = if (levelKnown) level else 0

    fun visiblyCursed(): Boolean = cursed && cursedKnown

    open fun isEquipped(hero: Hero): Boolean = false

    open fun identify(): Item {
        levelKnown = true
        cursedKnown = true

        return this
    }

    override fun toString(): String {
        var name = name()

        if (visiblyUpgraded() != 0)
            name = Messages.format(TXT_TO_STRING_LVL, name, visiblyUpgraded())

        if (quantity > 1)
            name = Messages.format(TXT_TO_STRING_X, name, quantity)

        return name
    }

    open fun name(): String = name

    fun trueName(): String = name

    open fun image(): Int = image

    open fun glowing(): ItemSprite.Glowing? = null

    open fun emitter(): Emitter? = null

    open fun info(): String = desc()

    open fun desc(): String = M.L(this, "desc")

    fun quantity(): Int = quantity

    fun quantity(value: Int): Item {
        quantity = value
        return this
    }

    open fun price(): Int = 0

    //! the quantity affect the price() function
    fun sellPrice(): Int = price() * (Dungeon.depth / 3 * 3 + 4)

    open fun random(): Item = this

    open fun status(): String? = if (quantity != 1) "$quantity" else null

    fun updateQuickslot() {
        QuickSlotButton.refresh()
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(QUANTITY, quantity)
        bundle.put(LEVEL, level)
        bundle.put(LEVEL_KNOWN, levelKnown)
        bundle.put(CURSED, cursed)
        bundle.put(CURSED_KNOWN, cursedKnown)
        bundle.put(EVER_PICKED, everPicked)
        if (Dungeon.quickslot.contains(this)) {
            bundle.put(QUICKSLOT, Dungeon.quickslot.getSlot(this))
        }
    }

    override fun restoreFromBundle(bundle: Bundle) {
        quantity = bundle.getInt(QUANTITY)
        levelKnown = bundle.getBoolean(LEVEL_KNOWN)
        cursedKnown = bundle.getBoolean(CURSED_KNOWN)
        everPicked = bundle.getBoolean(EVER_PICKED)

        val level = bundle.getInt(LEVEL)
        if (level > 0) {
            upgrade(level)
        } else if (level < 0) {
            degrade(-level)
        }

        cursed = bundle.getBoolean(CURSED)

        //only want to populate slot on first load.
        if (Dungeon.isHeroNull) {
            if (bundle.contains(QUICKSLOT))
                Dungeon.quickslot.setSlot(bundle.getInt(QUICKSLOT), this)
        }
    }

    open fun throwPos(user: Hero, dst: Int): Int {
        return Ballistica(user.pos, dst, Ballistica.PROJECTILE).collisionPos
    }

    open fun cast(user: Hero, dst: Int) {
        val cell = throwPos(user, dst)
        user.sprite.zap(cell)
        user.busy()

        Sample.INSTANCE.play(Assets.SND_MISS, 0.6f, 0.6f, 1.5f)

        val enemy = Actor.findChar(cell)
        QuickSlotButton.target(enemy)

        var delay = TIME_TO_THROW
        if (this is MissileWeapon)
            delay *= this.speedFactor(user)

        val finalDelay = delay

        (user.sprite.parent.recycle(MissileSprite::class.java) as MissileSprite).reset(user.pos, cell, this, Callback {
            this@Item.detach(user.belongings.backpack)!!.onThrow(cell)
            user.spendAndNext(finalDelay)
        })
    }

    companion object {
        const val TXT_TO_STRING_LVL = "%s %+d"
        const val TXT_TO_STRING_X = "%s x%d"

        const val TIME_TO_THROW = 1.0f
        const val TIME_TO_PICK_UP = 1.0f
        const val TIME_TO_DROP = 0.5f

        const val AC_DROP = "DROP"
        const val AC_THROW = "THROW"

        var itemComparator: Comparator<Item> = Comparator { lhs, rhs -> Generator.ItemOrder(lhs) - Generator.ItemOrder(rhs) }

        fun evoke(hero: Hero) {
            hero.sprite.emitter().burst(Speck.factory(Speck.EVOKE), 5)
        }

        fun virtual(cl: Class<out Item>): Item? {
            try {
                val item = cl.newInstance() as Item
                item.quantity = 0
                return item

            } catch (e: Exception) {
                DarkestPixelDungeon.reportException(e)
                return null
            }

        }

        private const val QUANTITY = "quantity"
        private const val LEVEL = "level"
        private const val LEVEL_KNOWN = "levelKnown"
        private const val CURSED = "cursed"
        private const val CURSED_KNOWN = "cursedKnown"
        private const val QUICKSLOT = "quickslotpos"
        private const val EVER_PICKED = "everpicked"

        lateinit var curUser: Hero
        lateinit var curItem: Item
        protected var thrower: CellSelector.Listener = object : CellSelector.Listener {
            override fun onSelect(target: Int?) {
                if (target != null) {
                    curItem.cast(curUser, target)
                }
            }

            override fun prompt(): String = M.L(Item::class.java, "prompt")
        }
    }
}
