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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.TempPathLight
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.PurpleParticle
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.EyeSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Eye : Mob() {
    private var beam: Ballistica? = null
    private var beamTarget = -1
    private var beamCooldown: Int = 0
    var beamCharged: Boolean = false

    init {
        spriteClass = EyeSprite::class.java
        flying = true
        HUNTING = Hunting()

        immunities.add(Terror::class.java)
    }

    override fun viewDistance(): Int = 6

    override fun giveDamage(target: Char): Damage = super.giveDamage(target).convertToElement(Damage.Element.Shadow)

    override fun canAttack(enemy: Char): Boolean {
        if (beamCooldown == 0) {
            val aim = Ballistica(pos, enemy.pos, Ballistica.STOP_TERRAIN)

            if (enemy.invisible == 0 && Level.fieldOfView[enemy.pos] && aim.subPath(1, aim.dist).contains(enemy.pos)) {
                beam = aim
                beamTarget = aim.collisionPos
                return true
            } else
            //if the beam is charged, it has to attack, will aim at previous
            // location of hero.
                return beamCharged
        } else
            return super.canAttack(enemy)
    }

    override fun act(): Boolean {
        if (beam == null && beamTarget != -1) {
            beam = Ballistica(pos, beamTarget, Ballistica.STOP_TERRAIN)
            sprite.turnTo(pos, beamTarget)
        }
        if (beamCooldown > 0)
            beamCooldown--
        return super.act()
    }

    override fun chooseEnemy(): Char? {
        return if (beamCharged && enemy != null) enemy else super.chooseEnemy()
    }

    override fun doAttack(enemy: Char): Boolean {

        if (beamCooldown > 0) {
            return super.doAttack(enemy)
        } else if (!beamCharged) {
            (sprite as EyeSprite).charge(enemy.pos)
            spend(attackDelay() * 2f)
            beamCharged = true
            return true
        } else {

            spend(attackDelay())

            if (Dungeon.visible[pos]) {
                sprite.zap(beam!!.collisionPos)
                return false
            } else {
                deathGaze()
                return true
            }
        }

    }

    override fun takeDamage(dmg: Damage): Int {
        if (beamCharged) dmg.value /= 4

        return super.takeDamage(dmg)
    }

    fun deathGaze() {
        if (!beamCharged || beamCooldown > 0 || beam == null)
            return

        beamCharged = false
        beamCooldown = Random.IntRange(3, 6)

        for (pos in beam!!.subPath(1, beam!!.dist)) {

            if (Level.flamable[pos]) {

                Dungeon.level.destroy(pos)
                GameScene.updateMap(pos)
            }

            val ch = Actor.findChar(pos) ?: continue

            val dmg = Damage(Random.NormalIntRange(30, 50),
                    this, ch).type(Damage.Type.MAGICAL)
            if (ch.checkHit(dmg)) {
                ch.takeDamage(dmg)

                if (Dungeon.visible[pos]) {
                    ch.sprite.flash()
                    CellEmitter.center(pos).burst(PurpleParticle.BURST, Random.IntRange(1, 2))
                }

                if (!ch.isAlive && ch === Dungeon.hero) {
                    Dungeon.fail(javaClass)
                    GLog.n(Messages.get(this, "deathgaze_kill"))
                }
            } else {
                ch.sprite.showStatus(CharSprite.NEUTRAL, ch.defenseVerb())
            }
        }

        TempPathLight.Light(beam!!.path, 3f)

        beam = null
        beamTarget = -1
        sprite.idle()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(BEAM_TARGET, beamTarget)
        bundle.put(BEAM_COOLDOWN, beamCooldown)
        bundle.put(BEAM_CHARGED, beamCharged)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        if (bundle.contains(BEAM_TARGET))
            beamTarget = bundle.getInt(BEAM_TARGET)
        beamCooldown = bundle.getInt(BEAM_COOLDOWN)
        beamCharged = bundle.getBoolean(BEAM_CHARGED)
    }

    private inner class Hunting : Mob.Hunting() {
        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            //always attack if the beam is charged, no exceptions
            return super.act(enemyInFOV || (beamCharged && enemy != null), justAlerted)
        }
    }

    companion object {
        private const val BEAM_TARGET = "beamTarget"
        private const val BEAM_COOLDOWN = "beamCooldown"
        private const val BEAM_CHARGED = "beamCharged"
    }
}
