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

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.artifacts.HandOfTheElder
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.SkeletonSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class Skeleton : Mob() {
    init {
        spriteClass = SkeletonSprite::class.java

        HT = 25
        HP = HT
        defenseSkill = 9

        EXP = 5
        maxLvl = 10

        loot = Generator.WEAPON.generate()
        lootChance = 0.175f

        properties.add(Property.UNDEAD)

        addResistances(Damage.Element.FIRE, .75f)
        addResistances(Damage.Element.HOLY, .667f)
    }

    override fun giveDamage(target: Char): Damage {
        return Damage(Random.NormalIntRange(2, 10), this, target)
    }

    override fun defendDamage(dmg: Damage): Damage {
        dmg.value -= Random.NormalIntRange(1, 2)
        return dmg
    }

    override fun die(cause: Any) {

        super.die(cause)

        var heroKilled = false
        for (i in PathFinder.NEIGHBOURS8) {
            Actor.findChar(i + pos)?.let {
                if (it.isAlive) {
                    val dmg = Damage(Random.NormalIntRange(4, 10), this@Skeleton, it).addElement(Damage.Element.FIRE)
                    it.takeDamage(it.defendDamage(dmg))
                    if (it === Dungeon.hero && !it.isAlive)
                        heroKilled = true
                }
            }
        }

        if (Dungeon.visible[pos]) Sample.INSTANCE.play(Assets.SND_BONES)

        if (heroKilled) {
            Dungeon.fail(javaClass)
            GLog.n(Messages.get(this, "explo_kill"))
        }
    }

    override fun createLoot(): Item? {
        return if (!Dungeon.limitedDrops.handOfElder.dropped() && Random.Float() < 0.04f) {
            Dungeon.limitedDrops.handOfElder.drop()
            HandOfTheElder().random()
        } else {
            var loot: Item
            do {
                loot = Generator.WEAPON.generate()
                //50% chance of re-rolling tier 4 or 5 items
            } while (loot is MeleeWeapon && loot.tier >= 4 &&
                    Random.Int(2) == 0)
            loot.level(0)

            loot
        }
    }

    override fun attackSkill(target: Char): Int = 12
}
