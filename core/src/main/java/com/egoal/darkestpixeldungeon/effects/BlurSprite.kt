package com.egoal.darkestpixeldungeon.effects

import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.watabou.noosa.Image
import com.watabou.utils.Random
import kotlin.math.min

//todo: use shader
class BlurSprite(val sprite: CharSprite) : Image() {
    init {
        origin.set(sprite.width / 2f, sprite.height / 2f)
        alpha(0.6f)

        texture(sprite.texture)
    }

    override fun update() {
        super.update()

        sprite.alpha(min(sprite.alpha(), 0.8f))
        
        flipHorizontal = sprite.flipHorizontal
        flipVertical = sprite.flipVertical
        frame(sprite.frame())

        x = sprite.x + Random.Float(-2f, 2f)
        y = sprite.y + Random.Float(-2f, 2f)
    }

    fun clear() {
        sprite.resetColor()
        killAndErase()
    }

    companion object {
        fun Blur(sprite: CharSprite): BlurSprite {
            val bs = BlurSprite(sprite)
            sprite.parent?.add(bs)
            return bs
        }
    }
}