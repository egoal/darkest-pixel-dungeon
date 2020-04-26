package com.egoal.darkestpixeldungeon.sprites

import com.egoal.darkestpixeldungeon.Assets
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm

/**
 * Created by 93942 on 9/1/2018.
 */

class PotionSellerSprite : MobSprite() {
    init {

        texture(Assets.POTION_SELLER)

        // set animations
        val frames = TextureFilm(texture, 12, 16)
        idle = MovieClip.Animation(1, true)
        idle.frames(frames, 0, 1)

        die = MovieClip.Animation(20, false)
        die.frames(frames, 0)

        run = idle.clone()
        attack = idle.clone()

        play(idle)
    }
}
