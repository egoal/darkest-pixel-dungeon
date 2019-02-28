package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Amok
import com.egoal.darkestpixeldungeon.actors.buffs.Sleep
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Dart
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Javelin
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.MissileSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.HashSet

class Ballista : Mob() {
    private var loaded = true

    init {
        spriteClass = Sprite::class.java

        HT = 75
        HP = HT

        defenseSkill = 18

        EXP = 12
        maxLvl = 22

        lootChance = 0.3f

        properties.add(Property.MACHINE)

        addResistances(Damage.Element.LIGHT, 0.75f)
    }

    override fun viewDistance(): Int = 6
    
    override fun giveDamage(enemy: Char): Damage =
            Damage(Random.NormalIntRange(16, 30), this, enemy).addFeature(Damage.Feature.RANGED)

    override fun attackSkill(target: Char?): Int = 30

    override fun defendDamage(dmg: Damage): Damage {
        dmg.value -= Random.NormalIntRange(0, 12)
        return super.defendDamage(dmg)
    }

    override fun canAttack(enemy: Char): Boolean {
        val trace = Ballistica(pos, enemy.pos, Ballistica.PROJECTILE)
        return trace.collisionPos == enemy.pos && loaded
    }

    override fun attack(enemy: Char): Boolean {
        loaded = false
        return super.attack(enemy)
    }
    
    override fun onAttackComplete() {
        // loaded = false
        if (Dungeon.level.adjacent(enemy.pos, pos))
            super.onAttackComplete()
        else {
            // show animation
            (sprite.parent.recycle(MissileSprite::class.java) as MissileSprite).reset(pos, enemy.pos, Dart()) {
                next()
                if (enemy != null)
                    attack(enemy)
            }
        }
    }

    override fun attackProc(dmg: Damage): Damage {
        // chance to knock back
        val chance = when (Dungeon.level.distance((dmg.from as Char).pos, (dmg.to as Char).pos)) {
            0, 1 -> 0.4f
            in 2..5 -> 0.25f
            else -> 0f
        }

        if (dmg.to is Char && Random.Float() < chance) {
            val tgt = dmg.to as Char
            val opposite = tgt.pos + (tgt.pos - pos)
            val shot = Ballistica(tgt.pos, opposite, Ballistica.MAGIC_BOLT)

            WandOfBlastWave.throwChar(tgt, shot, 1)
        }

        return super.attackProc(dmg)
    }

    override fun getCloser(target: Int): Boolean {
        return if (!loaded) {
            reload()
            true
        } else
            super.getCloser(target)
    }

    private fun reload() {
        loaded = true
        sprite.showStatus(0xffffff, Messages.get(this, "loaded"))
        if (Dungeon.visible[pos])
            Sample.INSTANCE.play(Assets.SND_RELOAD)
    }

    override fun createLoot(): Item = when (Random.Int(4)) {
        0 -> Dart(Random.IntRange(2, 5))
        1 -> Javelin(Random.IntRange(1, 3))
        else -> Gold().random()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("loaded", loaded)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        loaded = bundle.getBoolean("loaded")
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    companion object {
        val IMMUNITIES = hashSetOf<Class<*>>(Amok::class.java, Terror::class.java, Sleep::class.java)

        class Sprite : MobSprite() {
            init {
                texture(Assets.BALLISTA)

                val frames = TextureFilm(texture, 16, 16)

                idle = Animation(2, true)
                idle.frames(frames, 0, 0, 0, 1)

                run = Animation(2, true)
                run.frames(frames, 0, 2)

                attack = Animation(8, false)
                attack.frames(frames, 0, 2, 3)

                zap = attack.clone()

                die = Animation(8, false)
                die.frames(frames, 4, 5, 6)

                play(idle)
            }

            override fun blood(): Int = 0xff80706c.toInt()

            override fun onComplete(anim: Animation) {
                if (anim == die)
                    emitter().burst(ElmoParticle.FACTORY, 4)

                super.onComplete(anim)
            }
        }

    }
}