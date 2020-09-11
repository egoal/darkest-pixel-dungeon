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
package com.egoal.darkestpixeldungeon.levels.traps

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback

class GrimTrap : Trap() {

    init {
        color = TrapSprite.GREY
        shape = TrapSprite.LARGE_DOT
    }

    override fun hide(): Trap {
        //cannot hide this trap
        return reveal()
    }

    override fun activate() {
        var target = Actor.findChar(pos)

        //find the closest char that can be aimed at
        if (target == null) {
            for (ch in Actor.chars()) {
                val bolt = Ballistica(pos, ch.pos, Ballistica.PROJECTILE)
                if (bolt.collisionPos == ch.pos && (target == null || Dungeon.level.distance(pos, ch.pos) < Dungeon.level.distance(pos, target.pos))) {
                    target = ch
                }
            }
        }

        if (target != null) {
            val finalTarget = target
            val trap = this
            MagicMissile.shadow(target.sprite.parent, pos, target.pos, Callback {
                if (!finalTarget.isAlive) return@Callback
                if (finalTarget === Dungeon.hero) {
                    //almost kill the player
                    if (finalTarget.HP.toFloat() / finalTarget.HT >= 0.9f) {
                        finalTarget.takeDamage(Damage(finalTarget.HP - 1,
                                trap, finalTarget).addFeature(Damage.Feature.PURE))
                        //kill 'em
                    } else {
                        finalTarget.takeDamage(Damage(finalTarget.HP,
                                trap, finalTarget).addFeature(Damage.Feature.PURE))
                    }
                    Sample.INSTANCE.play(Assets.SND_CURSED)
                    if (!finalTarget.isAlive) {
                        Dungeon.fail(GrimTrap::class.java)
                        GLog.n(Messages.get(GrimTrap::class.java, "ondeath"))
                    }
                } else {
                    finalTarget.takeDamage(Damage(finalTarget.HP,
                            trap, finalTarget).addFeature(Damage.Feature.PURE))
                    Sample.INSTANCE.play(Assets.SND_BURNING)
                }
                finalTarget.sprite.emitter().burst(ShadowParticle.UP, 10)
                if (!finalTarget.isAlive) finalTarget.next()
            })
        } else {
            CellEmitter.get(pos).burst(ShadowParticle.UP, 10)
            Sample.INSTANCE.play(Assets.SND_BURNING)
        }
    }
}
