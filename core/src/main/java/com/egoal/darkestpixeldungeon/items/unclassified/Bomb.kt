/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.BlastParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.effects.particles.SmokeParticle
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList

open class Bomb : Item() {
    var fuse: Fuse? = null

    override val isUpgradable: Boolean
        get() = false

    override val isIdentified: Boolean
        get() = true

    init {
        image = ItemSpriteSheet.BOMB

        defaultAction = AC_LIGHTTHROW
        usesTargeting = true

        stackable = true
    }

    override fun isSimilar(item: Item): Boolean = item is Bomb && this.fuse === item.fuse

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_LIGHTTHROW)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        var action = action

        if (action == AC_LIGHTTHROW) {
            lightingFuse = true
            action = AC_THROW
        } else
            lightingFuse = false

        super.execute(hero, action)
    }

    override fun onThrow(cell: Int) {
        if (!Level.pit[cell] && lightingFuse) {
            fuse = Fuse().ignite(this)
            Actor.addDelayed(fuse!!, 2f)
        }
        if (Actor.findChar(cell) != null && Actor.findChar(cell) !is Hero) {
            val candidates = ArrayList<Int>()
            for (i in PathFinder.NEIGHBOURS8)
                if (Level.passable[cell + i])
                    candidates.add(cell + i)
            val newCell = if (candidates.isEmpty()) cell else Random.element(candidates)
            Dungeon.level.drop(this, newCell).sprite.drop(cell)
        } else
            super.onThrow(cell)
    }

    override fun doPickUp(hero: Hero): Boolean {
        if (fuse != null) {
            GLog.w(Messages.get(this, "snuff_fuse"))
            fuse = null
        }
        return super.doPickUp(hero)
    }

    fun explode(cell: Int) {
        //We're blowing up, so no need for a fuse anymore.
        this.fuse = null

        Sample.INSTANCE.play(Assets.SND_BLAST)

        if (Dungeon.visible[cell]) {
            CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30)
        }

        var terrainAffected = false
        for (n in PathFinder.NEIGHBOURS9) {
            val c = cell + n
            if (c >= 0 && c < Dungeon.level.length()) {
                if (Dungeon.visible[c]) {
                    CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4)
                }

                if (Level.flamable[c]) {
                    Dungeon.level.destroy(c)
                    GameScene.updateMap(c)
                    terrainAffected = true
                }

                //destroys items / triggers bombs caught in the blast.
                val heap = Dungeon.level.heaps.get(c)
                heap?.explode()

                val ch = Actor.findChar(c)
                if (ch != null) {
                    //those not at the center of the blast take damage less consistently.
                    val minDamage = if (c == cell) Dungeon.depth + 5 else 1
                    val maxDamage = 10 + Dungeon.depth * 2

                    val dmg = Damage(Random.NormalIntRange(minDamage, maxDamage),
                            this, ch).addElement(Damage.Element.FIRE)
                    ch.defendDamage(dmg)
                    ch.takeDamage(dmg)

                    if (ch === Dungeon.hero && !ch.isAlive)
                        Dungeon.fail(javaClass)
                }
            }
        }

        if (terrainAffected) {
            Dungeon.observe()
        }
    }

    override fun random(): Item {
        return when (Random.Int(2)) {
            0 -> this
            1 -> DoubleBomb()
            else -> this
        }
    }

    override fun glowing(): ItemSprite.Glowing? = if (fuse != null) ItemSprite.Glowing(0xFF0000, 0.6f) else null

    override fun price(): Int = 20 * quantity

    override fun desc(): String = if (fuse == null) super.desc() else M.L(this, "desc_burning")

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(FUSE, fuse)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        if (bundle.contains(FUSE)) {
            fuse = bundle.get(FUSE) as Fuse
            Actor.add(fuse!!.ignite(this))
        }
    }


    class Fuse : Actor() {
        private lateinit var bomb: Bomb

        init {
            actPriority = 3 //as if it were a buff
        }

        fun ignite(bomb: Bomb): Fuse {
            this.bomb = bomb
            return this
        }

        override fun act(): Boolean {
            //something caused our bomb to explode early, or be defused. Do nothing.
            if (bomb.fuse !== this) {
                Actor.remove(this)
                return true
            }

            //look for our bomb, remove it from its heap, and blow it up.
            for (heap in Dungeon.level.heaps.values()) {
                if (heap.items.contains(bomb)) {
                    heap.items.remove(bomb)

                    bomb.explode(heap.pos)

                    Actor.remove(this)
                    return true
                }
            }

            //can't find our bomb, something must have removed it, do nothing.
            bomb.fuse = null
            Actor.remove(this)
            return true
        }
    }


    class DoubleBomb : Bomb() {
        init {
            image = ItemSpriteSheet.DBL_BOMB
            stackable = false
        }

        override fun doPickUp(hero: Hero): Boolean {
            val bomb = Bomb()
            bomb.quantity(2)
            if (bomb.doPickUp(hero)) {
                // isaaaaac.... (don't bother doing this when not in english)
                if (Messages.get(this, "name") == "two bombs")
                    hero.sprite.showStatus(CharSprite.NEUTRAL, "1+1 free!")
                return true
            }
            return false
        }
    }

    companion object {
        //FIXME using a static variable for this is kinda gross, should be a better way
        private var lightingFuse = false

        private const val AC_LIGHTTHROW = "LIGHTTHROW"
        private const val FUSE = "fuse"
    }
}
