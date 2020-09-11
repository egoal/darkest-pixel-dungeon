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
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.security.cert.TrustAnchor

class RockfallTrap : Trap() {

    init {
        color = TrapSprite.GREY
        shape = TrapSprite.DIAMOND
    }

    override fun activate() {
        fallRocks(pos)
    }

    companion object {

        fun fallRocks(pos: Int) {
            var seen = false

            for (i in PathFinder.NEIGHBOURS9) {

                if (Level.solid[pos + i])
                    continue

                if (Dungeon.visible[pos + i]) {
                    CellEmitter.get(pos + i - Dungeon.level.width()).start(Speck.factory(Speck.ROCK), 0.07f, 10)
                    if (!seen) {
                        Camera.main.shake(3f, 0.7f)
                        Sample.INSTANCE.play(Assets.SND_ROCKS)
                        seen = true
                    }
                }

                val ch = Actor.findChar(pos + i)

                if (ch != null) {
                    val damage = Random.NormalIntRange(Dungeon.depth, Dungeon.depth * 2)
                    ch.takeDamage(ch.defendDamage(Damage(damage, RockfallTrap(),
                            ch)))

                    Buff.prolong(ch, Paralysis::class.java, Paralysis.duration(ch) / 2)

                    if (!ch.isAlive && ch === Dungeon.hero) {
                        Dungeon.fail(RockfallTrap::class.java)
                        GLog.n(Messages.get(RockfallTrap::class.java, "ondeath"))
                    }
                }
            }

            for (mob in Dungeon.level.mobs.toTypedArray()) {
                if (Dungeon.level.distance(mob.pos, pos) < 12)
                    mob.beckon(pos)
            }
            GLog.n(Messages.get(RockfallTrap::class.java, "roar"))
        }
    }
}
