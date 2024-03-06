package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.watabou.gltextures.SmartTexture
import com.watabou.gltextures.TextureCache
import com.watabou.noosa.ColorBlock
import com.watabou.noosa.Image
import com.watabou.noosa.RenderedText
import com.watabou.noosa.ui.Component
import kotlin.math.round

class ResistanceIndicator(val char: Char) : Component() {
    lateinit var resistanceIcons: SmartTexture

    override fun createChildren() {
        resistanceIcons = TextureCache.get(Assets.DPD_CONS_ICONS)
    }

    override fun layout() {
        val elementalText = PixelScene.renderText(M.L(this, "elemental_resistance"), FONT_SIZE)
        elementalText.y = y
        elementalText.x = x
        add(elementalText)

        for (i in Damage.Element.values().indices) {
            val r = i / ICON_COLS
            val c = i % ICON_COLS

            val icon = Image(resistanceIcons)
            icon.frame(ICON_SIZE * i, 16, ICON_SIZE, ICON_SIZE)
            icon.x = GAP + (width - GAP * 2f) / ICON_COLS * c + GAP
            icon.y = elementalText.y + elementalText.height() + GAP + ICON_HEIGHT * r
            add(icon)

            val txt = makePercentText(char.elementalResistance[i])
            txt.x = icon.x + icon.width + GAP
            txt.y = icon.y + (icon.height - txt.baseLine()) / 2f
            add(txt)
        }

        val magicalLine = PixelScene.renderText(M.L(this, "magical_resistance"), FONT_SIZE)
        magicalLine.x = elementalText.x
        magicalLine.y = elementalText.y + elementalText.height() + ICON_HEIGHT * 2f + GAP
        add(magicalLine)

        val magicalTxt = makePercentText(char.magicalResistance())
        magicalTxt.x = magicalLine.x + magicalLine.width() + GAP
        magicalTxt.y = magicalLine.y
        add(magicalTxt)

        height = elementalText.height() + ICON_HEIGHT * 2f + GAP + magicalLine.height()

        val bg = ColorBlock(width + 2f, height + 2f, 0xd680876f.toInt())
        bg.x = x - 1f
        bg.y = y - 2f
        add(bg)
        sendToBack(bg)
    }

    private fun makePercentText(value: Float): RenderedText {
        val percent = round(value * 100f).toInt()
        val line = PixelScene.renderText(String.format("%+2d%%", percent), FONT_SIZE)
        if (percent > 0) line.hardlight(0xf1ca3e)
        else if (percent < 0) line.hardlight(0xff0000)

        return line
    }

    companion object {
        private const val FONT_SIZE = 6
        private const val ICON_SIZE = 8
        private const val GAP = 3f

        private const val ICON_COLS = 3
        private const val ICON_HEIGHT = 12f
    }
}