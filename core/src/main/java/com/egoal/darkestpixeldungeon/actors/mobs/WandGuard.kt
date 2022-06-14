package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.wands.*
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle
import com.watabou.utils.Callback
import com.watabou.utils.Random

class WandGuard : Mob() {
    init {
        spriteClass = Sprite::class.java

        Config = Config.copy(MaxHealth = (Dungeon.depth / 5 + 1) * 10)

        state = WANDERING
    }

    private var wand: DamageWand = Random.chances(WAND_PROBS).newInstance().apply {
        level(Random.IntRange(Dungeon.depth / 5, Dungeon.depth / 3))
    }

    override fun viewDistance(): Int = 6

    override fun description(): String = M.L(this, "desc", wand.name())

    override fun canAttack(enemy: Char): Boolean = Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos

    override fun doAttack(enemy: Char): Boolean {
        val shot = Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT)

        wand.execute(Dungeon.hero, "") //patch: assign curUser

        if (enemy is Hero) enemy.busy()
        wand.fx(shot, Callback {
            // hit hero 
            val dmg = giveDamage(enemy)
            // enemy.defendDamage(dmg)
            enemy.takeDamage(dmg)

            if (enemy is Hero) enemy.ready()

            if (!enemy.isAlive) {
                Dungeon.fail(dmg.from.javaClass)
                GLog.n(Messages.capitalize(Messages.get(Char::class.java, "kill", (dmg.from as Char).name)))
            }

            //todo: i cannot use Wand::onZap(): coupling with Hero, may find way out
            //todo: fire wand performs not well
        })

        spend(TIME_TO_ZAP) //fixme: this wont wait the above animation

        return true
    }

    private fun damageRoll(): Int = Random.NormalIntRange(2 + 2 * wand.level(), 8 + 3 * wand.level())

    override fun giveDamage(enemy: Char): Damage = Damage(damageRoll(), this, enemy).type(Damage.Type.MAGICAL)

    override fun defendDamage(dmg: Damage): Damage = dmg.apply {
        value -= Random.NormalIntRange(0, value / 2)
    }

    override fun add(buff: Buff) {}

    override fun createLoot(): Item = wand.apply {
        level(0)
        random()
        // never be cursed
        cursed = false
        cursedKnown = true
    }

    // immovable 
    override fun getCloser(target: Int): Boolean = true

    override fun getFurther(target: Int): Boolean = true

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(WAND, wand)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        wand = bundle.get(WAND) as DamageWand
    }

    companion object {
        private const val WAND = "wand"

        private const val TIME_TO_ZAP = 1f

        private val WAND_PROBS = hashMapOf<Class<out DamageWand>, Float>(
                WandOfMagicMissile::class.java to 5f,
                WandOfLightning::class.java to 4f,
                WandOfDisintegration::class.java to 4f,
                WandOfFireblast::class.java to 4f,
                WandOfBlastWave::class.java to 3f,
                WandOfFrost::class.java to 3f,
                WandOfPrismaticLight::class.java to 3f,
                WandOfVenom::class.java to 3f
        )

        class Sprite : MobSprite() {
            init {
                texture(Assets.WAND_GUARD)

                val frames = TextureFilm(texture, 16, 16)

                idle = Animation(10, true)
                idle.frames(frames, 0)

                run = Animation(10, true)
                run.frames(frames, 0)

                attack = Animation(15, false)
                attack!!.frames(frames, 0, 0, 0)

                die = Animation(10, false)
                die.frames(frames, 0)

                play(idle)
            }


            override fun onComplete(anim: Animation) {
                if (anim === die) {
                    emitter().burst(ElmoParticle.FACTORY, 4)
                }
                super.onComplete(anim)
            }

            override fun blood(): Int = 0xce336d

            override fun link(ch: Char) {
                super.link(ch)

                add(State.MARKED)
            }

            override fun die() {
                super.die()

                CellEmitter.get(ch.pos).burst(ShadowParticle.UP, 5)

                remove(State.MARKED)
            }
        }
    }

}