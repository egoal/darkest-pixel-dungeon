package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.PerkSlot
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.ColorBlock

class WndSelectPerk(title: String, vararg perks: Perk) : Window() {
    private var index = 0
    private val perkButtons: MutableList<PerkButton>
    private val description: RenderedTextMultiline
    private val confirm: RedButton

    init {
        val width = (perks.size * BUTTON_SIZE + (perks.size + 1) * MARGIN).toInt()

        val titleLine = PixelScene.renderMultiline(title.capitalize(), 9)
        titleLine.hardlight(TITLE_COLOR)
        titleLine.setPos(MARGIN, MARGIN)
        titleLine.maxWidth(width - MARGIN.toInt() * 2)
        add(titleLine)

        var pos = titleLine.bottom() + MARGIN

        perkButtons = MutableList(perks.size) { PerkButton(it, perks[it]) }
        for (i in 0 until perkButtons.size) {
            perkButtons[i].setRect(MARGIN + (BUTTON_SIZE + MARGIN) * i, pos, BUTTON_SIZE, BUTTON_SIZE)
            add(perkButtons[i])
        }
        pos += BUTTON_SIZE + MARGIN

        description = PixelScene.renderMultiline(6)
        description.maxWidth(width - MARGIN.toInt() * 2)
        description.setPos(MARGIN, pos + 2f)
        add(description)
        pos += DESCRIPTION_HEIGHT

        confirm = object : RedButton(M.L(this, "confirm")) {
            override fun onClick() {
                hide()
                addPerkConfirmed()
            }
        }
        confirm.setRect(MARGIN, pos, width.toFloat() - MARGIN * 2, BUTTON_SIZE)
        add(confirm)
        pos += BUTTON_SIZE + MARGIN

        updateStates()

        resize(width, pos.toInt())
    }

    private fun updateStates() {
        for (btn in perkButtons) btn.setSelected(btn.index == index)
        if (index in 0 until perkButtons.size) {
            description.text(perkButtons[index].perk().description())
            confirm.enable(true)
        } else confirm.enable(false)
    }

    private fun addPerkConfirmed() {
        Dungeon.hero.heroPerk.add(perkButtons[index].perk())

        PerkGain.Show(Dungeon.hero!!, perkButtons[index].perk())
    }

    override fun onBackPressed() {
        // super.onBackPressed()
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

        fun CreateWithRandomPositives(title: String, count: Int): WndSelectPerk {
            return WndSelectPerk(title, *Perk.RandomPositives(Dungeon.hero, count).toTypedArray())
        }
    }
}