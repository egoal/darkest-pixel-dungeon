package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random

/**
 * Created by 93942 on 4/25/2018.
 */

class HighlyToxicGas : Blob(), Hero.Doom {

    override fun evolve() {
        super.evolve()

        // %40 sharper
        val levelDamage = (5 + Dungeon.depth * 5) * 7 / 5

        affectedChars().forEach {
            var dmg = (it.HT + levelDamage) / 40
            if (Random.Int(40) < (it.HT + levelDamage) % 40) dmg += 1
            it.takeDamage(Damage(dmg, this, it).addElement(Damage.Element.POISON))
        }
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)

        emitter.pour(Speck.factory(Speck.DPD_HIGHLY_TOXIC), 0.4f)
    }

    override fun tileDesc(): String? {
        return Messages.get(this, "desc")
    }

    override fun onDeath() {
        Badges.validateDeathFromGas()

        Dungeon.fail(javaClass)
        GLog.n(Messages.get(this, "ondeath"))
    }
}
