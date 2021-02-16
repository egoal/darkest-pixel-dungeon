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
package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.items.Catalog
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.ui.ScrollPane
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.Artifact
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.ColorBlock
import com.watabou.noosa.ui.Component

import java.util.ArrayList

class WndCatalogs : WndTabbed() {

    private val btnJournal: RedButton
    private val btnTitle: RedButton
    private val list: ScrollPane

    private val items = ArrayList<ListItem>()

    init {

        resize(WIDTH, HEIGHT)

        btnJournal = object : RedButton(Messages.get(WndJournal::class.java, "title"), 9) {
            override fun onClick() {
                hide()
                GameScene.show(WndJournal())
            }
        }
        btnJournal.setRect(0f, 0f, WIDTH / 2f - 1, btnJournal.reqHeight())
        PixelScene.align(btnJournal)
        add(btnJournal)

        //does nothing, we're already in the catalog
        btnTitle = RedButton(Messages.get(this, "title"), 9)
        btnTitle.textColor(Window.TITLE_COLOR)
        btnTitle.setRect(WIDTH / 2f + 1, 0f, WIDTH / 2f - 1, btnTitle.reqHeight())
        PixelScene.align(btnTitle)
        add(btnTitle)

        list = object : ScrollPane(Component()) {
            override fun onClick(x: Float, y: Float) {
                val size = items.size
                for (i in 0 until size) {
                    if (items[i].onClick(x, y)) {
                        break
                    }
                }
            }
        }
        add(list)
        list.setRect(0f, btnTitle.height() + 1, width.toFloat(), height.toFloat() - btnTitle.height() - 1f)

        val tabids = arrayOf("potions", "scrolls", "rings", "artifacts")
        for (i in tabids.indices) {
            val tab = object : WndTabbed.LabeledTab(M.L(WndCatalogs::class.java, tabids[i])) {
                override fun select(value: Boolean) {
                    super.select(value)
                    if (selected) CurrentIndex = i
                    updateList()
                }
            }

            add(tab)
        }

        layoutTabs()

        select(CurrentIndex)
    }

    private fun updateList() {
        //todo:
        val itemList = ArrayList<Class<out Item>>()
        val addCat = { cat: Catalog ->
            itemList.addAll(cat.allItems().map { it as Class<out Item> }.sortedBy { if (Catalog.IsSeen(it)) 0 else 1 })
        }

        when (CurrentIndex) {
            0 -> {
                itemList.addAll(Potion.known)
                itemList.addAll(Potion.unknown)
            }
            1 -> {
                itemList.addAll(Scroll.known)
                itemList.addAll(Scroll.unknown)
            }
            2 -> addCat(Catalog.Ring)
            3 -> addCat(Catalog.ARTIFACT)
        }

        showList(itemList)
    }

    //todo:
    private fun showList(itemList: List<Class<out Item>>) {
        items.clear()

        val content = list.content()
        content.clear()
        list.scrollTo(0f, 0f)

        var pos = 0f
        for (cls in itemList) {
            val item = ListItem(cls)
            item.setRect(0f, pos, width.toFloat(), ITEM_HEIGHT)
            content.add(item)
            items.add(item)

            pos += item.height()
        }

        content.setSize(width.toFloat(), pos)
        list.setSize(list.width(), list.height())
    }

    private class ListItem(cl: Class<out Item>) : Component() {
        val item: Item = cl.newInstance()
        private var identified: Boolean = false

        private lateinit var sprite: ItemSprite
        private lateinit var label: RenderedTextMultiline
        private lateinit var line: ColorBlock

        init {
            val notRandom = item is Ring || item is Artifact
            identified = if (notRandom) Catalog.IsSeen(cl) else item.isIdentified

            if (identified) {
                sprite.view(item.image(), null)
                label.text(Messages.titleCase(item.name()))
            } else {
                sprite.view(ItemSpriteSheet.SOMETHING, null)
                if (notRandom) {
                    label.text(M.L(WndCatalogs::class.java, "unknown"))
                    label.hardlight(0xcccccc)
                } else label.text(item.trueName())
            }
        }

        override fun createChildren() {
            sprite = ItemSprite()
            add(sprite)

            label = PixelScene.renderMultiline(7)
            add(label)

            line = ColorBlock(1f, 1f, -0xddddde)
            add(line)
        }

        override fun layout() {
            sprite.y = y + 1f + (height - 1f - sprite.height) / 2f
            PixelScene.align(sprite)

            line.size(width, 1f)
            line.x = 0f
            line.y = y

            label.maxWidth((width - sprite.width - 1f).toInt())
            label.setPos(sprite.x + sprite.width + 1f, y + 1f + (height - 1f - label.height()) / 2f)
            PixelScene.align(label)
        }

        fun onClick(x: Float, y: Float): Boolean {
            if (inside(x, y)) {
                if (identified) GameScene.show(WndInfoItem(item))
                return true
            } else {
                return false
            }
        }
    }

    companion object {
        private const val WIDTH = 112
        private const val HEIGHT = 141

        private const val ITEM_HEIGHT = 17f

        private var CurrentIndex = 0
    }
}
