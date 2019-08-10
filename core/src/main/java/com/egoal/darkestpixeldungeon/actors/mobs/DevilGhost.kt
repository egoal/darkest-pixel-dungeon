package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.unclassified.DemonicSkull
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.tweeners.AlphaTweener
import com.watabou.utils.Bundle
import com.watabou.utils.Random

/**
 * Created by 93942 on 6/18/2018.
 */

class DevilGhost : Wraith() {
    init {
        spriteClass = DevilGhostSprite::class.java

        HT = 4
        HP = HT
        EXP = 1

        loot = DemonicSkull()
        lootChance = 1f
    }

    override fun giveDamage(target: Char): Damage {
        return if (target is Hero) {
            Damage(Math.min(Random.Int(level / 2) + 2, 10), this, target).type(Damage.Type.MENTAL)
        } else Damage(Random.Int(0, 6) + level / 2, this, target).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.ACCURATE)

    }

    override fun takeDamage(dmg: Damage): Int {
        if (dmg.value > 0) {
            dmg.value = 1
        }

        return super.takeDamage(dmg)
    }

    override fun adjustStats(level: Int) {
        this.level = level
        defenseSkill = 10 + level * 2
        enemySeen = true
    }

    override fun createLoot(): Item? = if (!Dungeon.limitedDrops.demonicSkull.dropped()) {
        Dungeon.limitedDrops.demonicSkull.drop()
        super.createLoot()
    } else Gold(Random.NormalIntRange(80, 150))

    override fun add(buff: Buff) {
        //in other words, can't be directly affected by buffs/debuffs.
    }

    // sprite
    class DevilGhostSprite : MobSprite() {
        init {

            texture(Assets.DEVIL_GHOST)

            val frames = TextureFilm(texture, 14, 24)

            idle = Animation(10, true)
            idle.frames(frames, 0)

            run = Animation(10, true)
            run.frames(frames, 0)

            attack = Animation(15, false)
            attack.frames(frames, 0, 0, 0)

            die = Animation(10, false)
            die.frames(frames, 0)

            play(idle)
        }

        override fun die() {
            super.die()

            emitter().start(ElmoParticle.FACTORY, 0.03f, 60)
        }

        override fun blood(): Int {
            return -0x333334
        }
    }

    companion object {

        fun SpawnAt(pos: Int): DevilGhost? {
            if (Level.passable[pos] && Actor.findChar(pos) == null) {
                val dg = DevilGhost()
                dg.adjustStats(Dungeon.depth)
                dg.pos = pos
                dg.state = dg.HUNTING

                GameScene.add(dg, 1f)

                dg.sprite.alpha(0f)
                dg.sprite.parent.add(AlphaTweener(dg.sprite, 1f, .5f))
                dg.sprite.emitter().burst(ShadowParticle.CURSE, 8)

                return dg
            } else {
                return null
            }
        }
    }
}
