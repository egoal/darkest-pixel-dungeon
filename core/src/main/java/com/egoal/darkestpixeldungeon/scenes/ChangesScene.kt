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
package com.egoal.darkestpixeldungeon.scenes

import com.egoal.darkestpixeldungeon.TopExceptionHandler
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.ui.ScrollPane
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.Chrome
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.Archs
import com.egoal.darkestpixeldungeon.ui.ExitButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.windows.WndMessage
// import com.sun.prism.Image;
import com.watabou.input.Touchscreen
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.NinePatch
import com.watabou.noosa.RenderedText
import com.watabou.noosa.ui.Component
import com.watabou.noosa.TouchArea
import com.watabou.utils.Bundle

import java.io.FileInputStream
import java.io.IOException
import java.lang.annotation.Inherited
import java.util.ArrayList

class ChangesScene : PixelScene() {
    override fun create() {
        super.create()

        val w = Camera.main.width
        val h = Camera.main.height

        val title = renderText(Messages.get(this, "title"), 9)
        title.hardlight(Window.TITLE_COLOR)
        title.x = (w - title.width()) / 2
        title.y = 4f
        align(title)
        add(title)

        val btnExit = ExitButton()
        btnExit.setPos(Camera.main.width - btnExit.width(), 0f)
        add(btnExit)


        // add chrome base
        val pw = w - 6
        val ph = h - 20

        val panel = Chrome.get(Chrome.Type.WINDOW)
        panel.size(pw.toFloat(), ph.toFloat())
        panel.x = ((w - pw) / 2).toFloat()
        panel.y = title.y + title.height() + 2f
        add(panel)

        // add scroll text
        val list = ScrollPane(Component())
        add(list)

        val content = list.content()
        content.clear()

        val warning = Messages.get(this, "warning")
        val txtWarning = renderMultiline(warning, 6)
        txtWarning.maxWidth(panel.innerWidth().toInt())
        content.add(txtWarning)

        val text = renderMultiline("_" + DarkestPixelDungeon.version + "_\n" + Messages.get(this, "info" + DarkestPixelDungeon.version), 6)
        text.maxWidth(panel.innerWidth().toInt())
        content.add(text)
        text.setPos(txtWarning.left(), txtWarning.bottom() + 8f)

        // add versions' button
        val HSPLIT = "---"
        val oldVersions = arrayOf(
                "0.7.1", "0.7.0", HSPLIT,
                "0.6.1", "0.6.0.ru", "0.6.0", HSPLIT,
                "0.5.0", HSPLIT,
                "0.4.3", "0.4.2a", "0.4.2", "0.4.1", "0.4.0", HSPLIT,
                "0.3.2a", "0.3.2", "0.3.1a", "0.3.1", "0.3.0", HSPLIT,
                "0.2.4c", "0.2.4b", "0.2.4a", "0.2.4", "0.2.3", "0.2.2b", "0.2.2a", "0.2.2", "0.2.1a", "0.2.1", "0.2.0", HSPLIT,
                "0.1.3", "0.1.2", "0.1.1", "0.1.0")

        run {
            var y = text.bottom() + 8
            var x = BTN_GAP
            for (v in oldVersions) {
                if (v == HSPLIT) {
                    y += BTN_HEIGHT + BTN_GAP + BTN_GAP * 2
                    x = BTN_GAP
                    continue
                }
                if (x + BTN_WIDTH + BTN_GAP > panel.innerWidth()) {
                    y += BTN_HEIGHT + BTN_GAP
                    x = BTN_GAP
                }

                val button = createChangeButton(v)
                button.setRect(x, y, BTN_WIDTH, BTN_HEIGHT)
                content.add(button)

                x += BTN_WIDTH + BTN_GAP
            }

            content.setSize(panel.innerWidth(), y + BTN_HEIGHT + BTN_GAP)
        }

        list.setRect(panel.x + panel.marginLeft(), panel.y + panel.marginTop(), panel.innerWidth(), panel.innerHeight())
        list.scrollTo(0f, 0f)

        val archs = Archs()
        archs.setSize(Camera.main.width.toFloat(), Camera.main.height.toFloat())
        addToBack(archs)

        DarkestPixelDungeon.changeListChecked(true)

        fadeIn()
    }

    override fun onBackPressed() {
        DarkestPixelDungeon.switchNoFade(TitleScene::class.java)
    }

    private fun createChangeButton(version: String): RedButton {
        return object : RedButton(version) {
            override fun onClick() {
                parent.add(ChangesWindow(
                        Messages.get(ChangesScene::class.java, "info$version")))
            }
        }
    }

    private class ChangesWindow(message: String) : WndMessage(message) {
        init {
            add(object : TouchArea(chrome) {
                override fun onClick(touch: Touchscreen.Touch) {
                    hide()
                }
            })
        }
    }

    companion object {
        private const val BTN_WIDTH = 30f
        private const val BTN_HEIGHT = 15f
        private const val BTN_GAP = 2f
    }
}


