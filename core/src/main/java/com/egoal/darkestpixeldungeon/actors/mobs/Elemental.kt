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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Chill
import com.egoal.darkestpixeldungeon.actors.buffs.Frost
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLiquidFlame
import com.egoal.darkestpixeldungeon.items.unclassified.FireButterfly
import com.egoal.darkestpixeldungeon.items.wands.WandOfFireblast
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Blazing
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ElementalSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random

import java.util.HashSet

open class Elemental : Mob() {

    init {
        PropertyConfiger.set(this, "Elemental")
        spriteClass = ElementalSprite::class.java

        flying = true
    }

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy).addElement(Damage.Element.FIRE)

    override fun attackProc(dmg: Damage): Damage {
        val enemy = dmg.to as Char
        if (Random.Int(2) == 0)
            Buff.affect(enemy, Burning::class.java).reignite(enemy)

        return dmg
    }

    override fun defenseProc(dmg: Damage): Damage {
        if (dmg.from is Hero) {
            val hero = dmg.from as Hero
            if (Dungeon.level.adjacent(hero.pos, pos) && Random.Int(3) == 0) {
                val weapon = hero.belongings.weapon
                if (weapon is MeleeWeapon && weapon.enchantment == null) {
                    weapon.enchant(Blazing::class.java, 8f)
                }
            }
        }

        return super.defenseProc(dmg)
    }

    override fun add(buff: Buff) {
        if (buff is Burning) {
            if (HP < HT) {
                HP += 1
                sprite.emitter().burst(Speck.factory(Speck.HEALING), 1)
            }
        } else if (buff is Frost || buff is Chill) {
            if (Level.water[this.pos])
                takeDamage(Damage(Random.NormalIntRange(HT / 2, HT), buff, this)
                        .addElement(Damage.Element.ICE))
            else
                takeDamage(Damage(Random.NormalIntRange(1, HT * 2 / 3), buff,
                        this).addElement(Damage.Element.ICE))
        } else {
            super.add(buff)
        }
    }

    override fun createLoot(): Item? {
        return if (Random.Float() < 0.4f) PotionOfLiquidFlame() else FireButterfly()
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    companion object {

        private val IMMUNITIES = hashSetOf<Class<*>>(Burning::class.java, Blazing::class.java, WandOfFireblast::class.java)
    }
}
