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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene
import com.egoal.darkestpixeldungeon.scenes.RankingsScene
import com.egoal.darkestpixeldungeon.scenes.TitleScene
import com.egoal.darkestpixeldungeon.ui.Icons
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.Game

import java.io.IOException

class WndGame : Window() {
    private var pos: Int = 0

    init {
        addButton(object : RedButton(M.L(WndGame::class.java, "settings")) {
            override fun onClick() {
                hide()
                GameScene.show(WndSettings(false))
            }
        })

        // Restart
        if (!Dungeon.hero.isAlive) {
            val btnStart: RedButton = object : RedButton(M.L(WndGame::class.java, "start")) {
                override fun onClick() {
                    Dungeon.nullHero()
                    InterlevelScene.mode = InterlevelScene.Mode.DESCEND
                    InterlevelScene.noStory = true
                    Game.switchScene(InterlevelScene::class.java)
                }
            }
            btnStart.icon(Icons[Dungeon.hero.heroClass])
            addButton(btnStart)

            addButton(object : RedButton(M.L(WndGame::class.java, "rankings")) {
                override fun onClick() {
                    InterlevelScene.mode = InterlevelScene.Mode.DESCEND
                    Game.switchScene(RankingsScene::class.java)
                }
            })
        }

        addButtons(
                // Main menu
                object : RedButton(M.L(WndGame::class.java, "menu")) {
                    override fun onClick() {
                        try {
                            Dungeon.saveAll(false)
                        } catch (e: IOException) {
                            DarkestPixelDungeon.reportException(e)
                        }

                        Game.switchScene(TitleScene::class.java)
                    }
                },
                // Quit
                object : RedButton(M.L(WndGame::class.java, "exit")) {
                    override fun onClick() {
                        Game.instance.finish()
                    }
                }
        )

        // Cancel
        addButton(object : RedButton(M.L(WndGame::class.java, "return")) {
            override fun onClick() {
                hide()
            }
        })

        resize(WIDTH, pos)
    }

    private fun addButton(btn: RedButton) {
        add(btn)
        if (pos > 0) pos += GAP
        btn.setRect(0f, (if (pos > 0) pos else 0).toFloat(), WIDTH.toFloat(), BTN_HEIGHT.toFloat())
        pos += BTN_HEIGHT
    }

    private fun addButtons(btn1: RedButton, btn2: RedButton) {
        add(btn1)
        if (pos > 0) pos += GAP
        btn1.setRect(0f, (if (pos > 0) pos else 0).toFloat(), ((WIDTH - GAP) / 2).toFloat(), BTN_HEIGHT.toFloat())
        add(btn2)
        btn2.setRect(btn1.right() + GAP, btn1.top(), WIDTH.toFloat() - btn1.right() - GAP.toFloat(), BTN_HEIGHT.toFloat())
        pos += BTN_HEIGHT
    }

    companion object {
        private const val WIDTH = 120
        private const val BTN_HEIGHT = 20
        private const val GAP = 2
    }
}
