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
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Charm
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption
import com.egoal.darkestpixeldungeon.actors.buffs.MagicalSleep
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.GooWarn
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.buffs.Ooze
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.GooSprite
import com.egoal.darkestpixeldungeon.ui.BossHealthBar
import com.egoal.darkestpixeldungeon.utils.BArray
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.HashSet

class Goo : Mob() {
    private var pumpedUp = 0

    init {
        spriteClass = GooSprite::class.java

        PropertyConfiger.set(this, "Goo")

        loot = LloydsBeacon().identify()
    }

    override fun giveDamage(enemy: Char): Damage {
        val dmg = Damage(0, this, enemy)

        val min = 1
        val max = if (HP * 2 <= HT) 15 else 10
        if (pumpedUp > 0) {
            // pumped attack
            pumpedUp = 0
            PathFinder.buildDistanceMap(pos, BArray.not(Level.solid, null), 2)
            for (i in PathFinder.distance.indices) {
                if (PathFinder.distance[i] < Integer.MAX_VALUE)
                    CellEmitter.get(i).burst(ElmoParticle.FACTORY, 10)
            }
            Sample.INSTANCE.play(Assets.SND_BURNING)
            dmg.value = Random.NormalIntRange(min * 3, max * 3)
            dmg.addFeature(Damage.Feature.CRITICAL)
        } else {
            dmg.value = Random.NormalIntRange(min, max)
        }

        return dmg
    }

    override fun accRoll(damage: Damage): Float {
        var acc = super.accRoll(damage)
        if (HP <= HT / 2) acc *= 1.5f
        if (pumpedUp > 0) acc *= 2f

        return acc
    }

    override fun dexRoll(damage: Damage): Float {
        return super.dexRoll(damage) * if (HP <= HT / 2) 1.5f else 1f
    }

    public override fun act(): Boolean {
        // healing in the water, and update health bar animation
        if (Level.water[pos] && HP < HT) {
            sprite.emitter().burst(Speck.factory(Speck.HEALING), 1)
            if (HP * 2 == HT) {
                BossHealthBar.bleed(false)
                (sprite as GooSprite).spray(false)
            }
            HP += 1
        }

        return super.act()
    }

    override fun canAttack(enemy: Char): Boolean {
        return if (pumpedUp > 0) distance(enemy) <= 2 else super.canAttack(enemy)
    }

    override fun attackProc(damage: Damage): Damage {
        val enemy = damage.to as Char
        if (!damage.isFeatured(Damage.Feature.CRITICAL) && Random.Int(3) == 0) {
            Buff.prolong(enemy, Vulnerable::class.java, 3f).ratio = 1.25f
            enemy.sprite.burst(0xFF0000, 5)
        }

        if (pumpedUp > 0) {
            Camera.main.shake(3f, 0.2f)
        }

        return damage
    }

    override fun defenseProc(dmg: Damage): Damage {
        if (pumpedUp == 0 && dmg.from is Char &&
                !dmg.isFeatured(Damage.Feature.RANGED) && Random.Int(4) == 0) {
            Buff.affect(dmg.from as Char, Ooze::class.java)
            (dmg.from as Char).sprite.burst(0x000000, 5)
        }

        return super.defenseProc(dmg)
    }

    override fun doAttack(enemy: Char): Boolean {
        if (pumpedUp == 1) {
            // pumped an extra turn
            (sprite as GooSprite).pumpUp()
            PathFinder.buildDistanceMap(pos, BArray.not(Level.solid, null), 2)
            for (i in PathFinder.distance.indices) {
                if (PathFinder.distance[i] < Integer.MAX_VALUE)
                    GameScene.add(Blob.seed(i, 2, GooWarn::class.java))
            }
            pumpedUp++

            spend(attackDelay())

            return true
        } else if (pumpedUp >= 2 || Random.Int(if (HP * 2 <= HT) 2 else 6) > 0) {
            // pumped or life below half
            val visible = Dungeon.visible[pos]

            if (visible) {
                if (pumpedUp >= 2) {
                    (sprite as GooSprite).pumpAttack()
                } else
                // normal attack
                    sprite.attack(enemy.pos)
            } else {
                attack(enemy)
            }

            spend(attackDelay())

            return !visible

        } else {
            // increase pump
            pumpedUp++

            (sprite as GooSprite).pumpUp()

            for (i in PathFinder.NEIGHBOURS9.indices) {
                val j = pos + PathFinder.NEIGHBOURS9[i]
                if (!Level.solid[j]) {
                    GameScene.add(Blob.seed(j, 2, GooWarn::class.java))
                }
            }

            if (Dungeon.visible[pos]) {
                sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "!!!"))
                GLog.n(Messages.get(this, "pumpup"))
            }

            spend(attackDelay())

            return true
        }
    }

    override fun attack(enemy: Char): Boolean {
        val result = super.attack(enemy)
        pumpedUp = 0
        return result
    }

    override fun getCloser(target: Int): Boolean {
        pumpedUp = 0
        return super.getCloser(target)
    }

    override fun move(step: Int) {
        Dungeon.level.seal()
        super.move(step)
    }

    override fun takeDamage(dmg: Damage): Int {
        val bleeding = HP * 2 <= HT

        val value = super.takeDamage(dmg)
        if (HP * 2 <= HT && !bleeding) {
            BossHealthBar.bleed(true)
            GLog.w(Messages.get(this, "enraged_text"))
            sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "enraged"))
            (sprite as GooSprite).spray(true)
            yell(Messages.get(this, "gluuurp"))
        }

        Dungeon.hero.buff(LockedFloor::class.java)?.addTime(dmg.value * 2f)

        return value
    }

    override fun die(cause: Any?) {

        super.die(cause)

        Dungeon.level.unseal()

        GameScene.bossSlain()
        Dungeon.level.drop(SkeletonKey(Dungeon.depth), pos).sprite.drop()

        Badges.validateBossSlain()

        yell(Messages.get(this, "defeated"))
    }

    override fun notice() {
        super.notice()
        BossHealthBar.assignBoss(this)
        yell(Messages.get(this, "notice"))
    }

    override fun storeInBundle(bundle: Bundle) {

        super.storeInBundle(bundle)

        bundle.put(PUMPEDUP, pumpedUp)
    }

    override fun restoreFromBundle(bundle: Bundle) {

        super.restoreFromBundle(bundle)

        pumpedUp = bundle.getInt(PUMPEDUP)
        if (state !== SLEEPING) BossHealthBar.assignBoss(this)
        if (HP * 2 <= HT) BossHealthBar.bleed(true)

    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    companion object {
        private const val PUMPEDUP = "pumpedup"

        private val IMMUNITIES = hashSetOf<Class<*>>(Terror::class.java, Corruption::class.java, Charm::class.java, MagicalSleep::class.java)
    }
}
