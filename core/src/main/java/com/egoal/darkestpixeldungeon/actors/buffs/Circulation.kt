package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Random
import kotlin.math.min
import kotlin.math.round

class Circulation : Buff() {
    private var amount = 0
    private var last_ = 0 // 0:init, 1:wand: 2:weapon
    private var duration = 0

    private var Last: Int
        get() = last_
        set(value) {
            last_ = value
            BuffIndicator.refreshHero()
            duration = 10 + amount * 5
        }

    fun wandProc(wand: Wand, damage: Damage) {
        when (Last) {
            0 -> {
            }
            1 -> {
                amount = 0 // reset
            }
            2 -> {
                amount = min(5, amount + 1)
            }
        }

        if (amount > 0) {
            val r = 1f + 0.1f * amount + 0.01f * amount * amount
            damage.value = round(damage.value * r).toInt()
            if (amount == 5) damage.addFeature(Damage.Feature.CRITICAL)
        }

        Last = 1
    }

    fun weaponProc(weapon: Weapon, damage: Damage) {
        when (Last) {
            0 -> {
            }
            1 -> {
                amount = min(5, amount + 1)
            }
            2 -> {
                amount = 0
            }
        }

        if (amount > 0) {
            val t = when (weapon) {
                is MeleeWeapon -> weapon.tier
                is MissileWeapon -> weapon.tier
                else -> 0
            }
            damage.value += Random.NormalIntRange(0, amount * (t + 1))
            if (amount == 5) damage.addFeature(Damage.Feature.ACCURATE)
        }

        Last = 2
    }

    fun evasionFactor(): Float {
        return 1f + 0.1f * amount + 0.01f * amount * amount
    }

    override fun act(): Boolean {
        if (amount > 0) {
            duration -= 1
            if (duration <= 0) {
                // reset
                affect(target, Recharging::class.java, 0.5f + amount / 2f)

                amount = 0
                Last = 0
            }
        }

        spend(TICK)
        return true
    }

    override fun toString(): String = M.L(this, "name")

    override fun desc(): String = M.L(this, "desc", amount)

    override fun icon(): Int = BuffIndicator.CIRCULATION + last_
}