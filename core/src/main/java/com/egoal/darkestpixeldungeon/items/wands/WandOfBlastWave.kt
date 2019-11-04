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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Effects
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.watabou.noosa.Game
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.PathFinder
import com.watabou.utils.PointF
import com.watabou.utils.Random
import kotlin.math.round

class WandOfBlastWave : DamageWand() {
    init {
        image = ItemSpriteSheet.WAND_BLAST_WAVE

        collisionProperties = Ballistica.PROJECTILE
    }

    override fun min(lvl: Int): Int = 1 + lvl

    override fun max(lvl: Int): Int = 5 + 3 * lvl

    override fun onZap(bolt: Ballistica) {
        Sample.INSTANCE.play(Assets.SND_BLAST)
        BlastWave.blast(bolt.collisionPos)

        //presses all tiles in the AOE first
        for (i in PathFinder.NEIGHBOURS9) {
            Dungeon.level.press(bolt.collisionPos + i, Actor.findChar(bolt.collisionPos + i))
        }

        //throws other chars around the center.
        for (i in PathFinder.NEIGHBOURS8) {
            Actor.findChar(bolt.collisionPos + i)?.let {
                it.takeDamage(giveDamage(it).apply { value = round(value * .0667).toInt() })
                if (it.isAlive) {
                    val traj = Ballistica(it.pos, it.pos + i, Ballistica.MAGIC_BOLT)
                    val str = 1 + round(level() / 2f).toInt()
                    throwChar(it, traj, str)
                }
            }
        }

        //throws the char at the center of the blast
        Actor.findChar(bolt.collisionPos)?.let {
            onMissileHit(it, Dungeon.hero)

            it.takeDamage(giveDamage(it))
            if (it.isAlive && bolt.path.size > bolt.dist + 1) {
                val traj = Ballistica(it.pos, bolt.path[bolt.dist + 1], Ballistica.MAGIC_BOLT)
                val str = level() + 3
                throwChar(it, traj, str)
            }
        }

        if (!curUser.isAlive) {
            Dungeon.fail(javaClass)
            GLog.n(Messages.get(this, "ondeath"))
        }
    }

    //behaves just like glyph of Repulsion
    override fun onHit(staff: MagesStaff, damage: Damage) {
        val level = Math.max(0, staff.level())

        // lvl 0 - 25%
        // lvl 1 - 40%
        // lvl 2 - 50%
        val attacker = damage.from as Char
        val defender = damage.to as Char

        if (Random.Int(level + 4) >= 3) {
            val oppositeHero = defender.pos + (defender.pos - attacker.pos)
            val trajectory = Ballistica(defender.pos, oppositeHero,
                    Ballistica.MAGIC_BOLT)
            throwChar(defender, trajectory, 2)
        }
    }

    override fun fx(bolt: Ballistica, callback: Callback) {
        MagicMissile.slowness(curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0x664422)
        particle.am = 0.6f
        particle.setLifespan(2f)
        particle.speed.polar(Random.Float(PointF.PI2), 0.3f)
        particle.setSize(1f, 2f)
        particle.radiateXY(3f)
    }

    class BlastWave : Image(Effects.get(Effects.Type.RIPPLE)) {
        private var time: Float = 0.toFloat()

        init {
            origin.set(width / 2, height / 2)
        }

        fun reset(pos: Int) {
            revive()

            x = pos % Dungeon.level.width() * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - width) / 2
            y = pos / Dungeon.level.width() * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - height) / 2

            time = TIME_TO_FADE
        }

        override fun update() {
            super.update()

            time -= Game.elapsed
            if (time <= 0) {
                kill()
            } else {
                val p = time / TIME_TO_FADE
                alpha(p)
                scale.x = (1 - p) * 3
                scale.y = scale.x
            }
        }

        companion object {

            private const val TIME_TO_FADE = 0.2f

            fun blast(pos: Int) {
                val parent = Dungeon.hero.sprite.parent
                val b = parent.recycle(BlastWave::class.java) as BlastWave
                parent.bringToFront(b)
                b.reset(pos)
            }
        }

    }

    companion object {
        fun throwChar(ch: Char, trajectory: Ballistica,
                      power: Int) {
            var dist = Math.min(trajectory.dist, power)

            if (ch.properties().contains(Char.Property.BOSS))
                dist /= 2

            if (dist == 0 || ch.properties().contains(Char.Property.IMMOVABLE)) return

            if (Actor.findChar(trajectory.path[dist]) != null) {
                dist--
            }

            val newPos = trajectory.path[dist]

            if (newPos == ch.pos) return

            val finalDist = dist
            val initialpos = ch.pos

            Actor.addDelayed(Pushing(ch, ch.pos, newPos, Callback {
                if (initialpos != ch.pos) {
                    //something cased movement before pushing resolved, cancel to be safe.
                    ch.sprite.place(ch.pos)
                    return@Callback
                }
                ch.pos = newPos
                if (ch.pos == trajectory.collisionPos) {
                    ch.takeDamage(Damage(Random.NormalIntRange((finalDist + 1) / 2,
                            finalDist), Char.Nobody.INSTANCE, ch).type(Damage.Type.MAGICAL))

                    Paralysis.prolong(ch, Paralysis::class.java, (Random.NormalIntRange(
                            (finalDist + 1) / 2, finalDist) + 1).toFloat())
                }
                Dungeon.level.press(ch.pos, ch)
                // when hero is moved, update vision.
                if (ch is Hero) Dungeon.observe()
            }), -1f)
        }
    }
}
