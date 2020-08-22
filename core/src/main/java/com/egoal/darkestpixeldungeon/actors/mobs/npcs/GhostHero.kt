package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.SparkParticle
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfPsionicBlast
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.GhostSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Callback
import com.watabou.utils.Random
import java.util.HashSet
import kotlin.math.ceil
import kotlin.math.min

class GhostHero(var roseLevel: Int = 0) : NPC(), Callback {
    private var timeLeft = 0f

    init {
        spriteClass = GhostSprite::class.java

        flying = true

        state = WANDERING
        enemy = null

        camp = Camp.HERO

        defSkill = (Dungeon.hero.lvl + 4) * 2f
        HT = 10 + roseLevel * 5
        HP = HT
        timeLeft = 30f * roseLevel + 60f
    }

    private var cdCure = 0f

    override fun interact(): Boolean {
        if (!DriedRose.TalkedTo) {
            DriedRose.TalkedTo = true
            GameScene.show(WndQuest(this, Messages.get(this, "introduce")))
            return false
        } else {
            // swap 
            swapPosition(Dungeon.hero)
            Dungeon.hero.spend(1f/Dungeon.hero.speed())
            Dungeon.hero.busy()
            
            return true
        }
    }

    override fun act(): Boolean {
        timeLeft -= Actor.TICK
        if (timeLeft <= 0) {
            sayTimeout()
            die(null)
            return true
        }

        if (!Dungeon.hero.isAlive) {
            sayHeroKilled()
            die(null)
            return true
        }

        cdCure -= Actor.TICK //fixme: this is bad, act speed is not constant 1  
        if (cdCure <= 0f && Dungeon.hero.HP < Dungeon.hero.HT &&
                Dungeon.level.distance(pos, Dungeon.hero.pos) <= 2) {
            sprite.turnTo(pos, Dungeon.hero.pos)
            cdCure = COOLDOWN_CURE
            // heal hero
            with(Dungeon.hero) {
                val ratio = 0.05f + roseLevel * 0.01f
                val dhp = min(ceil(HT * ratio).toInt(), HT - HP)
                HP += dhp
                sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, Random.IntRange(1, 4))
            }
            sayCured()

            spend(Actor.TICK)

            return true
        }

        return super.act()
    }

    override fun getCloser(target: Int): Boolean {
        val theTarget = if (state == WANDERING || Dungeon.level.distance(target, Dungeon.hero.pos) > 6) {
            this.target = Dungeon.hero.pos
            this.target
        } else target

        return super.getCloser(theTarget)
    }

    override fun canAttack(enemy: Char): Boolean = Dungeon.level.distance(pos, enemy.pos) <= 4 &&
            Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos

    override fun doAttack(enemy: Char): Boolean {
        if (Dungeon.level.distance(pos, enemy.pos) <= 1)
            return super.doAttack(enemy)

        val visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos]
        if (visible) {
            sprite.zap(enemy.pos)
        } else {
            call()
        }

        return !visible
    }


    override fun call() {
        spend(1f)

        val dmg = giveDamage(enemy)
        if (enemy.checkHit(dmg)) {
            enemy.defendDamage(dmg)
            enemy.takeDamage(dmg)

            enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3)
            enemy.sprite.flash()

            if (enemy === Dungeon.hero) {
                Camera.main.shake(2f, 0.3f)
                if (!enemy.isAlive) {
                    Dungeon.fail(javaClass)
                    GLog.n(Messages.get(this, "zap-kill"))
                }
            }

        } else enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb())

        next()
    }

    override fun attackSkill(target: Char): Float = defSkill / 2 + 5

    override fun giveDamage(enemy: Char): Damage {
        val lvl = (HT - 10) / 3
        return Damage(Random.NormalIntRange(lvl / 2, 5 + lvl), this, enemy).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.RANGED)
    }

    override fun defendDamage(dmg: Damage): Damage = dmg.apply { value -= Random.NormalIntRange(0, (HT - 10) / 3) }

    override fun add(buff: Buff) {}

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    override fun die(cause: Any?) {
        sayDefeated()
        super.die(cause)
    }

    override fun destroy() {
        DriedRose.Spawned = false
        super.destroy()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(TIME_LEFT, timeLeft)
        bundle.put(COOLDOWN_CURE_STR, cdCure)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        timeLeft = bundle.getFloat(TIME_LEFT)
        cdCure = bundle.getFloat(COOLDOWN_CURE_STR)
    }

    // voice 
    fun saySpawned() {
        val depth = (Dungeon.depth - 1) / 5
        val str = if (chooseEnemy() == null) {
            if (depth == 5) Messages.get(this, "voice_ambient_5_0")
            else Messages.get(this, "voice_ambient_${depth}_${Random.Int(3)}")
        } else {
            if (depth == 5) Messages.get(this, "voice_enemies_5_0")
            else Messages.get(this, if (Dungeon.bossLevel()) "voice_bosses_${depth}_${Random.Int(3)}"
            else "voice_enemies_${depth}_${Random.Int(3)}")
        }

        say(str)
    }

    fun sayAnhk() {
        say(Messages.get(this, "voice_blessedankh_${Random.Int(3)}"))
    }

    fun sayDefeated() {
        val i = if (Dungeon.bossLevel()) 1 else 0
        say(Messages.get(this, "voice_defeated_${i}_${Random.Int(3)}"))
    }

    fun sayTimeout() {
        yell(Messages.get(this, "voice_timeout_${Random.Int(3)}"))
    }

    fun sayHeroKilled() {
        say(Messages.get(this, "voice_herokilled_${Random.Int(3)}"))
    }

    fun sayBossBeaten() {
        val i = if (Dungeon.depth == 25) 1 else 0
        say(Messages.get(this, "voice_bossbeaten_${i}_${Random.Int(2)}"))
    }

    fun sayCured() {
        say(Messages.get(this, "voice_cured_${Random.Int(3)}"))
    }

    override fun say(str: String) {
        super.say(str)
        Sample.INSTANCE.play(Assets.SND_GHOST)
    }

    companion object {
        private const val TIME_LEFT = "time-left"
        private const val COOLDOWN_CURE = 15f
        private const val COOLDOWN_CURE_STR = "cool-down"

        private val IMMUNITIES = hashSetOf<Class<*>>(ToxicGas::class.java, VenomGas::class.java, Burning::class.java, ScrollOfPsionicBlast::class.java, Corruption::class.java)

        fun Instance(): GhostHero? = Dungeon.level.mobs.find { it is GhostHero } as GhostHero?
    }
}