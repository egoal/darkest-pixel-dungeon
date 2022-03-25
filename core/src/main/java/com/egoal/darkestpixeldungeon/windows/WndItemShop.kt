package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Merchant
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.ItemSlot
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.BitmapText
import com.watabou.noosa.ColorBlock
import com.watabou.noosa.audio.Sample

class WndItemShop : Window() {
    private val items = listOf<Item>()
    private val goodsButtons = arrayListOf<GoodsButton>()

    init {
        val it = IconTitle()
        it.setRect(MARGIN, MARGIN, SHOP_WIDTH - MARGIN * 2, 0f)
        add(it)

        val line = ColorBlock(SHOP_WIDTH - MARGIN * 2, 1f, 0xff222222.toInt())
        line.x = MARGIN
        line.y = it.bottom() + GAP
        add(line)

        val btm = placeItems(line.y+ line.height()+ GAP)
        resize(SHOP_WIDTH.toInt(), btm.toInt())
    }

    private fun placeItems(top: Float):Float {
        var btnHeight = 0f
        for (pr in items.withIndex()) {
            val r = pr.index / SLOT_COLS
            val c = pr.index % SLOT_COLS

            val btn = object : GoodsButton(pr.value) {
                override fun onClick() {
                    onItemClicked(pr.value)
                }
            }
            btnHeight = btn.actualHeight()
            btn.setPos(c * (btn.width() + SLOT_MARGIN), top + r * (btn.actualHeight() + SLOT_MARGIN))
            add(btn)
            goodsButtons.add(btn)
        }

        val rows = (items.size - 1) / SLOT_COLS + 1
        return top + rows * (btnHeight + SLOT_MARGIN)
    }

    private fun onItemClicked(item: Item){

    }

    private open class GoodsButton(item: Item) : ItemSlot(item) {
        private lateinit var bg: ColorBlock
        private lateinit var price: BitmapText

        init {
//            price.text(buyPrice(item).toString())
            price.measure()
            price.hardlight(0xffff00)

            width = GOODS_BTN_SIZE
            height = GOODS_BTN_SIZE // + price.height() + GAP
        }

        fun actualHeight() = GOODS_BTN_SIZE + price.height() + GAP

        fun redPrice(on: Boolean = true) {
            if (on) price.hardlight(0xff0000)
            else price.hardlight(0xffff00)
        }

        override fun createChildren() {
            bg = ColorBlock(GOODS_BTN_SIZE, GOODS_BTN_SIZE, GOODS_BTN_BG)
            add(bg)

            price = BitmapText(PixelScene.pixelFont)
            add(price)

            super.createChildren()
        }

        override fun layout() {
            super.layout()

            bg.x = x
            bg.y = y

            price.x = bg.x + bg.width() / 2f - price.width() / 2f
            price.y = bg.y + bg.height() + GAP
        }

        override fun onTouchDown() {
            bg.brightness(1.5f)
            Sample.INSTANCE.play(Assets.SND_CLICK, 0.7f, 0.7f, 1.2f)
        }

        override fun onTouchUp() {
            bg.resetColor()
        }
    }

    companion object {
        private const val SLOT_COLS = 5
        private const val SLOT_MARGIN = 1
        private const val GAP = 2f
        private const val MARGIN = 2f

        private const val GOODS_BTN_BG = -0x66aca9b3
        private const val GOODS_BTN_SIZE = 20f

        private const val SHOP_WIDTH = (GOODS_BTN_SIZE + SLOT_MARGIN) * SLOT_COLS + SLOT_MARGIN
    }
}