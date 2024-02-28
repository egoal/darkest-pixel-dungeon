package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.WhiteFog
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.MobSpawner
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class ExplodeDying : Ability() {
    override fun onDying(belonger: Mob): Boolean {
        val heroWasAlive = Dungeon.hero.isAlive

        val dmgval = Random.NormalIntRange(Dungeon.depth / 2, Dungeon.depth)

        PathFinder.NEIGHBOURS8.map {
            Actor.findChar(belonger.pos + it)
        }.filter {
            it?.isAlive == true
        }.forEach {
            val damage = Damage(dmgval, belonger, it!!).convertToElement(Damage.Element.FIRE)
            it!!.takeDamage(damage)
        }

        if (Dungeon.visible[belonger.pos]) Sample.INSTANCE.play(Assets.SND_BONES)

        if (heroWasAlive && !Dungeon.hero.isAlive) {
            Dungeon.fail(belonger.javaClass)
            GLog.n(M.L(this, "explo_kill", belonger.name))
        }

        return super.onDying(belonger)
    }
}

class MentalExplodeDying : Ability() {
    override fun onDying(belonger: Mob): Boolean {
        val dis = Dungeon.level.distance(belonger.pos, Dungeon.hero.pos)
        if (Dungeon.hero.isAlive && dis <= 2) {
            val dmg = Damage(Random.NormalIntRange(1, Dungeon.depth / 2) + 1, this, Dungeon.hero).type(Damage.Type.MENTAL)
            Dungeon.hero.takeDamage(dmg)

            if (Random.Int(4) == 0) Dungeon.hero.sayShort(HeroLines.BAD_NOISE)
        }

        if (dis < 4) Sample.INSTANCE.play(Assets.SND_HOWL, 1.2f, 1.2f, 1f)

        return super.onDying(belonger)
    }
}

class RespawnDying(var mobclass: Class<out Mob>? = null) : Ability() {
    var respawnDelay = Random.Int(5, 10)

    override fun onDying(belonger: Mob): Boolean {
        val head = MobSpawner(mobclass ?: belonger.javaClass, respawnDelay)
        head.pos = belonger.pos
        GameScene.add(head)

        if (Dungeon.visible[belonger.pos]) Sample.INSTANCE.play(Assets.SND_BONES)

        return super.onDying(belonger)
    }
}

open class ReleaseGasDying(private val gas: Class<out Blob>) : Ability() {
    override fun onDying(belonger: Mob): Boolean {
        GameScene.add(Blob.seed(belonger.pos, 200, gas))
        return super.onDying(belonger)
    }
}

class ReleaseGasDying_Toxic : ReleaseGasDying(ToxicGas::class.java)

class ReleaseGasDying_WhiteFog : ReleaseGasDying(WhiteFog::class.java)

class RageDying : Ability() {
    override fun onDying(belonger: Mob): Boolean {
        Dungeon.level.mobs
                .filter { Dungeon.level.distance(belonger.pos, it.pos) <= 16 }
                .forEach { it.beckon(belonger.pos) }

        if (Dungeon.visible[belonger.pos])
            CellEmitter.center(belonger.pos).start(Speck.factory(Speck.SCREAM), .3f, 3)

        Sample.INSTANCE.play(Assets.SND_ALERT)

        return super.onDying(belonger)
    }
}