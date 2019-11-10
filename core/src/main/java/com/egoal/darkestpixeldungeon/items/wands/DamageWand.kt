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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.WandPiercing
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.BlastParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Random
import kotlin.math.round

// for wands that directly damage a target
// wands with AOE effects count here (e.g. fireblast), but wands with indrect
// damage do not (e.g. venom, transfusion)
abstract class DamageWand : Wand() {
    fun min(): Int = min(level())

    abstract fun min(lvl: Int): Int

    fun max(): Int = max(level())

    abstract fun max(lvl: Int): Int

    open fun giveDamage(enemy: Char): Damage {
        val dmg = Damage(damageRoll(), Item.curUser, enemy).type(Damage.Type.MAGICAL)
        Item.curUser!!.procWandDamage(dmg)
        return dmg
    }

    fun damageRoll(): Int = round(Random.NormalIntRange(min(), max()) * Dungeon.hero.arcaneFactor()).toInt()

    fun damageRoll(lvl: Int): Int = round(Random.NormalIntRange(min(lvl), max(lvl)) * Dungeon.hero.arcaneFactor()).toInt()

    override fun statsDesc(): String = if (levelKnown) M.L(this, "stats_desc", min(), max())
    else M.L(this, "stats_desc", min(0), max(0))

    //todo: refactor the missile handle
    open fun onMissileHit(char: Char, hero: Hero, dmg: Damage) {
        hero.heroPerk.get(WandPiercing::class.java)?.onHit(char)

        //todo: critical effects
        if (dmg.isFeatured(Damage.Feature.CRITICAL))
            CellEmitter.center(char.pos).burst(BlastParticle.FACTORY, 10)
    }
}
