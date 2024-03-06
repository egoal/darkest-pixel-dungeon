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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Rage
import com.egoal.darkestpixeldungeon.effects.particles.SparkParticle
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ShamanSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Callback
import com.watabou.utils.Random

class Shaman : Mob(), Callback {

    init {
        spriteClass = ShamanSprite::class.java

        HUNTING = HuntingAI()
    }

    private var buffcd = 0f //todo: rework skilled ai

    override fun giveDamage(target: Char): Damage = super.giveDamage(target)
            .setAdditionalDamage(Damage.Element.Light, Random.NormalIntRange(1, 5))

    override fun canAttack(enemy: Char): Boolean = Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos

    override fun doAttack(enemy: Char): Boolean {

        if (Dungeon.level.distance(pos, enemy.pos) <= 1)
            return super.doAttack(enemy)
        else {

            val visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos]
            if (visible) {
                sprite.zap(enemy.pos)
            }

            spend(TIME_TO_ZAP)

            val dmg = giveDamage(enemy).addFeature(Damage.Feature.RANGED)

            ProcessAttackDamage(dmg, onHit = {
                if (Level.water[enemy.pos] && !enemy.flying) dmg.value = dmg.value * 3 / 2

                enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3)
                enemy.sprite.flash()

                if (enemy === Dungeon.hero) {
                    Camera.main.shake(2f, 0.3f)
                }
            }) {
                if (enemy === Dungeon.hero) {
                    Dungeon.fail(javaClass)
                    GLog.n(Messages.get(this, "zap_kill"))
                }
            }

            return !visible
        }
    }

    override fun call() {
        next()
    }

    override fun act(): Boolean {
        if (buffcd > 0f) buffcd -= 1f
        return super.act()
    }

    inner class HuntingAI : Hunting() {
        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            if (enemyInFOV) {
                val enemy = enemy!!
                if (buffcd <= 0f && !isCharmedBy(enemy!!) && (!canAttack(enemy) || distance(enemy) <= 1)) {
                    // ^ cannot attack or is face to face, find nearby friends to give Rage
                    // others are preferred
                    val nearbys = Dungeon.level.mobs.filter { mob ->
                        !(mob === this@Shaman) && mob.camp == camp && mob.buff(Rage::class.java) == null &&
                                Level.fieldOfView[mob.pos] && Dungeon.level.distance(pos, mob.pos) <= 4
                    }

                    if (nearbys.isNotEmpty()) {
                        buffRage(Random.element(nearbys))
                        return true
                    } else if (buff(Rage::class.java) == null) {
                        buffRage(this@Shaman)
                        return true
                    }
                }
            }

            return super.act(enemyInFOV, justAlerted)
        }

        private fun buffRage(mob: Mob) {
            if (mob !== this@Shaman && enemy != null) mob.beckon(enemy!!.pos)

            Buff.prolong(mob, Rage::class.java, 10f)
            buffcd = COOLDOWN_BUFF

            if (Dungeon.visible[pos]) Sample.INSTANCE.play(Assets.SND_MELD)
            GLog.n(Messages.get(Shaman::class.java, "buffed", mob.name))
            spend(TIME_TO_BUFF)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(COOLDOWN, buffcd)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        buffcd = bundle.getFloat(COOLDOWN)
    }

    companion object {
        private const val TIME_TO_ZAP = 1f
        private const val TIME_TO_BUFF = 0.5f

        private const val COOLDOWN_BUFF = 8f
        private const val COOLDOWN = "cooldown"
    }

}
