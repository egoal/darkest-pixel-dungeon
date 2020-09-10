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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.audio.Sample

abstract class InventoryScroll : Scroll() {
    protected var inventoryTitle = M.L(this, "inv_title")
    protected var mode: WndBag.Mode = WndBag.Mode.ALL

    override fun doRead() {
        if (!isKnown) {
            setKnown()
            identifiedByUse = true
        } else {
            identifiedByUse = false
        }

        GameScene.selectItem(itemSelector, mode, inventoryTitle)
    }

    private fun confirmCancelation() {
        GameScene.show(object : WndOptions(name(), M.L(this, "warning"), M.L(this, "yes"), M.L(this, "no")) {
            override fun onSelect(index: Int) {
                when (index) {
                    0 -> {
                        curUser.spendAndNext(TIME_TO_READ)
                        identifiedByUse = false
                    }
                    1 -> GameScene.selectItem(itemSelector, mode, inventoryTitle)
                }
            }

            override fun onBackPressed() {}
        })
    }

    abstract fun onItemSelected(item: Item)

    companion object {
        // can be protected but not supported by kotlin for now.
        var identifiedByUse = false

        var itemSelector: WndBag.Listener = WndBag.Listener { item ->
            if (item != null) {
                (curItem as InventoryScroll).onItemSelected(item)
                (curItem as InventoryScroll).readAnimation()

                Sample.INSTANCE.play(Assets.SND_READ)
                Invisibility.dispel()

            } else if (identifiedByUse && !(curItem as Scroll).ownedByBook) {
                (curItem as InventoryScroll).confirmCancelation()
            } else if (!(curItem as Scroll).ownedByBook) {
                curItem.collect(curUser.belongings.backpack)
            }
        }
    }
}
