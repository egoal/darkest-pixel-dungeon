package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm

class QuickFiringGun : Ballista() {
    init {
        PropertyConfiger.set(this, "QuickFiringGun")

        spriteClass = Sprite::class.java
    }

    override fun attackDelay(): Float = super.attackDelay() / 3f // triple speed

    override fun ammoCapacity(): Int = 3

    override fun die(cause: Any?) {
        super.die(cause)
        Badges.validateRare(this)
    }
    
    companion object {
        class Sprite : MobSprite() {
            init {
                texture(Assets.BALLISTA)

                val frames = TextureFilm(texture, 16, 16)

                idle = Animation(2, true)
                idle.frames(frames, 7, 7, 7, 8)

                run = Animation(2, true)
                run.frames(frames, 7, 9)

                attack = Animation(8, false)
                attack.frames(frames, 7, 9, 10)

                zap = attack.clone()

                die = Animation(8, false)
                die.frames(frames, 11, 12, 13)

                play(idle)
            }

            override fun blood(): Int = 0xff80706c.toInt()

            override fun onComplete(anim: Animation) {
                if (anim == die)
                    emitter().burst(ElmoParticle.FACTORY, 4)

                super.onComplete(anim)
            }
        }

    }
}