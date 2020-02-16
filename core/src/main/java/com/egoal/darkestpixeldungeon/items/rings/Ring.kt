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

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.items.ItemStatusHandler
import com.egoal.darkestpixeldungeon.items.KindofMisc
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.ArrayList
import java.util.HashMap

open abstract class Ring : KindofMisc() {

    protected var buff: Buff? = null

    private var gem: String? = null

    private var ticksToKnow = TICKS_TO_KNOW

    val isKnown: Boolean
        get() = handler.isKnown(this)

    init {
        reset()
    }

    final override fun reset() {
        super.reset()
        image = handler.image(this)
        gem = handler.label(this)
    }

    override fun activate(ch: Char) {
        buff = buff()
        buff!!.attachTo(ch)
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        return if (super.doUnequip(hero, collect, single)) {
            hero.remove(buff!!)
            buff = null
            true
        } else false
    }

    protected fun setKnown() {
        if (!isKnown) handler.know(this)

        Badges.validateAllRingsIdentified()
    }

    override fun name(): String = if (isKnown) super.name() else Messages.get(Ring::class.java, gem)

    override fun info(): String {
        var desc = if (isKnown) desc() else Messages.get(this, "unknown_desc")

        if (cursed && isEquipped(Dungeon.hero)) {
            desc += "\n\n" + Messages.get(Ring::class.java, "cursed_worn")
        } else if (cursed && cursedKnown) {
            desc += "\n\n" + Messages.get(Ring::class.java, "curse_known")
        }

        return desc
    }

    override fun isIdentified(): Boolean = super.isIdentified() && isKnown

    override fun identify(): Item {
        setKnown()
        return super.identify()
    }

    override fun random(): Item {
        var n = 1
        if (Random.Int(3) == 0) {
            n++
            if (Random.Int(5) == 0) {
                n++
            }
        }

        if (Random.Float() < 0.3f) {
            level(-n)
            cursed = true
        } else
            level(n)

        return this
    }

    override fun price(): Int {
        var price = 75
        if (cursed && cursedKnown) {
            price /= 2
        }
        if (levelKnown) {
            if (level() > 0) {
                price *= level() + 1
            } else if (level() < 0) {
                price /= 1 - level()
            }
        }
        if (price < 1) {
            price = 1
        }
        return price
    }

    protected abstract fun buff(): RingBuff

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(UNFAMILIRIARITY, ticksToKnow)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        ticksToKnow = bundle.getInt(UNFAMILIRIARITY)
        if (ticksToKnow == 0) ticksToKnow = TICKS_TO_KNOW
    }

    open inner class RingBuff : Buff() {
        override fun attachTo(target: Char): Boolean {
            if (target is Hero && target.heroClass === HeroClass.ROGUE && !isKnown) {
                setKnown()
                GLog.i(Messages.get(Ring::class.java, "known", name()))
                Badges.validateItemLevelAquired(this@Ring)
            }

            return super.attachTo(target)
        }

        override fun act(): Boolean {
            if (!isIdentified && --ticksToKnow <= 0) {
                identify()
                GLog.w(Messages.get(Ring::class.java, "identify", this@Ring.toString()))
                Badges.validateItemLevelAquired(this@Ring)
            }

            spend(Actor.TICK)

            return true
        }

        fun level(): Int = this@Ring.level()
    }

    companion object {

        private const val TICKS_TO_KNOW = 200
        private const val UNFAMILIRIARITY = "unfamiliarity"

        private val rings = arrayOf(
                RingOfAccuracy::class.java, RingOfEvasion::class.java, RingOfResistance::class.java,
                RingOfForce::class.java, RingOfFuror::class.java, RingOfHaste::class.java,
                RingOfCritical::class.java, RingOfMight::class.java, RingOfSharpshooting::class.java,
                RingOfHealth::class.java, RingOfWealth::class.java)

        private val gems = hashMapOf(
                "garnet" to ItemSpriteSheet.RING_GARNET,
                "ruby" to ItemSpriteSheet.RING_RUBY,
                "topaz" to ItemSpriteSheet.RING_TOPAZ,
                "emerald" to ItemSpriteSheet.RING_EMERALD,
                "onyx" to ItemSpriteSheet.RING_ONYX,
                "opal" to ItemSpriteSheet.RING_OPAL,
                "tourmaline" to ItemSpriteSheet.RING_TOURMALINE,
                "sapphire" to ItemSpriteSheet.RING_SAPPHIRE,
                "amethyst" to ItemSpriteSheet.RING_AMETHYST,
                "quartz" to ItemSpriteSheet.RING_QUARTZ,
                "agate" to ItemSpriteSheet.RING_AGATE,
                "diamond" to ItemSpriteSheet.RING_DIAMOND)

        private lateinit var handler: ItemStatusHandler<Ring>

        fun initGems() {
            handler = ItemStatusHandler(rings, gems)
        }

        fun save(bundle: Bundle) {
            handler.save(bundle)
        }

        fun saveSelectively(bundle: Bundle, items: ArrayList<Item>) {
            handler.saveSelectively(bundle, items)
        }

        fun restore(bundle: Bundle) {
            handler = ItemStatusHandler(rings, gems, bundle)
        }

        fun allKnown(): Boolean = handler.known().size == rings.size - 2

        fun getBonus(target: Char, type: Class<out RingBuff>): Int = target.buffs(type).sumBy { it.level() }
    }
}
