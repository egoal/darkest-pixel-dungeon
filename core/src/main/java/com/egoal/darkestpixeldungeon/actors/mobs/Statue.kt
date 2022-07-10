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
package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Database
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Dementage
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Vampiric
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.SpikeShield
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.StatueSprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.*

open class Statue : Mob() {

    protected var weapon: Weapon

    init {
        spriteClass = StatueSprite::class.java

        state = PASSIVE

        Config = Database.ConfigOfMob("Statue")!!.copy(
                MaxHealth = 15 + Dungeon.depth * 5,
                DefendSkill = 4f + Dungeon.depth,
                AttackSkill = 9f + Dungeon.depth
        )

        do {
            weapon = Generator.WEAPON.generate() as Weapon
        } while (weapon !is MeleeWeapon || weapon.cursed || weapon is SpikeShield)

        weapon.identify()
        weapon.inscribe()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(WEAPON, weapon)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        weapon = bundle.get(WEAPON) as Weapon
    }

    override fun act(): Boolean {
        if (Dungeon.visible[pos]) {
            Journal.add(name)
        }
        return super.act()
    }

    override fun giveDamage(target: Char): Damage = Damage(Random.NormalIntRange(weapon.min(), weapon.max()), this, target)

    override fun defendDamage(dmg: Damage): Damage {
        weapon.defendDamage(dmg)
        dmg.value -= Random.NormalIntRange(0, Dungeon.depth)

        return dmg
    }

    override fun accRoll(damage: Damage): Float = super.accRoll(damage) * weapon.ACC

    override fun attackSpeed(): Float = 1f / weapon.DLY

    override fun canAttack(enemy: Char): Boolean = Dungeon.level.distance(pos, enemy.pos) <= weapon.RCH

    override fun takeDamage(dmg: Damage): Int {
        if (state === PASSIVE) {
            state = HUNTING
        }

        return super.takeDamage(dmg)
    }

    override fun attackProc(damage: Damage): Damage = weapon.proc(damage)

    override fun beckon(cell: Int) {
        // Do nothing
    }

    override fun die(src: Any?) {
        Dungeon.level.drop(weapon, pos).sprite.drop()
        super.die(src)
    }

    override fun destroy() {
        Journal.remove(name)
        super.destroy()
    }

    override fun reset(): Boolean {
        state = PASSIVE
        return true
    }

    override fun description(): String {
        return Messages.get(this, "desc", weapon.name())
    }

    public override fun resistDamage(dmg: Damage): Damage {
        if (dmg.isFeatured(Damage.Feature.DEATH))
            dmg.value = dmg.value * 4 / 5
        return super.resistDamage(dmg)
    }

    override fun immunizedBuffs(): HashSet<Class<*>> {
        return IMMUNITIES
    }

    companion object {

        private const val WEAPON = "weapon"

        private val IMMUNITIES = hashSetOf<Class<*>>(Vampiric::class.java, Dementage::class.java)
    }
}
