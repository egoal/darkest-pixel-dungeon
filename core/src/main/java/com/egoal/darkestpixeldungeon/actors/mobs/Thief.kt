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
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption
import com.egoal.darkestpixeldungeon.actors.buffs.Roots
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.artifacts.MasterThievesArmband
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.items.unclassified.Honeypot
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.melee.RedHandleDagger
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ThiefSprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.HashSet

open class Thief : Mob() {
    var item: Item? = null

    init {
        PropertyConfiger.set(this, "Thief")

        spriteClass = ThiefSprite::class.java

        FLEEING = Fleeing()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ITEM, item)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        item = bundle.get(ITEM) as Item?
    }

    override fun speed(): Float = if (item != null) super.speed() * 0.83333f
    else super.speed()

    override fun attackDelay(): Float = 0.5f

    override fun die(cause: Any?) {
        super.die(cause)

        if (item != null) {
            Dungeon.level.drop(item!!, pos).sprite.drop()
            //updates position
            if (item is Honeypot.ShatteredPot)
                (item as Honeypot.ShatteredPot).setHolder(this)
        }
    }

    override fun createLoot(): Item =
            if (!Dungeon.limitedDrops.armband.dropped() && Random.Float() < 0.1f) {
                Dungeon.limitedDrops.armband.drop()
                MasterThievesArmband().identify()
            } else if (Random.Float() < 0.3f) RedHandleDagger().random()
            else Gold(Random.NormalIntRange(80, 200))

    override fun attackProc(dmg: Damage): Damage {
        if (!isAlive) return dmg
        val enemy = dmg.to as Char
        if (item == null && enemy is Hero && steal(enemy)) {
            enemy.takeDamage(Damage(Random.IntRange(1, 5), this, enemy).type(Damage.Type.MENTAL))
            state = FLEEING
        }

        return dmg
    }

    override fun defenseProc(damage: Damage): Damage {
        if (state === FLEEING) {
            Dungeon.level.drop(Gold(), pos).sprite.drop()
        }

        return super.defenseProc(damage)
    }

    protected open fun steal(hero: Hero): Boolean {

        val item = hero.belongings.randomUnequipped()

        if (item != null && !item.unique && item.level() < 1) {

            GLog.w(Messages.get(Thief::class.java, "stole", item.name()))
            Dungeon.quickslot.clearItem(item)
            item.updateQuickslot()

            // process on the honey pot
            if (item is Honeypot) {
                this.item = item.shatter(this, this.pos)
                item.detach(hero.belongings.backpack)
            } else {
                this.item = item.detach(hero.belongings.backpack)
                if (item is Honeypot.ShatteredPot)
                    item.setHolder(this)
            }

            return true
        } else {
            return false
        }
    }

    override fun description(): String {
        var desc = super.description()

        if (item != null) {
            desc += Messages.get(this, "carries", item!!.name())
        }

        return desc
    }

    override fun immunizedBuffs(): HashSet<Class<*>> {
        IMMUS.add(Roots::class.java)
        return IMMUS
    }

    private inner class Fleeing : Mob.Fleeing() {
        override fun nowhereToRun() {
            if (buff(Terror::class.java) == null && buff(Corruption::class.java) == null) {
                if (enemySeen) {
                    sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Mob::class.java,
                            "rage"))
                    state = HUNTING
                } else {

                    var count = 32
                    var newPos: Int
                    do {
                        newPos = Dungeon.level.randomRespawnCell()
                        if (count-- <= 0) {
                            break
                        }
                    } while (newPos == -1 || Dungeon.visible[newPos] || Dungeon.level
                                    .distance(newPos, pos) < count / 3)

                    if (newPos != -1) {

                        if (Dungeon.visible[pos])
                            CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6)
                        pos = newPos
                        sprite.place(pos)
                        sprite.visible = Dungeon.visible[pos]
                        if (Dungeon.visible[pos])
                            CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6)

                    }

                    if (item != null) {
                        GLog.n(Messages.get(Thief::class.java, "escapes", item!!.name()))
                        if (Dungeon.hero.isAlive)
                            Dungeon.hero.takeDamage(Damage(Random.IntRange(2, 6), this, Dungeon.hero).type(Damage.Type.MENTAL).addFeature(Damage.Feature.PURE))
                    }
                    item = null
                    state = WANDERING
                }
            } else {
                super.nowhereToRun()
            }
        }
    }

    companion object {
        private const val ITEM = "item"

        private val IMMUS = HashSet<Class<*>>()
    }
}
