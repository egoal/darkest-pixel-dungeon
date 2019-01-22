package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.utils.Random

class SmokeOfBlindness : Blob() {
    override fun evolve() {
        super.evolve()

        for (cell in area.points.map { Dungeon.level.pointToCell(it) })
            if (cur[cell] > 0) {
                val ch = Actor.findChar(cell)
                if (ch != null && !ch.immunizedBuffs().contains(javaClass))
                    Buff.prolong(ch, Blindness::class.java, Random.Int(1, 3).toFloat())
            }
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)

        emitter.pour(Speck.factory(Speck.DPD_FOG), .25f)
    }

    override fun tileDesc(): String = Messages.get(this, "desc")
}