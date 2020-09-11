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

import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.Camera
import com.watabou.utils.Callback
import com.watabou.utils.PathFinder

class WarriorArmor : ClassArmor() {

    init {
        image = ItemSpriteSheet.ARMOR_WARRIOR
    }

    override fun doSpecial() {
        GameScene.selectCell(leaper)
    }

    companion object {

        private const val LEAP_TIME = 1
        private const val SHOCK_TIME = 3

        private val leaper: CellSelector.Listener = object : CellSelector.Listener {

            override fun onSelect(target: Int?) {
                if (target != null && target != curUser.pos) {

                    val route = Ballistica(curUser.pos, target, Ballistica.PROJECTILE)
                    var cell = route.collisionPos

                    //can't occupy the same cell as another char, so move back one.
                    if (Actor.findChar(cell) != null && cell != curUser.pos)
                        cell = route.path[route.dist - 1]


                    curUser.HP -= curUser.HP / 3

                    val dest = cell
                    curUser.busy()
                    curUser.sprite.jump(curUser.pos, cell, Callback {
                        curUser.move(dest)
                        Dungeon.level.press(dest, curUser)
                        Dungeon.observe()
                        GameScene.updateFog()

                        for (i in PathFinder.NEIGHBOURS8.indices) {
                            val mob = Actor.findChar(curUser.pos + PathFinder
                                    .NEIGHBOURS8[i])
                            if (mob != null && mob !== curUser) {
                                Buff.prolong(mob, Paralysis::class.java, SHOCK_TIME.toFloat())
                            }
                        }

                        CellEmitter.center(dest).burst(Speck.factory(Speck.DUST), 10)
                        Camera.main.shake(2f, 0.5f)

                        curUser.spendAndNext(LEAP_TIME.toFloat())
                    })
                }
            }

            override fun prompt(): String {
                return Messages.get(WarriorArmor::class.java, "prompt")
            }
        }
    }
}