package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.ui.Button

// button with a single icon

open class IconButton(private var icon: Image) : Button() {
    init {
        add(icon)
    }

    override fun layout() {
        super.layout()

        icon.x = x + (width - icon.width()) / 2f
        icon.y = y + (height - icon.height()) / 2f
        PixelScene.align(icon)
    }

    override fun onTouchDown() {
        icon.brightness(1.5f)
        Sample.INSTANCE.play(Assets.SND_CLICK, 1f, 1f, 0.8f)
    }

    override fun onTouchUp() {
        icon.resetColor()
    }
}