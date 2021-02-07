package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class WhiteFog : Blob() {
    override fun evolve() {
        super.evolve()

        val extra = 1 + Dungeon.depth / 5 * 1

        for (ch in affectedChars()) {
            Buff.affect(ch, White::class.java).deepen(1f, extra)
        }
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)

        emitter.pour(Speck.factory(Speck.WHITE_FOG), 0.4f)
    }

    override fun tileDesc(): String? = M.L(this, "desc")

    companion object {
        private const val DAMAGE = "damage"
        private const val LEFT = "left"
    }

    class White : Buff(), Hero.Doom {
        private var damage = 1
        private var left = 2f

        init {
            type = buffType.NEGATIVE
        }

        fun deepen(duration: Float, damage: Int) {
            left += duration
            this.damage += damage
        }

        override fun act(): Boolean {
            if (target.isAlive) {
                val dmg = Damage(Random.Int(1, damage), this, target).type(Damage.Type.MAGICAL)
                target.takeDamage(dmg)

                spend(TICK)
                left -= TICK
                if (left <= 0) detach()
            } else detach()

            return true
        }

        override fun toString(): String = M.L(this, "name")

        override fun onDeath() {
            Dungeon.fail(javaClass)
            GLog.n(M.L(this, "ondeath"))
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(DAMAGE, damage)
            bundle.put(LEFT, left)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            damage = bundle.getInt(DAMAGE)
            left = bundle.getFloat(LEFT)
        }
    }
}