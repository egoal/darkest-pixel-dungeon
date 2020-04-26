package com.egoal.darkestpixeldungeon.levels.traps

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.EarthParticle
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

// actually this is not a trap, huh
class PrizeTrap : Trap() {
    init {
        color = TrapSprite.VIOLET
        shape = TrapSprite.DIAMOND
    }

    override fun activate() {
        // todo: may use a specified heap type.
        val heap = Dungeon.level.heaps.get(pos) ?: Heap().apply {
            pos = this@PrizeTrap.pos
            GameScene.add(this)
        }

        heap.type = when (Random.Int(10)) {
            0 -> Heap.Type.MIMIC
            in 1..3 -> Heap.Type.SKELETON
            else -> Heap.Type.CHEST
        }


        val item = Generator.generate()
        if (item !is Gold)
            heap.drop(Generator.GOLD.generate()) // always give some gold
        heap.drop(item)

        Dungeon.level.heaps.put(heap.pos, heap)
        // GameScene.add(heap)

        if (Dungeon.visible[pos]) {
            heap.sprite.drop()
            GLog.p(M.L(this, "prize_showed"))
            CellEmitter.bottom(pos).start(EarthParticle.FACTORY, 0.05f, 8)
        }
    }
}