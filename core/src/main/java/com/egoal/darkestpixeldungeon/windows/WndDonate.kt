package com.egoal.darkestpixeldungeon.windows

import android.content.Intent
import android.net.Uri
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.CatEgoal
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.Game

// todo:
class WndDonate : Window() {
    init {
        val width = if (DarkestPixelDungeon.landscape()) WIDTH_L else WIDTH_P
        val innerWidth = (width - MARGIN * 2).toInt()

        val cat = CatEgoal()
        val title = IconTitle(cat.sprite(), cat.name)
        title.setRect(MARGIN, MARGIN, innerWidth.toFloat(), 0f)
        add(title)
        var top = title.bottom() + MARGIN

        val info = PixelScene.renderMultiline(M.L(this, "info"), 6)
        info.setPos(MARGIN, top)
        info.maxWidth(innerWidth)
        top = info.bottom() + MARGIN
        add(info)

        val button = object : WndDialogue.OptionButton(M.L(WndDonate::class.java, "ok")) {
            override fun onClick() {
                hide()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WEB))
                Game.instance.startActivity(intent)
            }
        }
        button.setRect(MARGIN, top, innerWidth.toFloat(), BTN_HEIGHT)
        add(button)
        top = button.bottom() + MARGIN

        val button2 = object : WndDialogue.OptionButton(M.L(WndDonate::class.java, "fine")) {
            override fun onClick() {
                hide()
            }
        }
        button2.setRect(MARGIN, top, innerWidth.toFloat(), BTN_HEIGHT)
        add(button2)
        top = button2.bottom() + MARGIN

        resize(width, top.toInt())
    }

    companion object {
        private const val WIDTH_P = 120
        private const val WIDTH_L = 144

        private const val MARGIN = 2f

        private const val BTN_HEIGHT = 20f

        private const val WEB = "https://afdian.net/@egoal-rl"
    }
}