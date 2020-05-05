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
package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MirrorSprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.HashSet

class MirrorImage : NPC() {
    var tier: Int = 0

    private var damage: Int = 0

    init {
        spriteClass = MirrorSprite::class.java

        state = HUNTING
        camp = Camp.HERO
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(TIER, tier)
        bundle.put(DAMAGE, damage)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        tier = bundle.getInt(TIER)
        damage = bundle.getInt(DAMAGE)
    }

    fun duplicate(hero: Hero) {
        tier = hero.tier()
        damage = hero.giveDamage(Nobody.INSTANCE).value
    }

    // accurate
    override fun giveDamage(target: Char): Damage = Damage(damage, this, target).addFeature(Damage.Feature.ACCURATE)

    override fun attackProc(damage: Damage): Damage {
        super.attackProc(damage)

        destroy()
        sprite.die()

        return damage
    }

    override fun chooseEnemy(): Char? {
        if (enemy == null || !enemy.isAlive) {
            val enemies = Dungeon.level.mobs.filter { it.camp == Camp.ENEMY && Level.fieldOfView[it.pos] }
            enemy = if (enemies.isNotEmpty()) enemies.random() else null
        }

        return enemy
    }

    override fun sprite(): CharSprite {
        val s = super.sprite()
        (s as MirrorSprite).updateArmor(tier)
        return s
    }

    override fun interact(): Boolean {
        swapPosition(Dungeon.hero)

        Dungeon.hero.spend(1 / Dungeon.hero.speed())
        Dungeon.hero.busy()

        return true
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    companion object {
        private const val TIER = "tier"
        private const val DAMAGE = "damage"

        private val IMMUNITIES = hashSetOf<Class<*>>(ToxicGas::class.java, VenomGas::class.java, Burning::class.java)
    }
}