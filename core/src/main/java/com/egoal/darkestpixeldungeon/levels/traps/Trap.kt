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
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.tweeners.AlphaTweener
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.Random

abstract class Trap : Bundlable {
    var name = M.L(this, "name")

    var color: Int = 0
    var shape: Int = 0

    var pos: Int = 0

    lateinit var sprite: TrapSprite
    val hasSprite: Boolean
        get() = ::sprite.isInitialized

    var visible: Boolean = false
    var active = true

    fun set(pos: Int): Trap {
        this.pos = pos
        return this
    }

    fun reveal(): Trap {
        visible = true
        if (hasSprite && !sprite.visible) {
            sprite.visible = true
            sprite.alpha(0f)
            sprite.parent.add(AlphaTweener(sprite, 1f, 0.6f))
        }
        return this
    }

    open fun hide(): Trap {
        visible = false
        if (hasSprite)
            sprite.visible = false
        return this
    }

    open fun trigger() {
        if (active) {
            if (Dungeon.visible[pos]) {
                Sample.INSTANCE.play(Assets.SND_TRAP)
            }

            // when trigger a hidden trap, up pressure
            if (!visible) {
                val ch = Actor.findChar(pos)
                if (ch is Hero && ch.isAlive) {
                    ch.takeDamage(Damage(Random.NormalIntRange(1, 9), this, ch).type(Damage.Type.MENTAL))
                }
            }

            disarm()
            reveal()
            activate()
        }
    }

    abstract fun activate()

    protected open fun disarm() {
        Dungeon.level.disarmTrap(pos)
        active = false
        if (hasSprite) {
            sprite.reset(this)
        }
    }

    override fun restoreFromBundle(bundle: Bundle) {
        pos = bundle.getInt(POS)
        visible = bundle.getBoolean(VISIBLE)
        if (bundle.contains(ACTIVE)) {
            active = bundle.getBoolean(ACTIVE)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(POS, pos)
        bundle.put(VISIBLE, visible)
        bundle.put(ACTIVE, active)
    }

    fun desc(): String = M.L(this, "desc")

    companion object {
        private const val POS = "pos"
        private const val VISIBLE = "visible"
        private const val ACTIVE = "active"
    }
}
