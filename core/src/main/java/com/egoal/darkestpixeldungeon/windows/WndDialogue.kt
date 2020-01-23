package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Chrome
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.Image
import com.watabou.noosa.NinePatch
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.ui.Button

// refactor from wnd options
abstract class WndDialogue(image: Image?, text: String, what: String, vararg options: String) : Window() {
    init {
        val width = if (DarkestPixelDungeon.landscape()) WIDTH_L else WIDTH_P

        val innerWidth = (width - MARGIN * 2).toInt()

        var top = MARGIN
        if (image != null) {
            val title = IconTitle(image, text).apply {
                setRect(MARGIN, MARGIN, innerWidth.toFloat(), 0f)
            }
            add(title)
            top = title.bottom() + MARGIN
        } else {
            val tf = PixelScene.renderMultiline(text, 9).apply {
                hardlight(TITLE_COLOR)
                setPos(MARGIN, MARGIN)
                maxWidth(innerWidth)
            }
            add(tf)
            top = tf.bottom() + MARGIN * 2
        }

        // message
        val rtm = PixelScene.renderMultiline(what, 6).apply {
            maxWidth(innerWidth)
            setPos(MARGIN, top)
        }
        add(rtm)

        // options
        if (options.isNotEmpty()) {
            top = rtm.bottom() + MARGIN + 10f
            for (pr in options.withIndex()) {
                val btn = object : OptionButton(pr.value) {
                    override fun onClick() {
                        hide()
                        onSelect(pr.index)
                    }
                }
                btn.setRect(MARGIN, top, innerWidth.toFloat(), 0f)
                add(btn)

                top = btn.bottom() + MARGIN
            }
        }

        resize(width, top.toInt())
    }

    abstract fun onSelect(idx: Int)

    companion object {
        private const val WIDTH_P = 120
        private const val WIDTH_L = 144

        private const val MARGIN = 2f

        fun Show(mob: Mob, content: String, vararg options: String, callback: (Int) -> Unit) {
            GameScene.show(object : WndDialogue(mob.sprite(), mob.name, content, *options) {
                override fun onSelect(idx: Int) {
                    callback(idx)
                }
            })
        }

        private const val COLOR_GRAY = 0x101010
        private const val MARGIN_OPTION = 4f
        private const val DOT_WIDTH = 6
    }

    open class OptionButton(line: String) : Button() {
        private val background: NinePatch = Chrome.get(Chrome.Type.DIALOG_OPTION)
        private val text: RenderedTextMultiline = PixelScene.renderMultiline(line, 6)
        private val dot = PixelScene.createText("-", 6f)

        init {
            add(background)
            add(dot)
            add(text)
        }

        override fun layout() {
            dot.x = x + MARGIN_OPTION
            dot.y = y + MARGIN_OPTION

            text.maxWidth((width - MARGIN_OPTION * 2 - DOT_WIDTH).toInt())
            text.setPos(x + MARGIN_OPTION + DOT_WIDTH, y + MARGIN_OPTION)
            PixelScene.align(text)

            height = text.height() + MARGIN_OPTION * 2

            background.x = x
            background.y = y
            background.size(width, height)

            super.layout() // update hot area
        }

        override fun onTouchDown() {
            background.brightness(1.2f)
            Sample.INSTANCE.play(Assets.SND_CLICK)
        }

        override fun onTouchUp() {
            background.resetColor()
        }

        fun enable(value: Boolean) {
            active = value
            text.hardlight(COLOR_GRAY)
        }
    }
}