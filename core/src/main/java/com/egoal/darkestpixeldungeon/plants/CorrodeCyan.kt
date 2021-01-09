package com.egoal.darkestpixeldungeon.plants

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.SnowParticle
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class CorrodeCyan : Plant(14) {
    override fun activate() {
        Actor.findChar(pos)?.let {
            val len = Random.Float(10f, 15f)
            Buff.prolong(it, Vulnerable::class.java, len).apply {
                dmgType = Damage.Type.MAGICAL
                ratio = 1.2f
            }
        }

        if (Dungeon.visible[pos])
            CellEmitter.get(pos).burst(SnowParticle.FACTORY, 5)
    }

    class Seed : Plant.Seed() {
        init {
            image = ItemSpriteSheet.SEED_CORRODE_CYAN
        }
    }
}