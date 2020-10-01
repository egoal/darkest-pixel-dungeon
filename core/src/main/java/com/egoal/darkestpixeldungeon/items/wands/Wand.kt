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
package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.QuickZap
import com.egoal.darkestpixeldungeon.actors.hero.perks.StealthCaster
import com.egoal.darkestpixeldungeon.actors.hero.perks.WandPerception
import com.egoal.darkestpixeldungeon.items.bags.Bag
import com.egoal.darkestpixeldungeon.items.bags.WandHolster
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Callback
import com.watabou.utils.PointF
import com.watabou.utils.Random

import java.util.ArrayList
import kotlin.math.min
import kotlin.math.pow

abstract class Wand(val isMissile: Boolean) : Item() {
    var maxCharges = initialCharges()
    var curCharges = maxCharges
    var partialCharge = 0f

    private var charger: Charger? = null

    private var curChargeKnown = false

    private var usagesToKnow = USAGES_TO_KNOW

    protected var collisionProperties = Ballistica.MAGIC_BOLT

    override val isIdentified: Boolean
        get() = super.isIdentified && curChargeKnown

    init {
        defaultAction = AC_ZAP
        usesTargeting = true
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (curCharges > 0 || !curChargeKnown) {
            actions.add(AC_ZAP)
        }

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_ZAP) {
            curUser = hero
            curItem = this
            GameScene.selectCell(zapper)
        }
    }

    protected abstract fun onZap(attack: Ballistica)

    // battle mage.
    abstract fun onHit(staff: MagesStaff, damage: Damage)

    override fun collect(container: Bag): Boolean {
        if (super.collect(container)) {
            if (container.owner != null) {
                if (container is WandHolster)
                    charge(container.owner, WandHolster.HOLSTER_SCALE_FACTOR)
                else
                    charge(container.owner)
            }
            return true
        } else {
            return false
        }
    }

    fun charge(owner: Char) {
        if (charger == null) charger = Charger()
        charger!!.attachTo(owner)
    }

    fun charge(owner: Char, chargeScaleFactor: Float) {
        charge(owner)
        charger!!.scalingFactor = chargeScaleFactor
    }

    public override fun onDetach() {
        stopCharging()
    }

    fun stopCharging() {
        charger?.detach()
        charger = null
    }

    override fun level(value: Int) {
        super.level(value)
        updateLevel()
    }

    override fun identify(): Item {
        curChargeKnown = true
        super.identify()

        updateQuickslot()
        return this
    }

    override fun info(): String {
        var desc = desc()

        desc += "\n\n" + statsDesc()

        if (cursed && cursedKnown)
            desc += "\n\n" + Messages.get(Wand::class.java, "cursed")

        return desc
    }

    open fun statsDesc(): String = M.L(this, "stats_desc")

    override fun status(): String? =
            if (levelKnown)
                if (curChargeKnown) "$curCharges/$maxCharges" else "?/$maxCharges"
            else null

    override fun upgrade(): Item {
        super.upgrade()

        if (Random.Float() > 0.9f.pow(level()))
            cursed = false

        updateLevel()
        curCharges = min(curCharges + 1, maxCharges)
        updateQuickslot()

        return this
    }

    override fun degrade(): Item {
        super.degrade()

        updateLevel()
        updateQuickslot()

        return this
    }

    open fun updateLevel() {
        maxCharges = min(initialCharges() + level(), 10)
        curCharges = min(curCharges, maxCharges)
    }

    protected open fun initialCharges(): Int = 2

    protected open fun chargesPerCast(): Int = 1

    open fun fx(bolt: Ballistica, callback: Callback) {
        MagicMissile.whiteLight(curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    open fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0xFFFFFF)
        particle.am = 0.3f
        particle.setLifespan(1f)
        particle.speed.polar(Random.Float(PointF.PI2), 2f)
        particle.setSize(1f, 2.5f)
        particle.radiateXY(1f)
    }

    fun wandUsed() {
        usagesToKnow -= if (cursed) 1 else chargesPerCast()
        curCharges -= if (cursed) 1 else chargesPerCast()
        if (!isIdentified && usagesToKnow <= 0) {
            identify()
            GLog.w(Messages.get(Wand::class.java, "identify", name()))
        } else {
            curUser.heroPerk.get(WandPerception::class.java)?.onWandUsed(this)

            updateQuickslot()
        }

        val zapTime = if (curUser.heroPerk.has(QuickZap::class.java)) 0.45f else TIME_TO_ZAP
        curUser.spendAndNext(zapTime)
    }

    override fun random(): Item {
        var n = 0

        if (Random.Int(3) == 0) {
            n++
            if (Random.Int(5) == 0) {
                n++
            }
        }

        upgrade(n)
        if (Random.Float() < 0.3f) {
            cursed = true
            cursedKnown = false
        }

        return this
    }

    override fun price(): Int {
        var price = 75
        if (cursed && cursedKnown) price /= 2

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

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(UNFAMILIRIARITY, usagesToKnow)
        bundle.put(CUR_CHARGES, curCharges)
        bundle.put(CUR_CHARGE_KNOWN, curChargeKnown)
        bundle.put(PARTIALCHARGE, partialCharge)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        usagesToKnow = bundle.getInt(UNFAMILIRIARITY)
        if (usagesToKnow == 0) {
            usagesToKnow = USAGES_TO_KNOW
        }
        curCharges = bundle.getInt(CUR_CHARGES)
        curChargeKnown = bundle.getBoolean(CUR_CHARGE_KNOWN)
        partialCharge = bundle.getFloat(PARTIALCHARGE)
    }

    inner class Charger : Buff() {
        var scalingFactor = NORMAL_SCALE_FACTOR

        override fun attachTo(target: Char): Boolean {
            super.attachTo(target)

            return true
        }

        override fun act(): Boolean {
            if (curCharges < maxCharges)
                recharge()

            if (partialCharge >= 1 && curCharges < maxCharges) {
                partialCharge--
                curCharges++
                updateQuickslot()
            }

            spend(TICK)

            return true
        }

        private fun recharge() {
            val missingCharges = maxCharges - curCharges

            val turnsToCharge = BASE_CHARGE_DELAY + SCALING_CHARGE_ADDITION * scalingFactor.pow(missingCharges)

            val lock = target.buff(LockedFloor::class.java)
            if (lock == null || lock.regenOn())
                partialCharge += 1f / turnsToCharge * Dungeon.hero.wandChargeFactor()

            val bonus = target.buff(Recharging::class.java)
            if (bonus != null && bonus.remainder() > 0f) {
                partialCharge += CHARGE_BUFF_BONUS * bonus.remainder()
            }
        }

        fun gainCharge(charge: Float) {
            partialCharge += charge
            while (partialCharge >= 1f) {
                curCharges++
                partialCharge--
            }
            curCharges = min(curCharges, maxCharges)
            updateQuickslot()
        }
    }

    companion object {
        // charger
        private const val BASE_CHARGE_DELAY = 10f
        private const val SCALING_CHARGE_ADDITION = 40f
        private const val NORMAL_SCALE_FACTOR = 0.875f

        private const val CHARGE_BUFF_BONUS = 0.25f

        //
        private const val USAGES_TO_KNOW = 20

        const val AC_ZAP = "ZAP"

        private const val TIME_TO_ZAP = 1f

        private const val UNFAMILIRIARITY = "unfamiliarity"
        private const val CUR_CHARGES = "curCharges"
        private const val CUR_CHARGE_KNOWN = "curChargeKnown"
        private const val PARTIALCHARGE = "partialCharge"

        protected var zapper: CellSelector.Listener = object : CellSelector.Listener {

            override fun onSelect(target: Int?) {

                if (target != null) {
                    val curWand = curItem as Wand

                    val shot = Ballistica(curUser.pos, target, curWand.collisionProperties)
                    val cell = shot.collisionPos

                    if (target == curUser.pos || cell == curUser.pos) {
                        GLog.i(Messages.get(Wand::class.java, "self_target"))
                        return
                    }

                    curUser.sprite.zap(cell)

                    //attempts to targetpos the cell aimed at if something is there,
                    // otherwise targets the collision pos.
                    if (Actor.findChar(target) != null)
                        QuickSlotButton.target(Actor.findChar(target))
                    else
                        QuickSlotButton.target(Actor.findChar(cell))

                    val cost = if (curWand.cursed) 1 else curWand.chargesPerCast()
                    if (curWand.curCharges < cost) {
                        GLog.w(Messages.get(Wand::class.java, "fizzles"))
                        return
                    }

                    // cast
                    curUser.busy()

                    if (curWand.cursed) {
                        CursedWand.cursedZap(curWand, curUser, Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT))
                        if (!curWand.cursedKnown) {
                            curWand.cursedKnown = true
                            GLog.n(Messages.get(Wand::class.java, "curse_discover", curWand.name()))
                        }
                    } else {
                        curWand.cursedKnown = true
                        curWand.fx(shot, Callback {
                            curWand.onZap(shot)
                            curWand.wandUsed()
                        })
                    }
                    
                    if (!curUser.heroPerk.has(StealthCaster::class.java))
                        Invisibility.dispel()
                }
            }

            override fun prompt(): String = M.L(Wand::class.java, "prompt")
        }
    }
}
