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
package com.egoal.darkestpixeldungeon.sprites

import android.util.Log
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.effects.BlurSprite
import com.egoal.darkestpixeldungeon.effects.BubbleText
import com.egoal.darkestpixeldungeon.effects.CriticalShock
import com.egoal.darkestpixeldungeon.effects.FloatingText
import com.egoal.darkestpixeldungeon.effects.TorchHalo
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.effects.particles.SoulFlameParticle
import com.egoal.darkestpixeldungeon.items.potions.PotionOfInvisibility
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.effects.DarkBlock
import com.egoal.darkestpixeldungeon.effects.EmoIcon
import com.egoal.darkestpixeldungeon.effects.IceBlock
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.effects.particles.SnowParticle
import com.egoal.darkestpixeldungeon.items.unclassified.Torch
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.MovieClip
import com.watabou.noosa.Visual
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.tweeners.PosTweener
import com.watabou.noosa.tweeners.Tweener
import com.watabou.utils.Callback
import com.watabou.utils.GameMath
import com.watabou.utils.PointF
import com.watabou.utils.Random
import kotlin.math.min
import kotlin.math.sqrt

open class CharSprite : MovieClip(), Tweener.Listener, MovieClip.Listener {
    protected lateinit var idle: Animation
    protected lateinit var run: Animation
    protected var attack: Animation? = null
    protected var operate: Animation? = null
    protected var zap: Animation? = null
    protected lateinit var die: Animation

    protected var animCallback: Callback? = null

    protected var motion: Tweener? = null

    protected var burning: Emitter? = null
    protected var chilled: Emitter? = null
    protected var marked: Emitter? = null
    protected var levitation: Emitter? = null

    protected var iceBlock: IceBlock? = null
    protected var darkBlock: DarkBlock? = null
    protected var halo: TorchHalo? = null

    protected var emo: EmoIcon? = null

    protected var soulburning_: Emitter? = null

    private var jumpTweener: Tweener? = null
    private var jumpCallback: Callback? = null

    private var flashTime = 0f

    protected var sleeping = false

    lateinit var ch: Char
    val hasChar: Boolean
        get() = ::ch.isInitialized

    // used to prevent the actor associated with this sprite from acting until movement completes
    @Volatile
    var isMoving = false

    enum class State {
        BURNING, LEVITATING, INVISIBLE, PARALYSED, FROZEN,
        ILLUMINATED, CHILLED, DARKENED, MARKED, SOUL_BURNING,
    }

    init {
        listener = this
    }

    open fun link(ch: Char) {
        this.ch = ch
        ch.sprite = this

        place(ch.pos)
        turnTo(ch.pos, Random.Int(Dungeon.level.length()))

        ch.updateSpriteState()
    }

    fun worldToCamera(cell: Int): PointF {
        val csize = DungeonTilemap.SIZE

        return PointF(
                PixelScene.align(Camera.main, (cell % Dungeon.level.width() + 0.5f) * csize - width * 0.5f),
                PixelScene.align(Camera.main, (cell / Dungeon.level.width() + 1.0f) * csize - height)
        )
    }

    open fun place(cell: Int) {
        point(worldToCamera(cell))
    }

    fun showStatus(color: Int, text: String, vararg args: Any) {
        if (!visible) return

        if (args.isNotEmpty()) showStatus(color, Messages.format(text, *args))

        if (hasChar) {
            val tile = DungeonTilemap.tileCenterToWorld(ch.pos)
            FloatingText.show(tile.x, tile.y - width * 0.5f, ch.pos, text, color)
        } else FloatingText.show(x + width * 0.5f, y, text, color)
    }

    fun showSentence(color: Int, text: String, vararg args: Any) {
        if (!visible) return

        if (args.isNotEmpty()) showSentence(color, Messages.format(text, *args))

        //todo: auto-warp
        BubbleText.Show(this, width / 2f, -height / 4f, text, color)
    }

    fun idle() {
        play(idle)
    }

    open fun move(from: Int, to: Int) {
        turnTo(from, to)

        play(run)

        motion = PosTweener(this, worldToCamera(to), MOVE_INTERVAL)
        motion!!.listener = this
        parent.add(motion!!)

        isMoving = true

        if (visible && Level.water[from] && !ch.flying) {
            GameScene.ripple(from)
        }

    }

    fun interruptMotion() {
        if (motion != null) {
            onComplete(motion!!)
        }
    }

    open fun attack(cell: Int) {
        turnTo(ch.pos, cell)
        play(attack)
    }

    fun attack(cell: Int, callback: Callback) {
        animCallback = callback
        turnTo(ch.pos, cell)
        play(attack)
    }

    fun operate(cell: Int) {
        turnTo(ch.pos, cell)
        play(operate)
    }

    open fun zap(cell: Int) {
        turnTo(ch.pos, cell)
        play(zap)
    }

    fun zap(cell: Int, onZap: Callback) {
        animCallback = onZap
        zap(cell)
    }

    open fun turnTo(from: Int, to: Int) {
        val fx = from % Dungeon.level.width()
        val tx = to % Dungeon.level.width()
        if (tx > fx) {
            flipHorizontal = false
        } else if (tx < fx) {
            flipHorizontal = true
        }
    }

    open fun jump(from: Int, to: Int, callback: Callback?) {
        jumpCallback = callback

        val distance = Dungeon.level.distance(from, to)
        jumpTweener = JumpTweener(this, worldToCamera(to), (distance * 4).toFloat(), distance * 0.1f)
        jumpTweener!!.listener = this
        parent.add(jumpTweener!!)

        turnTo(from, to)
    }

    open fun die() {
        sleeping = false
        play(die)

        emo?.killAndErase()
    }

    fun emitter(): Emitter = GameScene.emitter().apply { pos(this@CharSprite) }

    fun centerEmitter(): Emitter = GameScene.emitter().apply { pos(this@CharSprite.center()) }

    fun bottomEmitter(): Emitter {
        val emitter = GameScene.emitter()
        emitter!!.pos(x, y + height, width, 0f)
        return emitter
    }

    fun burst(color: Int, n: Int) {
        if (visible)
            Splash.at(center(), color, n)
    }

    open fun bloodBurstA(from: PointF, damage: Int) {
        if (visible) {
            val c = center()
            val n = min(9 * sqrt(damage.toFloat() / ch.HT), 9f).toInt()
            Splash.at(c, PointF.angle(from, c), 3.1415926f / 2f, blood(), n)
        }
    }

    // critical blood burst
    fun bloodBurstB(from: PointF, damage: Int) {
        if (visible) {
            val c = center()
            Splash.at(c, PointF.angle(from, c), 3.1415926f / 2, blood(), 12)
        }
    }

    fun spriteBurst(from: PointF, damage: Int) {
        if (visible) {
            val str = GameMath.clampf(sqrt(damage.toFloat() / ch.HT) * 1.5f, 1f, 1.5f)
            CriticalShock.show(ch, PointF.angle(from, center()), str)
        }
    }

    open fun blood(): Int = -0x450000

    fun flash() {
        ga = 1f
        ba = ga
        ra = ba
        flashTime = FLASH_INTERVAL
    }

    fun add(state: State) {
        when (state) {
            State.BURNING -> {
                burning = emitter()
                burning!!.pour(FlameParticle.FACTORY, 0.06f)
                if (visible) {
                    Sample.INSTANCE.play(Assets.SND_BURNING)
                }
            }
            State.LEVITATING -> {
                levitation = emitter()
                levitation!!.pour(Speck.factory(Speck.JET), 0.02f)
            }
            State.INVISIBLE -> PotionOfInvisibility.melt(ch)
            State.PARALYSED -> paused = true
            State.FROZEN -> {
                iceBlock = IceBlock.freeze(this)
                paused = true
            }
            State.ILLUMINATED -> {
                halo = TorchHalo(this)
                GameScene.effect(halo!!)
            }
            State.CHILLED -> {
                chilled = emitter()
                chilled!!.pour(SnowParticle.FACTORY, 0.1f)
            }
            State.DARKENED -> darkBlock = DarkBlock.darken(this)
            State.MARKED -> {
                marked = emitter()
                marked!!.pour(ShadowParticle.UP, 0.1f)
            }
            State.SOUL_BURNING -> {
                soulburning_ = emitter()
                soulburning_!!.pour(SoulFlameParticle.FACTORY, 0.06f)
                if (visible)
                    Sample.INSTANCE.play(Assets.SND_BURNING)
            }
        }
    }

    fun remove(state: State) {
        when (state) {
            State.BURNING -> if (burning != null) {
                burning!!.on = false
                burning = null
            }
            State.LEVITATING -> if (levitation != null) {
                levitation!!.on = false
                levitation = null
            }
            State.INVISIBLE -> alpha(1f)
            State.PARALYSED -> paused = false
            State.FROZEN -> {
                if (iceBlock != null) {
                    iceBlock!!.melt()
                    iceBlock = null
                }
                paused = false
            }
            State.ILLUMINATED -> if (halo != null) {
                halo!!.putOut()
            }
            State.CHILLED -> if (chilled != null) {
                chilled!!.on = false
                chilled = null
            }
            State.DARKENED -> if (darkBlock != null) {
                darkBlock!!.lighten()
                darkBlock = null
            }
            State.MARKED -> if (marked != null) {
                marked!!.on = false
                marked = null
            }
            State.SOUL_BURNING -> if (soulburning_ != null) {
                soulburning_!!.on = false
                soulburning_ = null
            }
        }
    }

    override fun update() {
        super.update()

        if (paused && listener != null) {
            listener.onComplete(curAnim)
        }

        if (flashTime > 0f) {
            flashTime -= Game.elapsed
            if (flashTime <= 0f) resetColor()
        }

        burning?.visible = visible

        levitation?.visible = visible

        iceBlock?.visible = visible

        chilled?.visible = visible

        if (sleeping) {
            showSleep()
        } else {
            hideSleep()
        }

        emo?.visible = visible
    }

    fun showSleep() {
        if (emo !is EmoIcon.Sleep) {
            emo?.killAndErase()

            emo = EmoIcon.Sleep(this)
            emo!!.visible = visible
        }
        idle()
    }

    fun hideSleep() {
        if (emo is EmoIcon.Sleep) {
            emo!!.killAndErase()
            emo = null
        }
    }

    fun showAlert() {
        if (emo !is EmoIcon.Alert) {
            emo?.killAndErase()

            emo = EmoIcon.Alert(this)
            emo!!.visible = visible
        }
    }

    fun hideAlert() {
        if (emo is EmoIcon.Alert) {
            emo!!.killAndErase()
            emo = null
        }
    }

    override fun kill() {
        super.kill()

        emo?.killAndErase()
        emo = null
    }

    override fun onComplete(tweener: Tweener) {
        if (tweener === jumpTweener) {

            if (visible && Level.water[ch.pos] && !ch.flying) {
                GameScene.ripple(ch.pos)
            }
            if (jumpCallback != null) {
                jumpCallback!!.call()
            }

        } else if (tweener === motion) {

            synchronized(this) {
                isMoving = false

                motion!!.killAndErase()
                motion = null
                ch.onMotionComplete()

                (this as java.lang.Object).notifyAll()
            }
        }
    }

    override fun onComplete(anim: Animation) {
        if (animCallback != null) {
            val executing = animCallback!!
            animCallback = null
            executing.call()
        } else {
            if (anim === attack) {
                idle()
                ch.onAttackComplete()
            } else if (anim === operate) {
                idle()
                ch.onOperateComplete()
            }
        }
    }

    private class JumpTweener(var visual: Visual, var end: PointF, var height: Float, time: Float) : Tweener(visual, time) {
        val start: PointF = visual.point()

        override fun updateValues(progress: Float) {
            visual.point(PointF.inter(start, end, progress).offset(0f, -height * 4f * progress * (1 - progress)))
        }
    }

    companion object {
        // Color constants for floating text
        const val DEFAULT = 0xFFFFFF  // 白
        const val POSITIVE = 0x00FF00 // 绿
        const val NEGATIVE = 0xFF0000  // 红
        const val WARNING = 0xFF8800  // 橙
        const val NEUTRAL = 0xFFFF00 // 中立黄

        private const val MOVE_INTERVAL = 0.1f
        private const val FLASH_INTERVAL = 0.05f
    }
}
