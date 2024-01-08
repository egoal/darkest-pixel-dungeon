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

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random

abstract class KindOfWeapon : EquipableItem() {
    override fun isEquipped(hero: Hero): Boolean = hero.belongings.weapon === this

    override fun doEquip(hero: Hero): Boolean {
        detachAll(hero.belongings.backpack)

        if (hero.belongings.weapon == null || hero.belongings.weapon!!.doUnequip(hero, true)) {

            hero.belongings.weapon = this
            activate(hero)

            updateQuickslot()

            cursedKnown = true
            if (cursed) {
                equipCursed(hero)
                GLog.n(M.L(KindOfWeapon::class.java, "cursed"))
            }

            hero.spendAndNext(TIME_TO_EQUIP)
            return true

        } else {
            collect(hero.belongings.backpack)
            return false
        }
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        if (super.doUnequip(hero, collect, single)) {
            hero.belongings.weapon = null
            return true
        } else {
            return false
        }
    }

    fun min(): Int = min(level())

    fun max(): Int = max(level())

    abstract fun min(lvl: Int): Int

    abstract fun max(lvl: Int): Int

    open fun canSurpriseAttack(): Boolean = true

    // damage attach to normal attack, called in give damage
    open fun giveDamage(owner: Hero, target: Char): Damage = Damage(Random.NormalIntRange(min(), max()), owner, target)

    open fun accuracyFactor(hero: Hero, target: Char): Float = 1f

    open fun evasionFactor(hero: Hero, target: Char): Float = 1f

    open fun speedFactor(hero: Hero): Float = 1f

    open fun reachFactor(hero: Hero): Int = 1

    open fun defendDamage(dmg: Damage): Damage = dmg

    // process, called in attackProc
    open fun proc(dmg: Damage): Damage = dmg

    companion object {
        const val TIME_TO_EQUIP = 1f
    }
}
