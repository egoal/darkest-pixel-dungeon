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
package com.egoal.darkestpixeldungeon.items.armor

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.curses.*
import com.egoal.darkestpixeldungeon.items.armor.glyphs.*
import com.egoal.darkestpixeldungeon.items.unclassified.BrokenSeal
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.particles.Emitter
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.*
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

open class Armor(var tier: Int) : EquipableItem() {

    private var hitsToKnow = HITS_TO_KNOW

    var glyph: Glyph? = null
    private var seal: BrokenSeal? = null

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(UNFAMILIRIARITY, hitsToKnow)
        bundle.put(GLYPH, glyph)
        bundle.put(SEAL, seal)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        hitsToKnow = bundle.getInt(UNFAMILIRIARITY)
        if (hitsToKnow == 0) hitsToKnow = HITS_TO_KNOW

        inscribe(bundle.get(GLYPH) as Glyph?)
        seal = bundle.get(SEAL) as BrokenSeal?
    }

    override fun reset() {
        super.reset()
        // armor can be kept in bones between runs, the seal cannot.
        seal = null
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (seal != null) actions.add(AC_DETACH)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_DETACH && seal != null) {
            hero.buff(BrokenSeal.WarriorShield::class.java)?.setArmor(null)

            if (seal!!.level() > 0) degrade()

            GLog.i(M.L(Armor::class.java, "detach_seal"))
            hero.sprite.operate(hero.pos)
            if (!seal!!.collect())
                Dungeon.level.drop(seal!!, hero.pos)
            seal = null
        }
    }

    override fun doEquip(hero: Hero): Boolean {
        detach(hero.belongings.backpack)

        if (hero.belongings.armor == null || hero.belongings.armor!!.doUnequip(hero, true, false)) {
            hero.belongings.armor = this

            cursedKnown = true
            if (cursed) {
                equipCursed(hero)
                GLog.n(Messages.get(Armor::class.java, "equip_cursed"))
            }

            (hero.sprite as HeroSprite).updateArmor()
            activate(hero)

            hero.spendAndNext(time2equip(hero))
            return true

        } else {
            collect(hero.belongings.backpack)
            return false
        }
    }

    override fun activate(ch: Char) {
        if (seal != null) Buff.affect(ch, BrokenSeal.WarriorShield::class.java).setArmor(this)
    }

    fun affixSeal(seal: BrokenSeal) {
        this.seal = seal
        if (seal.level() > 0) {
            //doesn't trigger upgrading logic such as affecting curses/glyphs
            level(level() + 1)
            Badges.validateItemLevelAquired(this)
        }
        if (isEquipped(Dungeon.hero))
            Buff.affect(Dungeon.hero, BrokenSeal.WarriorShield::class.java).setArmor(this)
    }

    fun checkSeal(): BrokenSeal? = seal

    override fun time2equip(hero: Hero): Float = 2f / hero.speed()

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        if (super.doUnequip(hero, collect, single)) {
            hero.belongings.armor = null
            (hero.sprite as HeroSprite).updateArmor()

            hero.buff(BrokenSeal.WarriorShield::class.java)?.setArmor(null)

            return true
        } else {
            return false
        }
    }

    override fun isEquipped(hero: Hero): Boolean = hero.belongings.armor === this

    fun DRMax(): Int = DRMax(level())

    open fun DRMax(lvl: Int): Int {
        var effectiveTier = tier
        if (glyph != null) effectiveTier += glyph!!.tierDRAdjust()
        effectiveTier = max(0, effectiveTier)

        return max(DRMin(lvl), effectiveTier * (2 + lvl))
    }

    fun DRMin(lvl: Int = level()): Int = if (glyph is Stone) 2 + 2 * lvl else lvl

    // base: 5, 6, 7, 8, 9, 10
    // scaling: 2, 2, 2.5, 2.5, 3, 3, 3.5
    open fun MRES(lvl: Int = level()): Float = ((4f + tier) + (2f + (tier - 1) / 2 * 0.5f) * lvl) / 100f

    override fun upgrade(): Item = upgrade(false)

    fun upgrade(inscribe: Boolean): Item {
        if (inscribe && (glyph == null || glyph!!.curse())) {
            inscribe(Glyph.random())
        } else if (!inscribe && Random.Float() > 0.9f.pow(level())) {
            inscribe(null)
        }

        if (seal != null && seal!!.level() == 0)
            seal!!.upgrade()

        return super.upgrade()
    }

    // called in Hero::defenseProc
    fun proc(damage: Damage): Damage {
        var damage = damage
        if (glyph != null)
            damage = glyph!!.proc(this, damage)

        if (!levelKnown) {
            if (--hitsToKnow <= 0) {
                levelKnown = true
                GLog.w(Messages.get(Armor::class.java, "identify"))
                Badges.validateItemLevelAquired(this)
            }
        }

        return damage
    }

    override fun name(): String {
        return if (glyph != null && (cursedKnown || !glyph!!.curse()))
            glyph!!.name(super.name())
        else
            super.name()
    }

    override fun info(): String {
        var info = desc()

        if (levelKnown) {
            info += "\n\n" + Messages.get(Armor::class.java, "curr_absorb",
                    DRMin(), DRMax(), round(MRES() * 100f).toInt(), STRReq())

            if (STRReq() > Dungeon.hero.STR()) {
                info += " " + Messages.get(Armor::class.java, "too_heavy")
            } else if (Dungeon.hero.heroClass === HeroClass.ROGUE && Dungeon.hero.STR() > STRReq()) {
                info += " " + Messages.get(Armor::class.java, "excess_str")
            }
        } else {
            info += "\n\n" + Messages.get(Armor::class.java, "avg_absorb",
                    DRMin(0), DRMax(0), round(MRES(0) * 100f).toInt(), STRReq(0))

            if (STRReq(0) > Dungeon.hero.STR()) {
                info += " " + Messages.get(Armor::class.java, "probably_too_heavy")
            }
        }

        if (glyph != null && (cursedKnown || !glyph!!.curse())) {
            info += "\n\n" + Messages.get(Item::class.java, "inscribed", glyph!!.name())
            info += " " + glyph!!.desc()
        }

        if (cursed && isEquipped(Dungeon.hero)) {
            info += "\n\n" + Messages.get(Armor::class.java, "cursed_worn")
        } else if (cursedKnown && cursed) {
            info += "\n\n" + Messages.get(Armor::class.java, "cursed")
        } else if (seal != null) {
            info += "\n\n" + Messages.get(Armor::class.java, "seal_attached")
        }

        return info
    }

    override fun emitter(): Emitter? {
        if (seal == null) return super.emitter()
        val emitter = Emitter()
        emitter.pos(10f, 6f)
        emitter.fillTarget = false
        emitter.pour(Speck.factory(Speck.RED_LIGHT), 0.6f)
        return emitter
    }

    override fun random(): Item {
        val roll = Random.Float()
        when {
            roll < 0.3f -> {
                inscribe(Glyph.randomCurse())
                cursed = true
            }
            roll < 0.75f -> {
            }
            roll < 0.95f -> upgrade(1)
            else -> upgrade(2)
        }

        if (Random.Int(6) == 0)
            inscribe()

        return this
    }

    fun STRReq(): Int = STRReq(level())

    open fun STRReq(lvl: Int): Int {
        var lvl = lvl
        lvl = max(0, lvl)
        var effectiveTier = tier.toFloat()
        if (glyph != null) effectiveTier += glyph!!.tierSTRAdjust()
        effectiveTier = max(0f, effectiveTier)

        return 8 + round(effectiveTier * 2f).toInt() - (lvl + 1) / 2 // +1, +3, +5, +7

        //strength req decreases at +1,+3,+6,+10,etc.
        //    return (8 + Math.round(effectiveTier * 2)) - (int) (Math.sqrt(8 * lvl +
        //            1) - 1) / 2;
    }

    override fun price(): Int {
        if (seal != null) return 0

        var price = 20 * tier
        if (hasGoodGlyph()) price += price / 2

        if (cursedKnown && (cursed || hasCurseGlyph())) {
            price /= 2
        }
        if (levelKnown && level() > 0) {
            price *= level() + 1
        }

        if (price < 1) price = 1

        return price
    }

    fun inscribe(glyph: Glyph?): Armor {
        this.glyph = glyph

        return this
    }

    fun inscribe(): Armor {
        val oldGlyphClass = glyph?.javaClass
        var gl = Glyph.random()
        while (gl!!.javaClass == oldGlyphClass) {
            gl = Glyph.random()
        }

        return inscribe(gl)
    }

    fun hasGlyph(type: Class<out Glyph>): Boolean = glyph?.javaClass == type

    fun hasGoodGlyph(): Boolean = glyph?.curse() == false

    fun hasCurseGlyph(): Boolean = glyph?.curse() == true

    override fun glowing(): ItemSprite.Glowing? {
        return if (glyph != null && (cursedKnown || !glyph!!.curse()))
            glyph!!.glowing()
        else
            null
    }

    abstract class Glyph : Bundlable {
        abstract fun proc(armor: Armor, damage: Damage): Damage

        fun name(): String = if (!curse()) name(Messages.get(this, "glyph"))
        else name(Messages.get(Item::class.java, "curse"))

        fun name(armorName: String): String = M.L(this, "name", armorName)

        fun desc(): String = M.L(this, "desc")

        open fun curse(): Boolean = false

        override fun restoreFromBundle(bundle: Bundle) {}

        override fun storeInBundle(bundle: Bundle) {}

        abstract fun glowing(): ItemSprite.Glowing

        open fun tierDRAdjust(): Int = 0

        open fun tierSTRAdjust(): Float = 0f

        fun checkOwner(owner: Char): Boolean {
            if (!owner.isAlive && owner is Hero) {
                Dungeon.fail(javaClass)
                GLog.n(Messages.get(this, "killed", name()))

                Badges.validateDeathFromGlyph()
                return true

            } else {
                return false
            }
        }

        companion object {
            private val glyphs = arrayOf(
                    arrayOf(Obfuscation::class.java, Swiftness::class.java, Stone::class.java, Potential::class.java),
                    arrayOf(Brimstone::class.java, Viscosity::class.java, Repulsion::class.java,
                            Camouflage::class.java, Flow::class.java, Clarifying::class.java,
                            ChuNeng::class.java),
                    arrayOf(Affection::class.java, AntiMagic::class.java, Thorns::class.java, Tough::class.java)
            )
            private val chances = floatArrayOf(55f, 30f, 15f)

            private val curses = arrayOf(
                    AntiEntropy::class.java, Corrosion::class.java, Displacement::class.java, Entanglement::class.java,
                    Metabolism::class.java, Multiplicity::class.java, Stench::class.java)

            fun random(): Glyph = glyphs[Random.chances(chances)].random().newInstance()

            fun randomCurse(): Glyph = Random.oneOf(*curses).newInstance()
        }

    }

    companion object {
        private const val HITS_TO_KNOW = 10

        protected const val AC_DETACH = "DETACH"

        private const val UNFAMILIRIARITY = "unfamiliarity"
        private const val GLYPH = "glyph"
        private const val SEAL = "seal"
    }
}
