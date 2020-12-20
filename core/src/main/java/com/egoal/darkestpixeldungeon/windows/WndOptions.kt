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
package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.Image

open class WndOptions : Window {
    constructor(title: String, message: String, vararg options: String) : super() {

        val width = if (DarkestPixelDungeon.landscape()) WIDTH_L else WIDTH_P

        val tfTitle = PixelScene.renderMultiline(title, 9)
        tfTitle.hardlight(Window.TITLE_COLOR)
        tfTitle.setPos(MARGIN.toFloat(), MARGIN.toFloat())
        tfTitle.maxWidth(width - MARGIN * 2)
        add(tfTitle)

        val pos = addMessageAndOptions(tfTitle.bottom() + MARGIN, width,
                message, *options)

        resize(width, pos.toInt())
    }

    constructor(icon: Image, title: String, message: String, vararg options: String) : super() {

        val width = if (DarkestPixelDungeon.landscape()) WIDTH_L else WIDTH_P

        val ic = IconTitle(icon, title)
        ic.setRect(0f, 0f, width.toFloat(), 0f)
        add(ic)

        val pos = addMessageAndOptions(ic.bottom() + MARGIN, width, message,
                *options)

        resize(width, pos.toInt())
    }

    protected open fun onSelect(index: Int) {}

    private fun addMessageAndOptions(pos: Float, width: Int, message: String,
                                     vararg options: String): Float {
        var pos = pos
        if (message.isNotEmpty())
            pos = addMessage(pos, width, message)

        return addOptions(pos, width, *options)
    }

    private fun addMessage(pos: Float, width: Int, message: String): Float {
        val rtm = PixelScene.renderMultiline(6)
        rtm.text(message, width - MARGIN * 2)
        rtm.setPos(MARGIN.toFloat(), pos)
        add(rtm)

        return rtm.bottom() + MARGIN
    }

    private fun addOptions(pos: Float, width: Int, vararg options: String): Float {
        var pos = pos
        for (i in options.indices) {
            val btn = object : RedButton(options[i]) {
                override fun onClick() {
                    onSelect(i)
                    hide()
                }
            }
            btn.setRect(MARGIN.toFloat(), pos, (width - MARGIN * 2).toFloat(), BUTTON_HEIGHT.toFloat())
            add(btn)

            pos += (BUTTON_HEIGHT + MARGIN).toFloat()
        }

        return pos
    }

    companion object {
        private const val WIDTH_P = 120
        private const val WIDTH_L = 144

        private const val MARGIN = 2
        private const val BUTTON_HEIGHT = 20

        fun Show(title: String, message: String, vararg options: String, onSelected: (Int) -> Unit) {
            GameScene.show(object : WndOptions(title, message, *options) {
                override fun onSelect(index: Int) {
                    onSelected(index)
                }
            })
        }

        fun Show(icon: Image, title: String, message: String, vararg options: String, onSelected: (Int) -> Unit) {
            GameScene.show(object : WndOptions(icon, title, message, *options) {
                override fun onSelect(index: Int) {
                    onSelected(index)
                }
            })
        }

        fun Confirm(title: String, message: String, onConfirmed: () -> Unit) {
            Show(title, message, M.L(WndOptions::class.java, "yes"), M.L(WndOptions::class.java, "no")) {
                if (it == 0) onConfirmed()
            }
        }

        fun Confirm(icon: Image, title: String, message: String, onConfirmed: () -> Unit) {
            Show(icon, title, message, M.L(WndOptions::class.java, "yes"), M.L(WndOptions::class.java, "no")) {
                if (it == 0) onConfirmed()
            }
        }

        fun CreateConfirm(icon: Image, title: String, message: String, onConfirmed: () -> Unit): WndOptions {
            return object : WndOptions(icon, title, message, M.L(WndOptions::class.java, "yes"), M.L(WndOptions::class.java, "no")) {
                override fun onSelect(index: Int) {
                    if (index == 0) onConfirmed()
                }
            }
        }
    }
}
