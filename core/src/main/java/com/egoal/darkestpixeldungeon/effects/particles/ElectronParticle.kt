package com.egoal.darkestpixeldungeon.effects.particles

import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.particles.PixelParticle
import com.watabou.utils.Point
import com.watabou.utils.PointF
import com.watabou.utils.Random

class ElectronParticle : PixelParticle.Shrinking() {

    init {
        color(0xffffff)
        lifespan = 0.6f

        // acc.set(0f, Random.Float())

    }

    fun reset(x: Float, y: Float) {
        revive()

        this.x = x
        this.y = y
        left = lifespan

        size = 2f
        speed.set(0f)
    }

    fun resetBurst(x: Float, y: Float) {
        revive()

        this.x = x
        this.y = y
        left = lifespan

        size = 2f
        speed.polar(Random.Float(PointF.PI2), Random.Float(16f, 32f))
    }

    override fun update() {
        super.update()
        val p = left / lifespan
        am = if (p > 0.5f) (1 - p) * 5f else 1f
    }

    companion object {
        val FACTORY = object : Emitter.Factory() {
            override fun emit(emitter: Emitter, index: Int, x: Float, y: Float) {
                (emitter.recycle(ElectronParticle::class.java) as ElectronParticle?)?.resetBurst(x, y)
            }

            override fun lightMode(): Boolean = true
        }
    }
}