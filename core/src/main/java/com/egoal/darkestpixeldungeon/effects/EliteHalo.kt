package com.egoal.darkestpixeldungeon.effects

import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.watabou.noosa.Game
import com.watabou.noosa.Image
import kotlin.math.sin

class EliteHalo(private val target: CharSprite) : Image(Effects.get(Effects.Type.HALO)) {
    init {
        origin.set(width / 2, height / 2)
        tint(target.blood())
    }

    override fun update() {
        super.update()

        val a = .75f + sin(Game.timeTotal * 3f) * .25f
        alpha(a * target.alpha())

        x = target.x
        y = target.y + 6f + sin(Game.timeTotal * 2f) * 2f
        PixelScene.align(this)

        scale.set(.9f + sin(Game.timeTotal) * .1f, 1f)

        visible = target.visible
    }
}