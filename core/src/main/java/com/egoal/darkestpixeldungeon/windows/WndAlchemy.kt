package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.Icons
import com.egoal.darkestpixeldungeon.ui.ItemSlot
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.ColorBlock

private const val WIDTH = 116f
private const val BTN_SIZE = 32f
private const val CNT_INPUTS = 3

class WndAlchemy : Window() {
    private val inputButtons: Array<BtnItem> = Array(CNT_INPUTS) {
        object : BtnItem() {
            override fun onSlotClick() {
                super.onSlotClick()
                // give back to bag failed.
                if (item?.collect() != null)
                    Dungeon.level.drop(item, Dungeon.hero.pos)
                item(null)
                GameScene.selectItem(itemSelector, WndBag.Mode.SEED, Messages.get(WndAlchemy::class.java, "select"))
            }
        }
    }

    private var outputButton: ItemSlot? = null

    init {
        val titleBar = IconTitle().apply {
            icon(DungeonTilemap.tile(Terrain.ALCHEMY))
            label(Messages.get(this, "title"))
            setRect(0f, 0f, WIDTH, 0f)
        }
        add(titleBar)

        var h = titleBar.height() + 6f

        for (btn in inputButtons) {
            btn.setRect(10f, h, BTN_SIZE, BTN_SIZE)
            add(btn)
            h += BTN_SIZE + 2f
        }

        val btnCombine = object : RedButton("") {
            val arrow = Icons.get(Icons.RESUME)

            override fun createChildren() {
                super.createChildren()
                add(arrow)
            }

            override fun layout() {
                super.layout()
                arrow.x = x + (width - arrow.width) / 2f
                arrow.y = y + (height - arrow.height) / 2f
                PixelScene.align(arrow)
            }

            override fun enable(value: Boolean) {
                super.enable(value)
                if (value) {
                    arrow.tint(1f, 1f, 0f, 1f)
                    arrow.alpha(1f)
                    bg.alpha(1f)
                } else {
                    arrow.color(0f, 0f, 0f)
                    arrow.alpha(0.6f)
                    bg.alpha(0.6f)
                }
            }

            override fun onClick() {
                super.onClick()
                combine()
            }
        }.apply {
            enable(false)
            setRect((WIDTH - 30) / 2f, inputButtons[CNT_INPUTS / 2].top() + 5f, 30f, inputButtons[CNT_INPUTS / 2].height() - 10f)
        }
        add(btnCombine)

        outputButton = object : ItemSlot() {
            override fun onClick() {
                super.onClick()
                if (visible && item.trueName() != null)
                    GameScene.show(WndInfoItem(item))
            }
        }.apply {
            setRect(WIDTH - BTN_SIZE - 10, inputButtons[CNT_INPUTS / 2].top(), BTN_SIZE, BTN_SIZE)
            visible = false
        }

        val outputBG = ColorBlock(outputButton!!.width(), outputButton!!.height(), 0x9991938c.toInt()).apply {
            x = outputButton!!.left()
            y = outputButton!!.top()
        }
        add(outputBG)

        add(outputButton)

        resize(WIDTH.toInt(), h.toInt())
    }

    private val itemSelector = object : WndBag.Listener {
        override fun onSelect(item: Item?) {
            if (item != null) {
                for (btn in inputButtons)
                    if (btn.item == null) {
                        btn.item(item.detach(Dungeon.hero.belongings.backpack))
                        break
                    }
                updateState()
            }
        }
    }

    private fun updateState() {}

    private fun combine() {}

}