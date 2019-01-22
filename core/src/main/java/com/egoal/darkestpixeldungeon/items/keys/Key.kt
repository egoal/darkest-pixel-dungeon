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
package com.egoal.darkestpixeldungeon.items.keys

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.StatusPane
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle

abstract class Key(var depth: Int = 0) : Item() {
    init {
        stackable = true
        unique = true
    }

    override fun isSimilar(item: Item) = 
            item.javaClass == javaClass && (item as Key).depth == depth

    override fun doPickUp(hero: Hero): Boolean {
        GameScene.pickUpJournal(this)
        Sample.INSTANCE.play(Assets.SND_ITEM)
        hero.spendAndNext(Item.TIME_TO_PICK_UP)
        StatusPane.needsKeyUpdate = true
        
        return true
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DEPTH, depth)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        depth = bundle.getInt(DEPTH)
    }

    override fun isUpgradable() = false

    override fun isIdentified() = true

    companion object {
        val TIME_TO_UNLOCK = 1f
        
        private val DEPTH = "depth"
    }

}
