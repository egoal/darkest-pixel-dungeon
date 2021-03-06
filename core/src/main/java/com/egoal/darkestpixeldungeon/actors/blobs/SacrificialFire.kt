package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.FlavourBuff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.effects.particles.SacrificialParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class SacrificialFire : Blob() {

    var pos = 0

    override fun evolve() {
        off[pos] = cur[pos]
        volume = off[pos]

        // affect char
        Actor.findChar(pos)?.let {
            if (Dungeon.visible[pos] && it.buff(Marked::class.java) == null) {
                it.sprite.emitter().burst(SacrificialParticle.FACTORY, 20)
                Sample.INSTANCE.play(Assets.SND_BURNING)
            }
            Buff.prolong(it, Marked::class.java, 50f)
        }

        if (Dungeon.visible[pos])
            Journal.add(Journal.Feature.SACRIFICIAL_FIRE)

    }

    // no decrease
    override fun seed(level: Level, cell: Int, amount: Int) {
        super.seed(level, cell, amount)

        cur[pos] = 0
        pos = cell
        cur[pos] = amount
        volume = cur[pos]

        area.setEmpty()
        area.union(cell % level.width(), cell / level.width())
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)

        emitter.pour(SacrificialParticle.FACTORY, 0.04f)
    }

    override fun tileDesc(): String = Messages.get(this, "desc")

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        for (i in cur.indices)
            if (cur[i] > 0) {
                pos = i
                break
            }
    }

    class Marked : FlavourBuff() {
        override fun icon(): Int = BuffIndicator.SACRIFICE

        override fun toString(): String = M.L(this, "name")

        override fun desc(): String = M.L(this, "desc", dispTurns())

        fun onEnemySlayed(ch: Char) {
            if (Sacrifice(ch)) detach()
        }
    }

    companion object {
        fun Sacrifice(ch: Char): Boolean {
            Wound.hit(ch)

            Dungeon.level.blobs[SacrificialFire::class.java]?.let {
                val fire = it as SacrificialFire
                val exp = when (ch) {
                    is Mob -> ch.exp() * Random.IntRange(1, 3)
                    is Hero -> ch.maxExp()
                    else -> 0
                }

                if (exp > 0) {
                    val vol = fire.volume - exp
                    if (vol > 0) {
                        fire.seed(Dungeon.level, fire.pos, vol)
                        GLog.w(Messages.get(SacrificialFire::class.java, "worthy"))
                    } else {
                        // enough!
                        fire.seed(Dungeon.level, fire.pos, 0)
                        Journal.remove(Journal.Feature.SACRIFICIAL_FIRE)

                        GLog.w(Messages.get(SacrificialFire::class.java, "reward"))
                        GameScene.effect(Flare(7, 32f).color(0x66ffff, true).show(
                                ch.sprite.parent, DungeonTilemap.tileCenterToWorld(fire.pos), 2f))
                        Dungeon.level.drop(Prize(), ch.pos).sprite.drop()
                        return true
                    }
                } else {
                    GLog.w(Messages.get(SacrificialFire::class.java, "unworthy"))
                }
            }

            return false
        }

        private fun Prize(): Item = Generator.RUNE.generate()
    }

}