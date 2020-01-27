package com.egoal.darkestpixeldungeon.effects

import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.RenderedText
import com.watabou.noosa.Visual

class BubbleText : RenderedText() {
    private var timeLeft = 0f
    private var ox = 0f
    private var oy = 0f
    private var cameraZoom = -1f
    lateinit var target: Visual

    override fun update() {
        super.update()

        if (timeLeft > 0f) {
            timeLeft -= Game.elapsed
            if (timeLeft < 0f) kill()
            else {
                x = target.x + PixelScene.align(Camera.main, ox - width() / 2)
                y = target.y + PixelScene.align(Camera.main, oy - height())

                val p = timeLeft / LIFESPAN
                val a = if (p > 0.5f) 1f else p * 2
                alpha(a)
            }
        }
    }

    override fun destroy() {
        kill()
        super.destroy()
    }

    fun reset(target: Visual, x: Float, y: Float, text: String, color: Int) {
        revive()

        this.target = target
        ox = x
        oy = y
        if (cameraZoom != Camera.main.zoom) {
            cameraZoom = Camera.main.zoom
            PixelScene.chooseFont(9f, cameraZoom)
            size(9 * cameraZoom.toInt())
            scale.set(1 / cameraZoom)
        }

        text(text)
        hardlight(color)

        this.x = target.x + PixelScene.align(Camera.main, ox - width() / 2)
        this.y = target.y + PixelScene.align(Camera.main, oy - height())

        timeLeft = LIFESPAN
    }

    companion object {
        private const val LIFESPAN = 4f

        fun Show(target: Visual, x: Float, y: Float, text: String, color: Int) {
            GameScene.sentenceFor(target)?.reset(target, x, y, text, color)
        }
    }
}