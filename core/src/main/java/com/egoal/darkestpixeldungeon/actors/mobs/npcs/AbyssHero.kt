package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Database
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.artifacts.HandleOfAbyss
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfPsionicBlast
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.Random
import java.util.*

class AbyssHero(var level: Int = 0, friendly: Boolean = false) : NPC() {
    init {
        spriteClass = Sprite::class.java

        flying = true
        state = WANDERING
        enemy = null

        camp = if (friendly) Camp.HERO else Camp.ENEMY

        Config = Database.DummyMobConfig
        addResistances(Damage.Element.Light, 0.5f)
        addResistances(Damage.Element.Shadow, 1.25f)

        if (friendly)
            initLevelStatus(level)
        else
            imitateHeroStatus()

    }

    private var timeLeft = 0f
    private var maxExp = 0f

    private fun initLevelStatus(lvl: Int) {
        level = lvl

        atkSkill = 10f + 2f * level
        defSkill = 10f + 2f * level
        HT = 20 + level * 5
        HP = HT

        timeLeft = 20f * level + 50f
        maxExp = 5f + 2f * level
    }

    private fun imitateHeroStatus() {
        level = Dungeon.hero.lvl / 2 - (3 - Dungeon.depth / 5)
        level = GameMath.clamp(level, 1, 10)

        atkSkill = 10f + level * 2f
        defSkill = 5f + Dungeon.hero.lvl
        HT = Math.max(Dungeon.hero.HT / 2, Dungeon.hero.HP)
        HP = HT

        timeLeft = Float.MAX_VALUE // infinity
        maxExp = Float.MAX_VALUE
    }

    override fun interact(): Boolean {
        // swap 
        val curpos = pos
        moveSprite(pos, Dungeon.hero.pos)
        move(Dungeon.hero.pos)

        with(Dungeon.hero) {
            sprite.move(pos, curpos)
            move(curpos)

            spend(1 / speed())
            busy()
        }

        return true
    }

    override fun giveDamage(enemy: Char): Damage {
        val dmg = Damage(Random.IntRange(1 + level, 5 + 6 * level), this, enemy)
                .setAdditionalDamage(Damage.Element.Shadow, Random.NormalIntRange(1, 2 * level))
        if (Random.Float() < 0.15f) {
            dmg.value = dmg.value * 5 / 4
            dmg.addFeature(Damage.Feature.CRITICAL)
        }

        return dmg
    }

    override fun defendDamage(dmg: Damage): Damage {
        dmg.value -= Random.NormalIntRange(0, level)
        return dmg
    }

    override fun add(buff: Buff) {}

    override fun act(): Boolean {
        timeLeft -= Actor.TICK
        if (timeLeft <= 0) {
            die(null)
            return true
        }

        if (!Dungeon.hero.isAlive) {
            die(null)
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

    // strengthen
    override fun attackProc(dmg: Damage): Damage {
        if (camp == Camp.HERO && dmg.to is Mob) {
            (dmg.to as Mob).let {
                earnExp(it.Config.EXP.toFloat() * (dmg.value.toFloat() / it.HT.toFloat()))
            }
        }

        return super.attackProc(dmg)
    }

    fun earnExp(exp: Float) {
        // fix by level
        val ratio = Math.pow(1.35, (Dungeon.depth / 2 - level).toDouble()).toFloat()
        Dungeon.hero.buff(HandleOfAbyss.Recharge::class.java)?.gainExp(exp / maxExp * ratio)
    }

    // voice
    fun onSpawned() {
        if (camp == Camp.ENEMY) yell(Messages.get(this, "defeat-me"))
        else {
            // on each summon, gain exp
            earnExp(maxExp / 5f)
            yell(Messages.get(this, "spawned"))
        }

        Dungeon.hero.takeDamage(Damage(Random.Int(level) + 1, this, Dungeon.hero).type(Damage.Type.MENTAL))
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(TIME_LEFT, timeLeft)
        bundle.put(CAMP, camp)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        timeLeft = bundle.getFloat(TIME_LEFT)
        camp = bundle.getEnum(CAMP, Camp::class.java)
    }

    override fun die(cause: Any?) {
        super.die(cause)

        HandleOfAbyss.SetDefeated()
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUNITIES

    companion object {
        private const val TIME_LEFT = "time-left"
        private const val CAMP = "camp"

        private val IMMUNITIES = hashSetOf<Class<*>>(ToxicGas::class.java, VenomGas::class.java, Burning::class.java, ScrollOfPsionicBlast::class.java, Corruption::class.java)

        fun Instance(): AbyssHero? = Dungeon.level.mobs.find { it is AbyssHero } as AbyssHero?

        class Sprite : MobSprite() {
            init {
                texture(Assets.ABYSS_HERO)

                val frames = TextureFilm(texture, 16, 16)

                idle = Animation(2, true)
                idle.frames(frames, 0, 0, 1, 0)

                run = Animation(2, true)
                run.frames(frames, 0, 2)

                attack = Animation(10, false)
                attack!!.frames(frames, 0, 2, 3, 4, 4)

                die = Animation(8, false)
                die.frames(frames, 0, 2)

                play(idle)
            }

            override fun die() {
                super.die()
                CellEmitter.get(ch.pos).burst(ShadowParticle.UP, 5)

                remove(State.SOUL_BURNING)
            }

            override fun link(ch: Char) {
                super.link(ch)

                add(State.SOUL_BURNING)
            }

            override fun blood(): Int = 0x000000
        }
    }
}