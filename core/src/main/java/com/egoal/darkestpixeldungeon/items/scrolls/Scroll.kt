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

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.artifacts.UnstableSpellbook
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Challenge
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.ItemStatusHandler
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle

import java.util.ArrayList
import java.util.HashSet

abstract class Scroll : Item() {
    protected var initials: Int = 0

    private var rune: String = ""

    var ownedByBook = false

    val isKnown: Boolean
        get() = handler.isKnown(this)

    override val isUpgradable: Boolean
        get() = false

    override val isIdentified: Boolean
        get() = isKnown

    init {
        stackable = true
        defaultAction = AC_READ

        cursed = false
        cursedKnown = true
    }

    init {
        reset()
    }

    override fun reset() {
        super.reset()
        image = handler.image(this)
        rune = handler.label(this)
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_READ)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_READ) {
            if (hero.buff(Blindness::class.java) != null) {
                GLog.w(Messages.get(this, "blinded"))
            } else if (hero.buff(UnstableSpellbook.bookRecharge::class.java)?.isCursed == true &&
                    this !is ScrollOfRemoveCurse) {
                GLog.n(Messages.get(this, "cursed"))
            } else {
                // want read
                val (first, second) = hero.canRead()
                if (!first)
                    GLog.n(second)
                else {
                    curUser = hero
                    curItem = detach(hero.belongings.backpack)!!
                    doRead()
                }
            }

        }
    }

    override fun doPickUp(hero: Hero): Boolean {
        if (hero.challenges.contains(Challenge.PathOfAsceticism)) {
            GLog.n(M.L(Challenge::class.java, "gone", name()))
            hero.next()
            return true
        }

        return super.doPickUp(hero)
    }

    protected abstract fun doRead()

    protected fun readAnimation() {
        curUser.spend(TIME_TO_READ)
        curUser.busy()
        (curUser.sprite as HeroSprite).read()
    }

    fun setKnown() {
        if (!isKnown && !ownedByBook) {
            handler.know(this)
        }

        Badges.validateAllScrollsIdentified()
    }

    override fun identify(): Item {
        setKnown()
        return super.identify()
    }

    override fun name(): String {
        return if (isKnown) name else M.L(Scroll::class.java, rune)
    }

    override fun info(): String = if (isKnown) desc() else M.L(this, "unknown_desc")

    fun initials(): Int? = if (isKnown) initials else null

    override fun price(): Int = 30 * quantity

    companion object {
        const val AC_READ = "READ"

        const val TIME_TO_READ = 1f

        private val scrolls = arrayOf(
                ScrollOfIdentify::class.java, ScrollOfMagicMapping::class.java, ScrollOfRecharging::class.java,
                ScrollOfRemoveCurse::class.java, ScrollOfTeleportation::class.java, ScrollOfUpgrade::class.java,
                ScrollOfRage::class.java, ScrollOfTerror::class.java, ScrollOfLullaby::class.java,
                // ScrollOfEnchanting::class.java,
                ScrollOfPsionicBlast::class.java, ScrollOfMirrorImage::class.java,
                ScrollOfCurse::class.java, ScrollOfLight::class.java)

        private val runes = hashMapOf<String, Int>(
                "KAUNAN" to ItemSpriteSheet.SCROLL_KAUNAN,
                "SOWILO" to ItemSpriteSheet.SCROLL_SOWILO,
                "LAGUZ" to ItemSpriteSheet.SCROLL_LAGUZ,
                "YNGVI" to ItemSpriteSheet.SCROLL_YNGVI,
                "GYFU" to ItemSpriteSheet.SCROLL_GYFU,
                "RAIDO" to ItemSpriteSheet.SCROLL_RAIDO,
                "ISAZ" to ItemSpriteSheet.SCROLL_ISAZ,
                "MANNAZ" to ItemSpriteSheet.SCROLL_MANNAZ,
                "NAUDIZ" to ItemSpriteSheet.SCROLL_NAUDIZ,
                "BERKANAN" to ItemSpriteSheet.SCROLL_BERKANAN,
                "ODAL" to ItemSpriteSheet.SCROLL_ODAL,
                "TIWAZ" to ItemSpriteSheet.SCROLL_TIWAZ,
                // "QI" to ItemSpriteSheet.SCROLL_QI,
                "LINGEL" to ItemSpriteSheet.SCROLL_LINGEL
        )

        private lateinit var handler: ItemStatusHandler<Scroll>

        fun initLabels() {
            handler = ItemStatusHandler(scrolls, runes)
        }

        fun save(bundle: Bundle) {
            handler.save(bundle)
        }

        fun saveSelectively(bundle: Bundle, items: ArrayList<Scroll>) {
            handler.saveSelectively(bundle, items)
        }

        fun restore(bundle: Bundle) {
            handler = ItemStatusHandler(scrolls, runes, bundle)
        }

        val known: HashSet<Class<out Scroll>>
            get() = handler.known()

        val unknown: HashSet<Class<out Scroll>>
            get() = handler.unknown()

        fun allKnown(): Boolean = handler.known().size == scrolls.size
    }
}
