package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Chrome
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.ui.ItemSlot
import com.watabou.noosa.NinePatch
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.ui.Component

open class BtnItem : Component() {
    var bg: NinePatch = Chrome.get(Chrome.Type.BUTTON)
    var slot = object : ItemSlot() {
        override fun onTouchDown() {
            bg.brightness(1.2f)
            Sample.INSTANCE.play(Assets.SND_CLICK)
        }

        override fun onTouchUp() {
            bg.resetColor()
        }

        override fun onClick() {
            super.onClick()
            onSlotClick()
        }
    }.apply { enable(true) }

    var item: Item? = null

    override fun createChildren() {
        super.createChildren()

        add(bg)

        add(slot)

    }

    override fun layout() {
        super.layout()

        bg.x = x
        bg.y = y
        bg.size(width, height)

        slot!!.setRect(x + 2, y + 2, width - 4, height - 4)
    }

    open fun onSlotClick() {}

    fun item(item: Item?) {
        this.item = item
        slot.item(this.item)
    }

}