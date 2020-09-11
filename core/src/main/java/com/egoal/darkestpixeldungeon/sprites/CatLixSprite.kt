package com.egoal.darkestpixeldungeon.sprites

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle
import com.watabou.noosa.TextureFilm

class CatLixSprite : MobSprite() {
    init {
        texture(Assets.CAT_LIX)

        val frames = TextureFilm(texture, 12, 14)

        idle = Animation(2, true)
        idle.frames(frames, 0, 1, 2, 3)

        run = Animation(20, true)
        run.frames(frames, 0)

        die = Animation(20, false)
        die.frames(frames, 0)

        play(idle)
    }

    override fun die() {
        super.die()

        // effect
        emitter().start(ShaftParticle.FACTORY, 0.3f, 4)
        emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3)
    }
}
