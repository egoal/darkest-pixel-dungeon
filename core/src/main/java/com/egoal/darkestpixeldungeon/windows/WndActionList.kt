package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.Image

class WndActionList(icon: Image, title: String, actions: List<Action>) : Window() {
    abstract class Action {
        abstract fun Name(): String
        abstract fun Info(): String
        abstract fun Execute()
        abstract fun Disabled(): Boolean
    }

    init {
        val ic = IconTitle(icon, title)
        ic.setRect(0f, 0f, WIDTH, 0f)
        add(ic)
        var pos = ic.bottom() + MARGIN
        for (pr in actions.withIndex()) {
            val btn = object : RedButton(pr.value.Name()) {
                override fun onClick() {
                    hide()
                    pr.value.Execute()
                }
            }
            btn.enable(!pr.value.Disabled())

            if (pr.value.Info().isEmpty()) {
                btn.setRect(MARGIN, pos, WIDTH - MARGIN * 2f, BTN_HEIGHT)
                add(btn)
            } else {
                val btnHelp = object : RedButton("?") {
                    override fun onClick() {
                        GameScene.show(WndOptions(pr.value.Name(), pr.value.Info()))
                    }
                }
                btnHelp.textColor(0xffffff)

                btn.setRect(MARGIN, pos, (WIDTH - MARGIN * 3 - WIDTH_HELP_BUTTON), BTN_HEIGHT)
                add(btn)
                btnHelp.setRect((WIDTH - MARGIN - WIDTH_HELP_BUTTON), pos, WIDTH_HELP_BUTTON, BTN_HEIGHT)
                add(btnHelp)
            }

            pos += BTN_HEIGHT + MARGIN
        }
        resize(WIDTH.toInt(), pos.toInt())
    }

    companion object {
        private const val WIDTH = 120f
        private const val MARGIN = 2f
        private const val BTN_HEIGHT = 20f

        private const val WIDTH_HELP_BUTTON = 15f
    }
}