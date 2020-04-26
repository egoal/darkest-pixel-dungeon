package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.PerkSlot
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.ColorBlock

open class WndSelectPerk(title: String, perks: List<Perk>) : Window() {
    private var index = 0
    protected val perkButtons: MutableList<PerkButton>
    private val description: RenderedTextMultiline
    private val confirm: RedButton

    init {
        val width = (COLS * BUTTON_SIZE + (COLS + 1) * MARGIN).toInt()

        val titleLine = PixelScene.renderMultiline(title.capitalize(), 9).apply {
            hardlight(TITLE_COLOR)
            setPos(MARGIN, MARGIN)
            maxWidth(width - (MARGIN * 2f).toInt())
        }
        add(titleLine)
        var bottom = titleLine.bottom()

        perkButtons = MutableList(perks.size) { PerkButton(it, perks[it]) }
        for (i in 0 until perkButtons.size) {
            val r = i / COLS
            val c = i % COLS
            perkButtons[i].setRect(MARGIN + (BUTTON_SIZE + MARGIN) * c, bottom + MARGIN + (BUTTON_SIZE + MARGIN) * r, BUTTON_SIZE, BUTTON_SIZE)
            add(perkButtons[i])
        }
        bottom = perkButtons[perkButtons.size - 1].bottom()

        description = PixelScene.renderMultiline(6).apply {
            maxWidth(width - (MARGIN * 2f).toInt())
            setPos(MARGIN, bottom + MARGIN)
        }
        add(description)
        bottom += MARGIN * 2f + DESCRIPTION_HEIGHT

        confirm = object : RedButton(M.L(WndSelectPerk::class.java, "confirm")) {
            override fun onClick() {
                hide()
                onPerkSelected(perks[index])
            }
        }
        confirm.setRect(MARGIN, bottom + MARGIN, width - MARGIN * 2f, BUTTON_SIZE)
        add(confirm)
        bottom = confirm.bottom() + MARGIN

        updateStates()

        resize(width, bottom.toInt())
    }

    protected open fun onPerkSelected(perk: Perk) {

    }

    private fun updateStates() {
        for (btn in perkButtons) btn.setSelected(btn.index == index)
        if (index in 0 until perkButtons.size) {
            description.text(perkButtons[index].perk().description())
            confirm.enable(true)
        } else confirm.enable(false)
    }

    inner class PerkButton(val index: Int, perk: Perk) : PerkSlot(perk) {
        private lateinit var bg: ColorBlock

        fun perk(): Perk = perk

        override fun createChildren() {
            super.createChildren()

            bg = ColorBlock(18f, 18f, 0xaaaaaaaa.toInt())
            add(bg)
            sendToBack(bg)
        }

        override fun layout() {
            super.layout()

            bg.x = x + (width - 18) / 2
            bg.y = y + (height - 18) / 2
        }

        override fun onClick() {
            this@WndSelectPerk.index = index
            updateStates()
        }

        fun setSelected(b: Boolean) {
            bg.alpha(if (b) 1f else 0f)
        }
    }

    companion object {
        private const val MARGIN = 2f
        private const val BUTTON_SIZE = 20f
        private const val DESCRIPTION_HEIGHT = 40f

        private const val COLS = 4

    }
}