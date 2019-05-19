package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.scenes.ErrorReportScene
import com.watabou.noosa.Game
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.ui.Button

//todo: like ChangesButton, but always blinking...
class ErrorButton : Button() {
    private lateinit var image: Image
    private var time = 0f

    init {
        width = image.width
        height = image.height
    }

    override fun createChildren() {
        super.createChildren()
        image = Icons.WARNING.get()
        add(image)
    }

    override fun layout() {
        super.layout()

        image.x = x
        image.y = y
    }

    override fun onTouchDown() {
        image.brightness(1.5f)
        Sample.INSTANCE.play(Assets.SND_CLICK)
    }

    override fun onTouchUp() {
        image.resetColor()
    }

    override fun update() {
        super.update()

        time += 3f * Game.elapsed
        image.am = Math.sin(time.toDouble()).toFloat() / 2f + .5f + .2f
    }

    override fun onClick() {
        DarkestPixelDungeon.switchNoFade(ErrorReportScene::class.java)
    }
}