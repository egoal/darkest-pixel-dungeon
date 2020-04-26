package com.egoal.darkestpixeldungeon.sprites

import com.egoal.darkestpixeldungeon.Assets
import com.watabou.noosa.TextureFilm

/**
 * Created by 93942 on 5/8/2018.
 */

class DisheartenedBuddySprite : MobSprite() {
    init {
        texture(Assets.NOVE)

        val frames = TextureFilm(texture, 12, 15)

        idle = Animation(1, true)
        idle.frames(frames, 0, 0, 0, 0, 0, 1, 0, 1)

        run = Animation(20, true)
        run.frames(frames, 0)

        die = Animation(20, false)
        die.frames(frames, 0)

        play(idle)
    }
}
