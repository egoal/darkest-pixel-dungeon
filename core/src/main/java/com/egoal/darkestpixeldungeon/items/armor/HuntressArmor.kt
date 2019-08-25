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
package com.egoal.darkestpixeldungeon.items.armor

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Shuriken
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.MissileSprite
import com.watabou.utils.Callback

import java.util.HashMap

class HuntressArmor : ClassArmor() {
    private val targets = HashMap<Callback, Mob>()

    init {
        image = ItemSpriteSheet.ARMOR_HUNTRESS
    }

    override fun doSpecial() {
        val proto = Shuriken()

        for (mob in Dungeon.level.mobs) {
            if (Level.fieldOfView[mob.pos] && Dungeon.level.distance(mob.pos, Item.curUser.pos) <= 8) {

                val callback = object : Callback {
                    override fun call() {
                        Item.curUser.attack(targets[this])
                        targets.remove(this)
                        if (targets.isEmpty()) {
                            Item.curUser.spendAndNext(Item.curUser.attackDelay())
                        }
                    }
                }

                (Item.curUser.sprite.parent.recycle(MissileSprite::class.java) as MissileSprite).reset(Item.curUser.pos, mob.pos, proto, callback)

                targets[callback] = mob
            }
        }

        if (targets.size == 0) {
            GLog.w(Messages.get(this, "no_enemies"))
            return
        }

        Item.curUser.HP -= Item.curUser.HP / 3

        Item.curUser.sprite.zap(Item.curUser.pos)
        Item.curUser.busy()
    }

}