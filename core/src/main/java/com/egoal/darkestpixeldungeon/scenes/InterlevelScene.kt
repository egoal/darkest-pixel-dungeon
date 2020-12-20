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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.items.artifacts.HomurasShield
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.GameLog
import com.egoal.darkestpixeldungeon.windows.WndError
import com.egoal.darkestpixeldungeon.windows.WndStory
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.RenderedText
import com.watabou.noosa.audio.Music
import com.watabou.noosa.audio.Sample

import java.io.FileNotFoundException
import java.io.IOException

class InterlevelScene : PixelScene() {
    enum class Mode {
        DESCEND, ASCEND, CONTINUE, RESURRECT, RETURN, FALL, RESET, NONE, REFLUX, BACK_TO_PAST
    }

    private enum class Phase {
        FADE_IN, STATIC, FADE_OUT
    }

    private var phase: Phase = Phase.FADE_IN
    private var timeLeft: Float = 0f

    private lateinit var message: RenderedText

    private lateinit var thread: Thread
    private var error: Exception? = null

    override fun create() {
        super.create()

        val text = Messages.get(Mode::class.java, mode.name)

        message = renderText(text, 9)
        message.x = (Camera.main.width - message.width()) / 2
        message.y = (Camera.main.height - message.height()) / 2
        align(message)
        add(message)

        phase = Phase.FADE_IN
        timeLeft = TIME_TO_FADE

        thread = object : Thread() {
            override fun run() {
                try {
                    when (mode) {
                        Mode.DESCEND -> descend()
                        Mode.ASCEND -> ascend()
                        Mode.CONTINUE -> restore()
                        Mode.REFLUX -> reflux()
                        Mode.RESURRECT -> resurrect()
                        Mode.RETURN -> returnTo()
                        Mode.FALL -> fall()
                        Mode.RESET -> reset()
                        Mode.BACK_TO_PAST -> backToPast()
                        Mode.NONE -> {
                        }
                    }

                    if (Dungeon.bossLevel())
                        Sample.INSTANCE.load(Assets.SND_BOSS)

                } catch (e: FileNotFoundException) {
                    error = e
                } catch (e: IOException) {
                    error = e
                }

                if (phase == Phase.STATIC && error == null) {
                    phase = Phase.FADE_OUT
                    timeLeft = TIME_TO_FADE
                }
            }
        }
        thread.start()
    }

    override fun update() {
        super.update()

        val p = timeLeft / TIME_TO_FADE

        when (phase) {
            Phase.FADE_IN -> {
                message.alpha(1 - p)
                timeLeft -= Game.elapsed
                if (timeLeft <= 0f) {
                    if (!thread.isAlive && error == null) {
                        phase = Phase.FADE_OUT
                        timeLeft = TIME_TO_FADE
                    } else phase = Phase.STATIC
                }
            }

            Phase.FADE_OUT -> {
                message.alpha(p)

                if (mode == Mode.CONTINUE || mode == Mode.DESCEND && Dungeon.depth == 0) {
                    Music.INSTANCE.volume(p * (DarkestPixelDungeon.musicVol() / 10f))
                }
                timeLeft -= Game.elapsed
                if (timeLeft <= 0f) Game.switchScene(GameScene::class.java)
            }

            Phase.STATIC -> if (error != null) {
                var errorMsg: String = when (error) {
                    is FileNotFoundException -> M.L(this, "file_not_found")
                    is IOException -> M.L(this, "io_error")
                    else -> throw RuntimeException(error)
                }

                if (mode == Mode.REFLUX) errorMsg = "备存存档不存在，返回。"

                add(object : WndError(errorMsg) {
                    override fun onBackPressed() {
                        super.onBackPressed()
                        if (mode == Mode.REFLUX) Game.switchScene(StartScene::class.java)
                        else {
                            mode = Mode.REFLUX
                            Game.switchScene(InterlevelScene::class.java)
                        }
                    }
                })
                error = null
            }
        }
    }

    @Throws(IOException::class)
    private fun descend() {
        Actor.fixTime()
        if (Dungeon.hero == null) {
            // start a new game
            Dungeon.init()
            if (noStory) {
                Dungeon.chapters.add(WndStory.ID_SEWERS)
                noStory = false
            }
            GameLog.wipe()
        } else {
            Dungeon.hero.holdFollowers(Dungeon.level)
            Dungeon.saveAll(true)
        }

        val level: Level
        if (Dungeon.depth >= Statistics.DeepestFloor) {
            level = Dungeon.newLevel()
        } else {
            Dungeon.depth++
            level = Dungeon.loadLevel(Dungeon.hero.heroClass)
        }
        Dungeon.switchLevel(level, level.entrance)
    }

    @Throws(IOException::class)
    private fun fall() {
        Actor.fixTime()
        Dungeon.hero.holdFollowers(Dungeon.level)
        Dungeon.saveAll(true)

        val level: Level
        if (Dungeon.depth >= Statistics.DeepestFloor) {
            level = Dungeon.newLevel()
        } else {
            Dungeon.depth++
            level = Dungeon.loadLevel(Dungeon.hero.heroClass)
        }
        Dungeon.switchLevel(level, if (fallIntoPit)
            level.pitCell()
        else
            level
                    .randomRespawnCell())
    }

    @Throws(IOException::class)
    private fun ascend() {
        Actor.fixTime()
        Dungeon.hero.holdFollowers(Dungeon.level)

        Dungeon.saveAll()
        Dungeon.depth--
        val level = Dungeon.loadLevel(Dungeon.hero.heroClass)
        Dungeon.switchLevel(level, level.exit)
    }

    @Throws(IOException::class)
    private fun returnTo() {

        Actor.fixTime()
        Dungeon.hero.holdFollowers(Dungeon.level)

        Dungeon.saveAll()
        Dungeon.depth = returnDepth
        val level = Dungeon.loadLevel(Dungeon.hero.heroClass)
        Dungeon.switchLevel(level, returnPos)
    }

    @Throws(IOException::class)
    private fun restore() {
        Actor.fixTime()

        GameLog.wipe()

        // init level
        Dungeon.loadGame(StartScene.CurrentClass)

        if (Dungeon.depth == -1) {
            Dungeon.depth = Statistics.DeepestFloor
            Dungeon.switchLevel(Dungeon.loadLevel(StartScene.CurrentClass), -1)
        } else {
            Dungeon.switchLevel(Dungeon.loadLevel(StartScene.CurrentClass), Dungeon
                    .hero.pos)
        }
    }

    @Throws(IOException::class)
    private fun reflux() {
        Actor.fixTime()

        GameLog.wipe()

        Dungeon.loadBackupGame(StartScene.CurrentClass)
        val level = Dungeon.loadBackupLevel(StartScene.CurrentClass)
        //todo: remove shield only if reflux with shield
        val shield = Dungeon.hero.belongings.getItem(HomurasShield::class.java)
        if (shield != null) {
            shield.cursed = false
            if (shield.isEquipped(Dungeon.hero)) shield.doUnequip(Dungeon.hero, false)
            shield.detach(Dungeon.hero.belongings.backpack)
        }
        Dungeon.switchLevel(level, Dungeon.hero.pos)
    }

    @Throws(IOException::class)
    private fun resurrect() {
        Actor.fixTime()
        Dungeon.hero.holdFollowers(Dungeon.level)

        if (Dungeon.level.locked) {
            Dungeon.hero.resurrect(Dungeon.depth)
            Dungeon.depth--
            val level = Dungeon.newLevel()
            Dungeon.switchLevel(level, level.entrance)
        } else {
            Dungeon.hero.resurrect(-1)
            Dungeon.resetLevel()
        }
    }

    @Throws(IOException::class)
    private fun reset() {
        Actor.fixTime()
        Dungeon.hero.holdFollowers(Dungeon.level)

        Dungeon.depth--
        val level = Dungeon.newLevel()
        //FIXME this only partially addresses issues regarding weak floors.
        Level.weakFloorCreated = false
        Dungeon.switchLevel(level, level.entrance)
    }

    private fun backToPast() {}

    override fun onBackPressed() {
        //Do nothing
    }

    companion object {
        private const val TIME_TO_FADE = 0.3f

        var mode: Mode = Mode.NONE

        var returnDepth: Int = 0
        var returnPos: Int = 0

        var noStory = false

        var fallIntoPit: Boolean = false
    }
}
