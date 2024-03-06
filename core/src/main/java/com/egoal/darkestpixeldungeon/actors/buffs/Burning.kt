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
package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Thief
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Brimstone
import com.egoal.darkestpixeldungeon.items.food.ChargrilledMeat
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance.Resistance
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Burning : Buff(), Hero.Doom {
    private var left: Float = 0f
    private var burnedSomething = false

    init {
        type = buffType.NEGATIVE
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEFT, left)
        bundle.put(BURNED, burnedSomething)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        left = bundle.getFloat(LEFT)
        burnedSomething = bundle.getBoolean(BURNED)
    }

    override fun act(): Boolean {

        if (target.isAlive) {

            //maximum damage scales from 6 to 2 depending on remaining hp.
            val maxval = 3 + 4 * target.HP / target.HT + Dungeon.depth / 5 * 5
            val damage = Random.Int(1, maxval)
            Buff.detach(target, Chill::class.java)

            if (target is Hero) {

                val hero = target as Hero

                if (hero.belongings.armor != null && hero.belongings.armor!!.hasGlyph(Brimstone::class.java)) {
                    // wear armor with brimstone

                    var heal = hero.belongings.armor!!.level() / 5f
                    if (Random.Float() < heal % 1) heal++
                    if (heal >= 1 && hero.HP < hero.HT) {
                        hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), heal.toInt())
                        hero.HP = Math.min(hero.HT, hero.HP + heal.toInt())
                    }

                } else {
                    hero.takeDamage(Damage(0, this, hero).setAdditionalDamage(Damage.Element.Fire, damage))

                    // burn something
                    if (!burnedSomething) {
                        var item = hero.belongings.randomUnequipped()
                        if (item is Scroll && item !is ScrollOfUpgrade) {

                            item = item.detach(hero.belongings.backpack)
                            GLog.w(Messages.get(this, "burnsup", Messages.capitalize(item!!
                                    .toString())))

                            Heap.burnFX(hero.pos)

                            burnedSomething = true
                        } else if (item is MysteryMeat) {

                            item = item.detach(hero.belongings.backpack)
                            val steak = ChargrilledMeat()
                            if (!steak.collect(hero.belongings.backpack)) {
                                Dungeon.level.drop(steak, hero.pos).sprite.drop()
                            }
                            GLog.w(Messages.get(this, "burnsup", item!!.toString()))

                            Heap.burnFX(hero.pos)

                            burnedSomething = true
                        }
                    }
                }

            } else {
                target.takeDamage(Damage(this, target).setAdditionalDamage(Damage.Element.Fire, damage))
            }

            if (target is Thief) {

                val item = (target as Thief).item

                if (item is Scroll && item !is ScrollOfUpgrade) {
                    target.sprite.emitter().burst(ElmoParticle.FACTORY, 6)
                    (target as Thief).item = null
                }

            }

        } else {
            detach()
        }

        if (Level.flamable[target.pos]) {
            GameScene.add(Blob.seed(target.pos, 4, Fire::class.java))
        }

        spend(TICK)
        left -= TICK

        if (left <= 0 || Level.water[target.pos] && !target.flying) {

            detach()
        }

        return true
    }

    fun reignite(ch: Char) {
        left = duration(ch)
    }

    override fun icon(): Int {
        return BuffIndicator.FIRE
    }

    override fun fx(on: Boolean) {
        if (on)
            target.sprite.add(CharSprite.State.BURNING)
        else
            target.sprite.remove(CharSprite.State.BURNING)
    }

    override fun heroMessage(): String? {
        return Messages.get(this, "heromsg")
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns(left))
    }

    override fun onDeath() {

        Badges.validateDeathFromFire()

        Dungeon.fail(javaClass)
        GLog.n(Messages.get(this, "ondeath"))
    }

    companion object {

        private const val DURATION = 8f

        private const val LEFT = "left"
        private const val BURNED = "burned"

        fun duration(ch: Char): Float {
            val r = ch.buff(Resistance::class.java)
            return if (r != null) r.durationFactor() * DURATION else DURATION
        }
    }
}
