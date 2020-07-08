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

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.DM300Sprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.items.artifacts.LloydsBeacon
import com.egoal.darkestpixeldungeon.items.books.TomeOfRetrain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.BossHealthBar
import com.egoal.darkestpixeldungeon.ui.Icons
import com.watabou.noosa.Camera
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.HashSet

class DM300 : Mob() {
    private var overloaded = false
    private val cross: Image = Icons.TARGET.get()
    //todo: overhaul this, but i may not.
    private var onBumped: (() -> Unit)? = null

    private var bumpcd = 0
    private var bumpcell = -1

    init {
        spriteClass = DM300Sprite::class.java

        PropertyConfiger.set(this, "DM300")
        loot = TomeOfRetrain()
    }

    override fun viewDistance(): Int = 6

    override fun giveDamage(target: Char): Damage {
        val dmg = super.giveDamage(target)
        if (overloaded) dmg.value += dmg.value / 4

        return dmg
    }

    public override fun act(): Boolean {
        GameScene.add(Blob.seed(pos, 30, ToxicGas::class.java))

        if (bumpcd > 0) --bumpcd

        Dungeon.level.updateFieldOfView(this, Level.fieldOfView) //todo: optimize this.

        if (bumpcell < 0 && bumpcd <= 0)
            if (state == HUNTING && paralysed <= 0 && enemy != null && enemy.invisible == 0 &&
                    Level.fieldOfView[enemy.pos] && Dungeon.level.distance(pos, enemy.pos) <= 8 &&
                    !Dungeon.level.adjacent(pos, enemy.pos) && canBump(enemy.pos)) {
                setBumpCell(enemy.pos)
                if (Dungeon.visible[pos]) say(M.L(this, "bump"))
                else GLog.n("\"${M.L(this, "bump")}\"") // todo
                spend(Actor.TICK)
                return true
            }
        if (bumpcell >= 0 && paralysed <= 0) {
            val tgt = bumpcell
            resetBumpCell()
            if (bump(tgt))
                return false
        }

        return super.act()
    }

    private fun setBumpCell(pos: Int) {
        bumpcell = pos
        if (cross.parent == null) {
            sprite.parent.add(cross)
            cross.color(1f, 0.1f, 0.02f)
        }
        cross.visible = true
        val cen = DungeonTilemap.tileCenterToWorld(bumpcell)
        cross.point(cen.offset(-cross.width / 2f, -cross.height / 2f))
    }

    private fun resetBumpCell() {
        bumpcell = -1
        if(cross!=null) cross.visible = false
    }

    public override fun attackDelay(): Float = if (overloaded) .667f else 1f

    override fun description(): String {
        var desc = Messages.get(this, "desc")
        if (overloaded)
            desc += "\n\n" + Messages.get(this, "overloaded_desc")

        return desc
    }

    override fun move(step: Int) {
        super.move(step)

        if (Dungeon.level.map[step] == Terrain.INACTIVE_TRAP && HP < HT) {
            HP += Random.Int(1, HT - HP)
            sprite.emitter().burst(ElmoParticle.FACTORY, 5)

            if (Dungeon.visible[step] && Dungeon.hero.isAlive)
                GLog.n(Messages.get(this, "repair"))
        }

        val cells = intArrayOf(step - 1, step + 1, step - Dungeon.level.width(), step + Dungeon
                .level.width(), step - 1 - Dungeon.level.width(), step - 1 + Dungeon.level.width(), step + 1 - Dungeon.level.width(), step + 1 + Dungeon.level.width())
        val cell = cells[Random.Int(cells.size)]

        if (Dungeon.visible[cell]) {
            CellEmitter.get(cell).start(Speck.factory(Speck.ROCK), 0.07f, 10)
            Camera.main.shake(3f, 0.7f)
            Sample.INSTANCE.play(Assets.SND_ROCKS)

            if (Level.water[cell]) {
                GameScene.ripple(cell)
            } else if (Dungeon.level.map[cell] == Terrain.EMPTY) {
                Level[cell] = Terrain.EMPTY_DECO
                GameScene.updateMap(cell)
            }
        }

        val ch = Actor.findChar(cell)
        if (ch != null && ch !== this) {
            Buff.prolong(ch, Paralysis::class.java, 2f)
        }
    }

    override fun takeDamage(dmg: Damage): Int {
        val taken = super.takeDamage(dmg)

        val lock = Dungeon.hero.buff(LockedFloor::class.java)
        if (lock != null && !immunizedBuffs().contains(dmg.from.javaClass))
            lock.addTime(dmg.value * 1.5f)

        if (HP < HT * .3 && !overloaded) overload()

        return taken
    }

    override fun attackProc(dmg: Damage): Damage {
        // chance to knock back
        if (dmg.to is Char && Random.Float() < .3f) {
            val tgt = dmg.to as Char
            val opposite = tgt.pos + (tgt.pos - pos)
            val shot = Ballistica(tgt.pos, opposite, Ballistica
                    .MAGIC_BOLT)

            WandOfBlastWave.throwChar(tgt, shot, 1)
        }

        return super.attackProc(dmg)
    }

    override fun die(cause: Any) {
        super.die(cause)

        GameScene.bossSlain()

        resetBumpCell();
        Dungeon.level.drop(SkeletonKey(Dungeon.depth), pos).sprite.drop()
        Dungeon.level.drop(TomeOfRetrain(), pos).sprite.drop()

        Badges.validateBossSlain()

        val beacon = Dungeon.hero.belongings.getItem(LloydsBeacon::class.java)
        beacon?.upgrade()

        yell(Messages.get(this, "defeated"))
    }

    override fun notice() {
        super.notice()
        BossHealthBar.assignBoss(this)
        yell(Messages.get(this, "notice"))
    }

    private fun canBump(cell: Int): Boolean {
        val bumpPath = Ballistica(pos, cell, Ballistica.STOP_TARGET or Ballistica.STOP_TERRAIN)
        return bumpPath.path.size >= 3 && bumpPath.dist >= 2
    }

    private fun bump(cell: Int): Boolean {
        bumpcd = 3
        val bumpPath = Ballistica(pos, cell, Ballistica.STOP_TARGET or Ballistica.STOP_TERRAIN)

        if (bumpPath.path.size < 3 || bumpPath.dist < 2) return false

        var dst = bumpPath.collisionPos
        if (Actor.findChar(cell) != null) dst = bumpPath.path[bumpPath.dist - 1] //todo: may try knock back it

        onBumped = {
            super.move(dst) // super: no moving paralysis
            CellEmitter.get(pos).start(Speck.factory(Speck.ROCK), 0.07f, 20)
            Camera.main.shake(5f, 1.2f)
            Sample.INSTANCE.play(Assets.SND_ROCKS)

            for (i in bumpPath.path) if (Level.water[i]) GameScene.ripple(cell)

            // directly affected
            val bumpedChars = bumpPath.path.mapNotNull {
                val ch = Actor.findChar(it)
                if (ch === this) null
                else ch
            }.toHashSet()

            for (ch in bumpedChars) {
                val dmg = giveDamage(ch)
                ch.defendDamage(dmg)
                ch.takeDamage(dmg)
                Buff.prolong(ch, Paralysis::class.java, 2f)
            }
            // splash
            for (i in PathFinder.NEIGHBOURS8) {
                val ch = Actor.findChar(pos + i)
                if (ch != null && !bumpedChars.contains(ch)) {
                    // damage only
                    val dmg = giveDamage(ch)
                    dmg.value /= 2
                    ch.defendDamage(dmg)
                    ch.takeDamage(dmg)
                }
            }

            spend(2f)
            next()
        }
        sprite.move(pos, dst)

        return true
    }

    override fun onMotionComplete() {
        if (onBumped != null) {
            onBumped!!.invoke()
            onBumped = null
        }
    }

    override fun defendDamage(dmg: Damage): Damage {
        if (!overloaded && dmg.type == Damage.Type.NORMAL) {
            dmg.value -= dmg.value / 5
            if (dmg.isFeatured(Damage.Feature.RANGED)) dmg.value -= dmg.value / 5
        }

        return super.defendDamage(dmg)
    }

    private fun overload() {
        overloaded = true

        // remove ice resistance, immune fire damage
        addResistances(Damage.Element.ICE, -0.5f)
        addResistances(Damage.Element.FIRE, 0.5f)
        // addResistances(Damage.Element.FIRE, 100f, 1f);

        sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "overload"))
        sprite.emitter().burst(Speck.factory(Speck.WOOL), 5)

        GLog.w(Messages.get(this, "overload_warning"))
        spend(1f)
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(OVERLOADED, overloaded)
        bundle.put(BUMP_CELL, bumpcell)
        bundle.put(BUMP_CD, bumpcd)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        overloaded = bundle.getBoolean(OVERLOADED)
        bumpcell = bundle.getInt(BUMP_CELL)
        bumpcd = bundle.getInt(BUMP_CD)

        BossHealthBar.assignBoss(this)
    }

    companion object {
        private val IMMUNITIES = hashSetOf<Class<*>>(ToxicGas::class.java, Terror::class.java,
                Corruption::class.java, Charm::class.java, MagicalSleep::class.java, Cripple::class.java)

        private const val OVERLOADED = "overloaded"
        private const val BUMP_CELL = "bumpcell"
        private const val BUMP_CD = "bumpcd"
    }
}
