package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle

/**
 * Created by 93942 on 7/29/2018.
 */

class RoaringFire : Fire() {
    override fun burn(pos: Int) {
        val ch = Actor.findChar(pos)
        if (ch != null)
            Buff.prolong(ch, Cripple::class.java, Cripple.DURATION / 2)

        super.burn(pos)
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)
        emitter.start(FlameParticle.FACTORY, 0.03f, 0)

    }

    //todo: use different color, more 'red'
    class RoaringFlameParticle : FlameParticle() {
        init {
            color(0xEE3322)
        }

    }
}
