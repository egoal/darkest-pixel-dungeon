package com.egoal.darkestpixeldungeon.sprites

import com.watabou.noosa.TextureFilm

// finally... i add this
open class SimpleMobSprite(texfile: String) : MobSprite() {
    init {
        texture(texfile)

        // set animations
        val frames = TextureFilm(texture, 16, 16)
        idle = Animation(1, true)
        idle.frames(frames, 0, 1)

        die = Animation(20, false)
        die.frames(frames, 0)

        run = idle.clone()
        attack = idle.clone()

        play(idle)
    }
}