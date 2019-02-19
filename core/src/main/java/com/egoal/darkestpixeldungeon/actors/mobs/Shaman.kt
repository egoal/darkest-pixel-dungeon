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
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Rage
import com.egoal.darkestpixeldungeon.effects.particles.SparkParticle
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ShamanSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.Random

class Shaman : Mob(), Callback {

    init {
        spriteClass = ShamanSprite::class.java

        HT = 18
        HP = HT
        defenseSkill = 8

        HUNTING = HuntingAI()

        EXP = 6
        maxLvl = 14

        loot = Generator.Category.SCROLL
        lootChance = 0.25f

        addResistances(Damage.Element.LIGHT, 1.25f)
    }

    override fun giveDamage(target: Char): Damage = Damage(Random.NormalIntRange(2, 8), this, target).addElement(Damage.Element.LIGHT)

    override fun defendDamage(dmg: Damage): Damage = dmg.apply { value -= Random.NormalIntRange(0, 4); }

    override fun attackSkill(target: Char): Int = 11

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

            val dmg = Damage(Random.NormalIntRange(3, 10),
                    this, enemy).type(Damage.Type.MAGICAL).addElement(Damage
                    .Element.LIGHT)

            if (enemy.checkHit(dmg)) {
                if (Level.water[enemy.pos] && !enemy.flying)
                    dmg.value = (dmg.value.toFloat() * 1.5f).toInt()

                enemy.defendDamage(dmg)
                enemy.takeDamage(dmg)

                enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3)
                enemy.sprite.flash()

                if (enemy === Dungeon.hero) {

                    Camera.main.shake(2f, 0.3f)

                    if (!enemy.isAlive()) {
                        Dungeon.fail(javaClass)
                        GLog.n(Messages.get(this, "zap_kill"))
                    }
                }
            } else {
                enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb())
            }

            return !visible
        }
    }

    override fun call() {
        next()
    }

    inner class HuntingAI : Hunting() {
        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            if (enemyInFOV && !isCharmedBy(enemy) && (!canAttack(enemy) || distance(enemy) <= 1)) {
                // ^ cannot attack or is face to face, find nearby friends to give Rage
                var buffed: Mob? = null

                val pt = Dungeon.level.cellToPoint(pos)
                for (x in -4..4) {
                    for (y in -4..4) {
                        val cell = Dungeon.level.xy2cell(pt.x + x, pt.y + y)
                        if (Level.fieldOfView[cell])
                        //^ in vision
                            Dungeon.level.findMob(cell)?.let { mob ->
                                if (mob.hostile && mob.buff(Rage::class.java) == null) {
                                    Buff.prolong(mob, Rage::class.java, 10f)
                                    buffed = mob
                                    if (Dungeon.visible[pos]) 
                                        Sample.INSTANCE.play(Assets.SND_MELD)
                                }
                            }
                        
                        if (buffed != null) break
                    }
                    if (buffed != null) break
                }

                if (buffed != null) {
                    GLog.n(Messages.get(Shaman::class.java, "buffed", buffed!!.name))
                    spend(TIME_TO_BUFF)
                    return true
                }
            }

            return super.act(enemyInFOV, justAlerted)
        }
    }

    companion object {
        private const val TIME_TO_ZAP = 1f
        private const val TIME_TO_BUFF = 1f
    }

}
