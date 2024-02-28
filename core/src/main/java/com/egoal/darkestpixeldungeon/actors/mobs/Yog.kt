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

import com.egoal.darkestpixeldungeon.Database
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.effects.Beam
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.*
import com.egoal.darkestpixeldungeon.ui.BossHealthBar
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Yog : Mob() {
    private var zapCD_ = 3;

    init {
        spriteClass = YogSprite::class.java

        state = Watching() // PASSIVE
    }

    fun spawnFists() {
        val fist1 = RottingFist()
        val fist2 = BurningFist()

        do {
            fist1.pos = pos + PathFinder.NEIGHBOURS8[Random.Int(8)]
            fist2.pos = pos + PathFinder.NEIGHBOURS8[Random.Int(8)]
        } while (!Level.passable[fist1.pos] || !Level.passable[fist2.pos] || fist1.pos == fist2.pos)

        GameScene.add(fist1)
        GameScene.add(fist2)

        notice()
    }

    override fun act(): Boolean {
        //heals 1 health per turn
        HP = min(HT, HP + 1)

        return super.act()
    }

    override fun takeDamage(dmg: Damage): Int {
        val fists = HashSet<Mob>()

        for (mob in Dungeon.level.mobs)
            if (mob is RottingFist || mob is BurningFist)
                fists.add(mob)

        for (fist in fists)
            fist.beckon(pos)

        dmg.value = dmg.value shr fists.size

        val value = super.takeDamage(dmg)


        val lock = Dungeon.hero.buff(LockedFloor::class.java)
        lock?.addTime(dmg.value * 0.5f)

        return value
    }

    override fun defenseProc(damage: Damage): Damage {
        val enemy = damage.from as Char

        val spawnPoints = ArrayList<Int>()

        for (i in PathFinder.NEIGHBOURS8.indices) {
            val p = pos + PathFinder.NEIGHBOURS8[i]
            if (Actor.findChar(p) == null && (Level.passable[p] || Level.avoid[p])) {
                spawnPoints.add(p)
            }
        }

        if (spawnPoints.size > 0) {
            val larva = Larva()
            larva.pos = Random.element(spawnPoints)

            GameScene.add(larva)
            Actor.addDelayed(Pushing(larva, pos, larva.pos), -1f)
        }

        for (mob in Dungeon.level.mobs) {
            if (mob is BurningFist || mob is RottingFist || mob is Larva) {
                mob.aggro(enemy)
            }
        }

        return super.defenseProc(damage)
    }

    override fun beckon(cell: Int) {}

    override fun die(cause: Any?) {
        // remove view mark
        Buff.detach(Dungeon.hero, ViewMark::class.java)

        Dungeon.level.mobs.filter { it is BurningFist || it is RottingFist }.forEach { it.die(cause) }

        GameScene.bossSlain()
        Dungeon.level.drop(SkeletonKey(Dungeon.depth), pos).sprite.drop()
        super.die(cause)

        yell(Messages.get(this, "defeated"))
    }

    override fun notice() {
        super.notice()
        BossHealthBar.assignBoss(this)
        yell(Messages.get(this, "notice"))
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("zapcd", zapCD_)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        BossHealthBar.assignBoss(this)
        zapCD_ = bundle.getInt("zapcd")
    }

    private inner class Watching : AiState {
        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            enemy = Dungeon.hero
            if (enemy!!.isAlive && zapCD_ <= 0) {
                val value = Random.NormalIntRange(5, 15 + (10 * (1f - HP / HT.toFloat())).toInt())
                val dmg = Damage(value, this@Yog, enemy!!).type(Damage.Type.MENTAL)
                enemy!!.takeDamage(dmg)
                GLog.n(M.L(Yog::class.java, "no_hiding"))
                zapCD_ = 5

                sprite.parent.add(Beam.DarkRay(sprite.center(), DungeonTilemap.tileCenterToWorld(enemy!!.pos * 2 - pos)))
            } else zapCD_ = max(zapCD_ - 1, 0)

            spend(1 / speed())
            return true
        }

        override fun status(): String = M.L(Hunting::class.java, "status", this)
    }

    class RottingFist : Mob() {
        init {
            Config = Database.ConfigOfMob("Yog_RottingFist")!!

            spriteClass = RottingFistSprite::class.java

            state = WANDERING

            properties.add(Char.Property.BOSS)
            properties.add(Char.Property.DEMONIC)

            addResistances(Damage.Element.POISON, 0.2f)
            addResistances(Damage.Element.HOLY, -0.25f)
        }

        override fun attackProc(damage: Damage): Damage {
            val enemy = damage.to as Char
            if (Random.Int(3) == 0) {
                Buff.affect(enemy, Ooze::class.java)
                enemy.sprite.burst(-0x1000000, 5)
            }

            return damage
        }

        public override fun act(): Boolean {

            if (Level.water[pos] && HP < HT) {
                sprite.emitter().burst(ShadowParticle.UP, 2)
                HP += REGENERATION
            }

            // eyed, share vision with yog
            // code related to Mob::act, but no need to update field of view,
            val justAlerted = alerted
            alerted = false

            sprite.hideAlert()

            if (paralysed > 0) {
                enemySeen = false
                spend(Actor.TICK)
                return true
            }

            enemy = chooseEnemy()
            val enemyInFOV = enemy != null && enemy!!.isAlive &&
                    enemy!!.invisible <= 0

            return state.act(enemyInFOV, justAlerted)
        }

        override fun takeDamage(dmg: Damage): Int {
            Dungeon.hero.buff(LockedFloor::class.java)?.addTime(dmg.value * 0.5f)

            return super.takeDamage(dmg)
        }

        override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

        companion object {
            private const val REGENERATION = 4

            private val IMMUNITIES = hashSetOf<Class<*>>(Amok::class.java, Sleep::class.java, Burning::class.java,
                    Terror::class.java, Poison::class.java, Vertigo::class.java, Corruption::class.java, MagicalSleep::class.java)
        }
    }

    class BurningFist : Mob() {

        init {
            Config = Database.ConfigOfMob("Yog_BurningFist")!!

            spriteClass = BurningFistSprite::class.java
            state = WANDERING
        }

        override fun canAttack(enemy: Char): Boolean {
            return Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT)
                    .collisionPos == enemy.pos
        }

        override fun attack(enemy: Char): Boolean {

            if (!Dungeon.level.adjacent(pos, enemy.pos)) {
                spend(attackDelay())

                val dmg = giveDamage(enemy).convertToElement(Damage.Element.FIRE)
                if (enemy.checkHit(dmg)) {

                    enemy.takeDamage(dmg)

                    enemy.sprite.bloodBurstA(sprite.center(), dmg.value)
                    enemy.sprite.flash()

                    if (!enemy.isAlive && enemy === Dungeon.hero) {
                        Dungeon.fail(javaClass)
                        GLog.n(Messages.get(Char::class.java, "kill", name))
                    }
                    return true

                } else {

                    enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb())
                    return false
                }
            } else {
                return super.attack(enemy)
            }
        }

        public override fun act(): Boolean {

            for (i in PathFinder.NEIGHBOURS9.indices) {
                GameScene.add(Blob.seed(pos + PathFinder.NEIGHBOURS9[i], 2, Fire::class.java))
            }

            // code related to RottingFist::act
            val justAlerted = alerted
            alerted = false

            sprite.hideAlert()

            if (paralysed > 0) {
                enemySeen = false
                spend(Actor.TICK)
                return true
            }

            enemy = chooseEnemy()
            val enemyInFOV = enemy != null && enemy!!.isAlive &&
                    enemy!!.invisible <= 0

            return state.act(enemyInFOV, justAlerted)
        }

        override fun takeDamage(dmg: Damage): Int {
            Dungeon.hero.buff(LockedFloor::class.java)?.addTime(dmg.value * 0.5f)

            return super.takeDamage(dmg)
        }

        override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

        companion object {
            private val IMMUNITIES = hashSetOf<Class<*>>(Amok::class.java, Sleep::class.java,
                    Terror::class.java, Burning::class.java, Vertigo::class.java, Corruption::class.java, MagicalSleep::class.java)
        }
    }

    class Larva : Mob() {
        init {
            Config = Database.ConfigOfMob("Yog_Larva")!!

            spriteClass = LarvaSprite::class.java
            state = HUNTING
        }
    }

    companion object {
        private val IMMUNITIES = hashSetOf<Class<*>>(Amok::class.java, Sleep::class.java, Terror::class.java,
                Poison::class.java, Vertigo::class.java, Corruption::class.java,
                MagicalSleep::class.java, Paralysis::class.java)
    }
}
