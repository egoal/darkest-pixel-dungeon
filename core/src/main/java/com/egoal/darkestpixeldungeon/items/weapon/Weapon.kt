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
package com.egoal.darkestpixeldungeon.items.weapon

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.EnchantmentExtraDamage
import com.egoal.darkestpixeldungeon.actors.hero.perks.ExtraStrengthPower
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KindOfWeapon
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.items.rings.RingOfFuror
import com.egoal.darkestpixeldungeon.items.rings.RingOfSharpshooting
import com.egoal.darkestpixeldungeon.items.weapon.curses.Wayward
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Projecting
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

abstract class Weapon : KindOfWeapon() {
    var ACC = 1f  // Accuracy modifier
    var DLY = 1f  // Speed modifier
    var RCH = 1    // Reach modifier (only applies to melee hits)

    var imbue = Imbue.NONE

    private var hitsToKnow = HITS_TO_KNOW

    var inscription: Inscription? = null
    var enchantment: Enchantment? = null

    enum class Imbue(private val damageFactor: Float, private val delayFactor: Float, private val strFix: Int) {
        NONE(1.0f, 1.00f, 0),
        LIGHT(0.7f, 0.67f, -1),
        HEAVY(1.5f, 1.67f, 1);

        fun strFix(): Int {
            return strFix
        }

        fun damageFactor(dmg: Int): Int = round(dmg * damageFactor).toInt()

        fun delayFactor(dly: Float): Float = dly * delayFactor
    }

    override fun proc(dmg: Damage): Damage {
        var dmg = dmg
        if (inscription != null) dmg = inscription!!.proc(this, dmg)
        if (enchantment != null) {
            dmg = enchantment!!.proc(this, dmg)
            if (dmg.from is Hero) {
                Dungeon.hero.heroPerk.get(EnchantmentExtraDamage::class.java)?.procDamage(dmg)
            }
        }

        if (!levelKnown) {
            if (--hitsToKnow <= 0) {
                levelKnown = true
                GLog.i(Messages.get(Weapon::class.java, "identify"))
                Badges.validateItemLevelAquired(this)
            }
        }

        return dmg
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(UNFAMILIRIARITY, hitsToKnow)
        bundle.put(INSCRIPTION, inscription)
        bundle.put(ENCHANTMENT, enchantment)
        bundle.put(IMBUE, imbue)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        hitsToKnow = bundle.getInt(UNFAMILIRIARITY)
        inscription = bundle.get(INSCRIPTION) as Inscription?
        enchantment = bundle.get(ENCHANTMENT) as Enchantment?
        imbue = bundle.getEnum(IMBUE, Imbue::class.java)
    }

    override fun accuracyFactor(hero: Hero, target: Char): Float {
        var encumbrance = STRReq() - hero.STR()

        if (isInscribed(Wayward::class.java))
            encumbrance = max(3, encumbrance + 3)

        var acc = ACC

        if (this is MissileWeapon) {
            val bonus = Ring.getBonus(hero, RingOfSharpshooting.Aim::class.java)
            acc *= 1.1f.pow(bonus)
        }

        return if (encumbrance > 0) acc / 1.5f.pow(encumbrance) else acc
    }

    override fun speedFactor(hero: Hero): Float {
        val encumbrance = STRReq() - hero.STR()

        var dly = imbue.delayFactor(DLY)

        val bonus = Ring.getBonus(hero, RingOfFuror.Furor::class.java)

        dly = 0.25f + (dly - 0.25f) * 0.8f.pow(bonus)

        return if (encumbrance > 0) dly * 1.2f.pow(encumbrance) else dly
    }

    override fun reachFactor(hero: Hero): Int = if (isInscribed(Projecting::class.java)) RCH + 1 else RCH

    override fun giveDamage(hero: Hero, target: Char): Damage {
        val dmg = super.giveDamage(hero, target)

        // extra damage
        val exStr = hero.STR() - STRReq()
        if (exStr > 0) {
            dmg.value += Random.Int(1, exStr)
            hero.heroPerk.get(ExtraStrengthPower::class.java)?.affectDamage(dmg, exStr)
        }

        dmg.value = imbue.damageFactor(dmg.value)
        return dmg
    }

    fun STRReq(): Int = STRReq(level())

    abstract fun STRReq(lvl: Int): Int

    open fun upgrade(inscribe: Boolean): Item {
        if (inscribe && (inscription == null || inscription!!.curse)) {
            inscribe()
        } else if (!inscribe && Random.Float() > 0.9f.pow(level())) {
            inscribe(null)
        }

        return super.upgrade()
    }

    override fun name(): String {
        return if (inscription != null && (cursedKnown || !inscription!!.curse)) inscription!!.name(super.name())
        else super.name()
    }

    override fun random(): Item {
        val roll = Random.Float()
        if (roll < 0.3f) {
            //30% chance to be level 0 and cursed
            inscribe(Inscription.randomNegative())
            cursed = true
            return this
        } else if (roll < 0.75f) {
            //45% chance to be level 0
        } else if (roll < 0.95f) {
            //15% chance to be +1
            upgrade(1)
        } else {
            //5% chance to be +2
            upgrade(2)
        }

        //if not cursed, 10% chance to be enchanted (7% overall)
        if (Random.Int(10) == 0)
            inscribe()

        return this
    }

    open fun inscribe(insc: Inscription?): Weapon {
        inscription = insc
        return this
    }

    open fun inscribe(): Weapon {
        val old = inscription?.javaClass
        var new = Inscription.randomPositive()
        while (new.javaClass == old) new = Inscription.randomPositive()

        return inscribe(new)
    }

    open fun isInscribed(type: Class<out Inscription>): Boolean = inscription?.javaClass == type

    fun hasGoodInscription(): Boolean = inscription?.curse == false
    fun hasCurseInscription(): Boolean = inscription?.curse == true

    open fun enchant(type: Class<out Enchantment>, duration: Float): Weapon {
        // prolong
        if (enchantment?.javaClass == type)
            enchantment!!.left = max(enchantment!!.left, duration)
        else
            enchantment = type.newInstance().apply { left = duration }

        GLog.w(M.L(Weapon::class.java, "on_enchanted", name(), enchantment!!.name()))

        return this
    }

    open fun hasEnchant(type: Class<out Enchantment>): Boolean = enchantment?.javaClass == type

    override fun glowing(): ItemSprite.Glowing? = enchantment?.glowing()

    companion object {
        private const val HITS_TO_KNOW = 30

        private const val UNFAMILIRIARITY = "unfamiliarity"
        private const val INSCRIPTION = "inscription"
        private const val ENCHANTMENT = "enchantment"
        private const val IMBUE = "imbue"
    }
}
