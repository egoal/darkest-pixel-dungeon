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
package com.egoal.darkestpixeldungeon.items.scrolls

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class ScrollOfPsionicBlast : Scroll() {
    init {
        initials = 5

        bones = true
    }

    override fun doRead() {
        GameScene.flash(0xFFFFFF)

        Sample.INSTANCE.play(Assets.SND_BLAST)
        Invisibility.dispel()

        Dungeon.level.mobs.filter { Level.fieldOfView[it.pos] }.forEach {
            it.takeDamage(Damage(it.HP, Item.curUser, it).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.DEATH))
        }

        curUser.takeDamage(Damage(Math.max(curUser.HT / 5, curUser.HP / 2), this, curUser).type(Damage.Type.MAGICAL))
        Buff.prolong(curUser, Paralysis::class.java, Random.Float(4f, 6f))
        Buff.prolong(curUser, Blindness::class.java, Random.Float(6f, 9f))
        Dungeon.observe()

        setKnown()

        curUser.spendAndNext(TIME_TO_READ) // no animation here, the flash interrupts it anyway.

        if (!curUser.isAlive) {
            Dungeon.fail(javaClass)
            GLog.n(M.L(this, "ondeath"))
        }
    }

    override fun price(): Int = if (isKnown) 50 * quantity else super.price()
}
