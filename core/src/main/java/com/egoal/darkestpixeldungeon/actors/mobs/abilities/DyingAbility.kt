package com.egoal.darkestpixeldungeon.actors.mobs.abilities

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.MobSpawner
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class ExplodeDyingAbility : Ability() {
    override fun onDying(belonger: Mob): Boolean {
        val heroWasAlive = Dungeon.hero.isAlive

        val dmgval = Random.NormalIntRange(Dungeon.depth / 2, Dungeon.depth)

        PathFinder.NEIGHBOURS8.map {
            Actor.findChar(belonger.pos + it)
        }.filter {
            it?.isAlive == true
        }.forEach {
            val damage = Damage(dmgval, belonger, it).addElement(Damage.Element.FIRE)
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

class MentalExplodeDyingAbility : Ability() {
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

class RespawnDyingAbility(var mobclass: Class<out Mob>? = null) : Ability() {
    var respawnDelay = Random.Int(5, 10)

    override fun onDying(belonger: Mob): Boolean {
        val head = MobSpawner(mobclass, respawnDelay)
        head.pos = belonger.pos
        GameScene.add(head)

        if (Dungeon.visible[belonger.pos]) Sample.INSTANCE.play(Assets.SND_BONES)

        return super.onDying(belonger)
    }
}