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

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Chill
import com.egoal.darkestpixeldungeon.actors.buffs.Frost
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class FrostTrap : Trap() {

    init {
        color = TrapSprite.WHITE
        shape = TrapSprite.STARS
    }

    override fun activate() {

        if (Dungeon.visible[pos]) {
            Splash.at(sprite.center(), -0x4d2901, 10)
            Sample.INSTANCE.play(Assets.SND_SHATTER)
        }

        val heap = Dungeon.level.heaps.get(pos)
        heap?.freeze()

        val ch = Actor.findChar(pos)
        if (ch != null) {
            ch.takeDamage(Damage(this, ch, Damage.Type.MAGICAL)
                    .setAdditionalDamage(Damage.Element.Ice, Random.NormalIntRange(1, Dungeon.depth)))
            Buff.prolong(ch, Frost::class.java, 10f + Random.Int(Dungeon.depth))
            if (!ch.isAlive && ch === Dungeon.hero) {
                Dungeon.fail(javaClass)
                GLog.n(Messages.get(this, "ondeath"))
            }
        }
    }
}
