package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Amok
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Sleep
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.KnockBackAttack
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Dart
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.MissileSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Callback

open class Ballista : Mob() {
    private var ammo: Int = ammoCapacity()

    init {
        spriteClass = Sprite::class.java

        immunities.addAll(listOf(Amok::class.java, Terror::class.java, Sleep::class.java, Bleeding::class.java))
        abilities.add(KnockBackAttack())
    }

    override fun viewDistance(): Int = 6

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy).addFeature(Damage.Feature.RANGED)

    override fun canAttack(enemy: Char): Boolean {
        val trace = Ballistica(pos, enemy.pos, Ballistica.PROJECTILE)
        return trace.collisionPos == enemy.pos && ammo > 0
    }

    override fun attack(enemy: Char): Boolean {
        ammo -= 1
        return super.attack(enemy)
    }

    override fun onAttackComplete() {
        if (Dungeon.level.adjacent(enemy!!.pos, pos))
            super.onAttackComplete()
        else {
            // show animation
            (sprite.parent.recycle(MissileSprite::class.java) as MissileSprite).reset(pos, enemy!!.pos, Dart(), Callback {
                next()
                if (enemy != null) attack(enemy!!)
            })
        }
    }

    override fun getCloser(target: Int): Boolean {
        return if (ammo <= 0) {
            reload()
            true
        } else
            super.getCloser(target)
    }

    protected open fun ammoCapacity(): Int = 1

    private fun reload() {
        ammo = ammoCapacity()
        if (Dungeon.visible[pos]) {
            sprite.showStatus(0xffffff, Messages.get(this, "loaded"))
            Sample.INSTANCE.play(Assets.SND_RELOAD)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(AMMO, ammo)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        ammo = bundle.getInt(AMMO)
    }

    companion object {
        private const val AMMO = "ammo"

        class Sprite : MobSprite() {
            init {
                texture(Assets.BALLISTA)

                val frames = TextureFilm(texture, 16, 16)

                idle = Animation(2, true)
                idle.frames(frames, 0, 0, 0, 1)

                run = Animation(2, true)
                run.frames(frames, 0, 2)

                attack = Animation(8, false)
                attack!!.frames(frames, 0, 2, 3)

                zap = attack!!.clone()

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