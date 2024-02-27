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
package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Dungeon.hero
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndCatalogs
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.Group
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.ui.Button
import com.watabou.noosa.ui.Component
import com.watabou.utils.GameMath
import kotlin.math.abs

class Toolbar : Component() {
    private var btnWait: Tool? = null
    private var btnSearch: Tool? = null
    private lateinit var btnInventory: Tool
    private lateinit var btnQuick: Array<QuickslotTool>
    private lateinit var page1: Group
    private lateinit var page2: Group
    private lateinit var btnSwitchSlots: Tool
    private var pickedUp: PickedUpItem? = null
    private var lastEnabled = true
    private var examining = false
    private var currentQuickSlotTab = 0

    private lateinit var quickYs: Array<Float>

    enum class Mode {
        SPLIT, GROUP, CENTER
    }

    init {
        instance = this
        height = btnInventory.height()
    }

    override fun createChildren() {
        add(object : Tool(24, 0, 20, 24) {
            override fun onClick() {
                examining = false
                hero.rest(false)
            }

            override fun onLongClick(): Boolean {
                examining = false
                hero.rest(true)
                return true
            }
        }.also { btnWait = it })
        add(object : Tool(44, 0, 20, 24) {
            override fun onClick() {
                if (!examining) {
                    GameScene.selectCell(informer)
                    examining = true
                } else {
                    informer.onSelect(null)
                    hero.search(true)
                }
            }

            override fun onLongClick(): Boolean {
                hero.search(true)
                return true
            }
        }.also { btnSearch = it })
        btnSwitchSlots = object : Tool(125, 0, 15, 24) {
            override fun onClick() {
                Sample.INSTANCE.play(Assets.SND_CLICK)
                currentQuickSlotTab = (currentQuickSlotTab + 1) % TAB_QUICK_SLOTS
                updateLayout()
            }
        }
        add(btnSwitchSlots)

        page1 = Group()
        add(page1)
        page2 = Group()
        add(page2)
        btnQuick = Array(TOTAL_SLOTS_COUNT) { i ->
            val b = QuickslotTool(64, 0, 22, 24, i)
            b.setPos(0f, 250f) // invisible on initialization
            b
        }
        quickYs = Array(TOTAL_SLOTS_COUNT) { 0f }
        for ((i, b) in btnQuick.withIndex())
            if (i < NUM_QUICK_SLOTS) page1.add(b)
            else page2.add(b)

        add(object : Tool(0, 0, 24, 26) {
            private var gold: GoldIndicator? = null
            override fun onClick() {
                GameScene.show(WndBag(hero.belongings.backpack, null,
                        WndBag.Mode.ALL, null))
            }

            override fun onLongClick(): Boolean {
                GameScene.show(WndCatalogs())
                return true
            }

            override fun createChildren() {
                super.createChildren()
                gold = GoldIndicator()
                add(gold)
            }

            override fun layout() {
                super.layout()
                gold!!.fill(this)
            }
        }.also { btnInventory = it })
        add(PickedUpItem().also { pickedUp = it })
    }

    override fun layout() {
        // the ys for slots: extra slots is put outside the screen
        val visibleY = y + 2f
        val visibleY2 = y - 10f
        val invisibleY = y + 26f
        val slots = DarkestPixelDungeon.quickSlots()
        val moreSlots = DarkestPixelDungeon.moreQuickSlots()
        for (t in 0 until TAB_QUICK_SLOTS) {
            for (i in 0 until NUM_QUICK_SLOTS) {
                val slot = btnQuick[t * NUM_QUICK_SLOTS + i]
                slot.border(0, 0)
                slot.frame(88, 0, 18, 24)
                if (i == slots - 1 && !moreSlots) {
                    slot.border(0, 0)
                    slot.frame(86, 0, 20, 24)
                }
            }
        }
        if (moreSlots) {
            btnSwitchSlots.frame(125 + 15 * currentQuickSlotTab, 0, 15, 24)
        }

        // Mode.valueOf(DarkestPixelDungeon.toolbarMode())
        // todo: many enable mode align, i mean, someday...
        // split mode
        val right = width
        if (!DarkestPixelDungeon.landscape() && slots > 3) {
            // if more than 3 quick buttons is used, move up
            // 4 lines, text height is 6
            btnSearch!!.setPos(x, y - btnQuick[0].height() - 6 * 5)
            btnWait!!.setPos(x, btnSearch!!.top() - btnSearch!!.height())
        } else {
            // left bottom corner
            btnWait!!.setPos(x, y + 2)
            btnSearch!!.setPos(btnWait!!.right(), btnWait!!.top())
        }

        // bottom right
        btnInventory.setPos(right - btnInventory.width(), y)

        // layout the quick slots
        var switchRight = 0f
        for (t in 0 until TAB_QUICK_SLOTS) {
            val isCurrent = t == currentQuickSlotTab

            var left = btnInventory.left()
            for (i in 0 until NUM_QUICK_SLOTS) {
                val slot = btnQuick[t * NUM_QUICK_SLOTS + i]
                val y = when {
                    i >= slots -> invisibleY
                    isCurrent -> visibleY
                    moreSlots -> visibleY2
                    else -> invisibleY
                }
                quickYs[t * NUM_QUICK_SLOTS + i] = y
                slot.setPos(left - slot.width(), slot.top())
                slot.enable(isCurrent)

                left = slot.left()
                if (i + 1 == slots) switchRight = left
            }
        }
        if (currentQuickSlotTab == 0) bringToFront(page1)
        else bringToFront(page2)

        btnSwitchSlots.setPos(switchRight - btnSwitchSlots.width(), if (moreSlots) visibleY else invisibleY)
    }

    override fun top(): Float {
        val moreSlots = DarkestPixelDungeon.moreQuickSlots()
        return if (moreSlots) y - 10 else y
    }

    override fun update() {
        super.update()
        if (lastEnabled != hero.ready) {
            lastEnabled = hero.ready
            for (tool in members) {
                if (tool is Tool) {
                    tool.enable(lastEnabled)
                }
            }
        }
        if (!hero.isAlive) {
            btnInventory.enable(true)
        }

        for ((btn, y) in btnQuick.zip(quickYs)) {
            btn.setPos(btn.left(), GameMath.Lerp(0.2f, btn.top(), y))
        }
    }

    fun pickup(item: Item?) {
        pickedUp!!.reset(item,
                btnInventory.centerX(),
                btnInventory.centerY(),
                false)
    }

    private open class Tool(x: Int, y: Int, width: Int, height: Int) : Button() {
        private lateinit var base: Image

        init {
            hotArea.blockWhenInactive = true
            frame(x, y, width, height)
        }

        override fun createChildren() {
            super.createChildren()
            base = Image(Assets.TOOLBAR)
            add(base)
        }

        fun frame(x: Int, y: Int, width: Int, height: Int) {
            base.frame(x, y, width, height)
            this.width = width.toFloat()
            this.height = height.toFloat()
        }

        override fun layout() {
            super.layout()
            base.x = x
            base.y = y
        }

        override fun onTouchDown() {
            base.brightness(1.4f)
        }

        override fun onTouchUp() {
            if (active) base.resetColor()
            else base.tint(BGCOLOR, 0.7f)
        }

        open fun enable(value: Boolean) {
            if (value != active) {
                if (value) base.resetColor()
                else base.tint(BGCOLOR, 0.7f)
                active = value
            }
        }

        companion object {
            private const val BGCOLOR = 0x7B8073
        }
    }

    private class QuickslotTool(x: Int, y: Int, width: Int, height: Int, slotNum: Int) : Tool(x, y, width, height) {
        private val slot: QuickSlotButton
        private var borderLeft = 2
        private var borderRight = 2

        init {
            hotArea.blockWhenInactive = false
            slot = QuickSlotButton(slotNum)
            add(slot)
        }

        fun border(left: Int, right: Int) {
            borderLeft = left
            borderRight = right
            layout()
        }

        override fun layout() {
            super.layout()
            slot.setRect(x + borderLeft, y + 2, width - borderLeft - borderRight,
                    height - 4)
        }

        override fun enable(value: Boolean) {
            super.enable(value)
            slot.enable(value)
        }
    }

    class PickedUpItem : ItemSprite() {
        private var dstX = 0f
        private var dstY = 0f
        private var left = 0f
        private var rising = false
        private var distanceX = 0f
        fun reset(item: Item?, dstX: Float, dstY: Float, rising: Boolean) {
            view(item)
            visible = true
            active = visible
            this.rising = rising
            this.dstX = dstX - SIZE / 2
            this.dstY = dstY - SIZE / 2
            left = DURATION
            x = this.dstX - DISTANCE
            y = if (rising) this.dstY + DISTANCE else this.dstY - DISTANCE

            val cell = Dungeon.hero.pos
            val p = DungeonTilemap.tileCenterToWorld(cell)
            val ps = Camera.main.cameraToScreen(p.x - SIZE / 2, p.y - SIZE / 2)
            val spos = camera().screenToCamera(ps.x, ps.y)
            x = spos.x
            y = spos.y
            distanceX = abs(dstX - x)

            alpha(1f)
        }

        override fun update() {
            super.update()

            x = GameMath.Lerp(0.1f, x, dstX)
            y = GameMath.Lerp(0.1f, y, dstY)

            val p = abs(dstX - x) / distanceX
            if (p < 0.5f) {
                scale.set(0.5f + p)

                left -= Game.elapsed
                if (left <= 0f) {
                    active = false
                    visible = active
                    emitter?.on = false
                }
            }
        }

        companion object {
            private const val DISTANCE = DungeonTilemap.SIZE.toFloat()
            private const val DURATION = 1f
        }

        init {
            originToCenter()
            visible = false
            active = visible
        }
    }

    companion object {
        private var instance: Toolbar? =null
        private const val NUM_QUICK_SLOTS = 8
        private const val TAB_QUICK_SLOTS = 2
        private const val TOTAL_SLOTS_COUNT = NUM_QUICK_SLOTS * TAB_QUICK_SLOTS

        @JvmStatic
        fun updateLayout() {
            instance?.layout()
        }

        private val informer: CellSelector.Listener = object : CellSelector.Listener {
            override fun onSelect(cell: Int?) {
                instance!!.examining = false
                GameScene.examineCell(cell)
            }

            override fun prompt(): String {
                return Messages.get(Toolbar::class.java, "examine_prompt")
            }
        }
    }
}