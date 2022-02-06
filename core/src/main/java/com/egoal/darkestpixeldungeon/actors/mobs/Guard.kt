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

import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.effects.Chains
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.helmets.GuardHelmet
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.sprites.GuardSprite
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.utils.Bundle
import com.watabou.utils.Callback
import com.watabou.utils.Random

class Guard : Mob() {

    //they can only use their chains once
    private var chainsUsed = false

    init {
        PropertyConfiger.set(this, "Guard")

        spriteClass = GuardSprite::class.java
        loot = null    //see createloot.
    }

    override fun act(): Boolean {
        Dungeon.level.updateFieldOfView(this, Level.fieldOfView)

        return if (state === HUNTING && paralysed <= 0 && enemy != null &&
                enemy!!.invisible == 0 && Level.fieldOfView[enemy!!.pos] &&
                Dungeon.level.distance(pos, enemy!!.pos) < 5 && !Dungeon.level.adjacent(pos, enemy!!.pos) &&
                Random.Int(3) == 0 && chain(enemy!!.pos)) {
            false
        } else {
            super.act()
        }
    }

    private fun chain(target: Int): Boolean {
        if (chainsUsed || enemy!!.properties().contains(Char.Property.IMMOVABLE))
            return false

        val chain = Ballistica(pos, target, Ballistica.PROJECTILE)

        if (chain.collisionPos != enemy!!.pos || chain.path.size < 2 || Level.pit[chain.path[1]])
            return false
        else {
            var newPos = -1
            for (i in chain.subPath(1, chain.dist)) {
                if (!Level.solid[i] && Actor.findChar(i) == null) {
                    newPos = i
                    break
                }
            }

            if (newPos == -1) {
                return false
            } else {
                val newPosFinal = newPos
                say(Messages.get(this, "scorpion"))
                sprite.parent.add(Chains(pos, enemy!!.pos, Callback {
                    Actor.addDelayed(Pushing(enemy, enemy!!.pos, newPosFinal, Callback {
                        enemy!!.pos = newPosFinal
                        Dungeon.level.press(newPosFinal, enemy)
                        Buff.prolong(enemy!!, Cripple::class.java, 4f)
                        if (enemy === Dungeon.hero) {
                            Dungeon.hero.interrupt()
                            Dungeon.observe()
                            GameScene.updateFog()
                        }
                    }), -1f)
                    next()
                }))
            }
        }
        chainsUsed = true
        return true
    }

    override fun createLoot(): Item? {
        //first see if we drop armor, overall chance is 1/8
        val p = Random.Float()
        if (p < 0.5f) {
            var loot: Armor
            do {
                loot = Generator.ARMOR.generate() as Armor
                //50% chance of re-rolling tier 4 or 5 items
            } while (loot.tier >= 4 && Random.Int(2) == 0)
            loot.level(0)
            return loot
            //otherwise, we may drop a health potion. overall chance is 7/(8 * (7 +
            // potions dropped))
            //with 0 potions dropped that simplifies to 1/8
        } else if (p < 0.85f) {
            if (Random.Int(7 + Dungeon.limitedDrops.guardHP.count) < 6) {
                Dungeon.limitedDrops.guardHP.drop()
                return PotionOfHealing()
            }
        } else {
            return GuardHelmet().random()
        }

        return null
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(CHAINSUSED, chainsUsed)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        chainsUsed = bundle.getBoolean(CHAINSUSED)
    }

    companion object{
        private const val CHAINSUSED = "chainsused"
    }
}
