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
package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.scenes.AmuletScene
import com.watabou.noosa.Game
import com.watabou.utils.Bundle

import java.io.IOException
import java.util.ArrayList

class Amulet : Item() {

    init {
        image = ItemSpriteSheet.AMULET

        unique = true
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_END)
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_END) {
            showAmuletScene(false)
        }
    }

    override fun onFirstPick(hero: Hero) {
        super.onFirstPick(hero)
        hero.recoverSanity(Pressure.MAX_PRESSURE) // recover all

        if (!Statistics.AmuletObtained) {
            Statistics.AmuletObtained = true
            Badges.validateVictory()

            hero.spend(-TIME_TO_PICK_UP)
            // add a delayed actor here so pickup behaviour can fully process.
            Actor.addDelayed(object :Actor(){
                override fun act(): Boolean {
                    remove(this)
                    showAmuletScene(true)
                    return false
                }
            }, -5f)
        }
    }

    private fun showAmuletScene(showText: Boolean) {
        try {
            Dungeon.saveAll()
            AmuletScene.noText = !showText
            Game.switchScene(AmuletScene::class.java)
        } catch (e: IOException) {
            DarkestPixelDungeon.reportException(e)
        }

    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    companion object {

        private const val AC_END = "END"
    }

}
