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
package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class IncendiaryDart(number: Int = 1) : MissileWeapon(2, stick = true) {

    init {
        image = ItemSpriteSheet.INCENDIARY_DART
        
        quantity = number
    }

    override fun min(lvl: Int): Int = 1

    override fun max(lvl: Int): Int = 2

    override fun onThrow(cell: Int) {
        val enemy = Actor.findChar(cell)
        if ((enemy == null || enemy === Item.curUser) && Level.flamable[cell])
            GameScene.add(Blob.seed(cell, 4, Fire::class.java))
        else
            super.onThrow(cell)
    }

    override fun proc(damage: Damage): Damage {
        Buff.affect(damage.to as Char, Burning::class.java).reignite(damage.to as Char)
        return super.proc(damage)
    }

    override fun random(): Item {
        quantity = Random.Int(3, 6)
        return this
    }

}
