package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.artifacts.HandleOfAbyss
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.Random

class AbyssHero(var level: Int = 0, friendly: Boolean = false) : NPC() {
    init {
        spriteClass = Sprite::class.java

        flying = true
        state = WANDERING
        enemy = null

        hostile = !friendly
        ally = friendly

        if (friendly)
            initLevelStatus(level)
        else
            imitateHeroStatus()

        addResistances(Damage.Element.LIGHT, 0.5f)
        addResistances(Damage.Element.SHADOW, 1.25f)
    }

    private var timeLeft = 0f
    private var maxExp = 0f

    private fun initLevelStatus(lvl: Int) {
        level = lvl

        defSkill = 10f + 2f * level
        HT = 20 + level * 5
        HP = HT

        timeLeft = 20f * level + 50f
        maxExp = 5f + 2f * level
    }

    private fun imitateHeroStatus() {
        level = Dungeon.hero.lvl / 2 - (3 - Dungeon.depth / 5)
        level = GameMath.clamp(level, 1, 10)

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

    override fun isFollower(): Boolean = true

    override fun attackSkill(target: Char): Float = 10f + level * 2f

    override fun giveDamage(enemy: Char): Damage {
        val dmg = Damage(Random.IntRange(1 + level, 5 + 8 * level), this, enemy).addElement(Damage.Element.SHADOW)
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

    override fun chooseEnemy(): Char? {
        if (hostile) return super.chooseEnemy()

        if (enemy == null || !enemy.isAlive || !Dungeon.level.mobs.contains(enemy) || state == WANDERING) {
            val avls = Dungeon.level.mobs.filter {
                it.hostile && Level.fieldOfView[it.pos] && it.state != it.PASSIVE
            }
            enemy = if (avls.isEmpty()) null else Random.element(avls)
        }

        return enemy
    }

    // strengthen
    override fun attackProc(dmg: Damage): Damage {
        if (!hostile) {
            (dmg.to as Mob?)?.let {
                earnExp(it.EXP.toFloat() * (dmg.value.toFloat() / it.HT.toFloat()))
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
        if (hostile) yell(Messages.get(this, "defeat-me"))
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
        bundle.put(ALLY, ally)
        bundle.put(HOSTILE, hostile)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        timeLeft = bundle.getFloat(TIME_LEFT)
        ally = bundle.getBoolean(ALLY)
        hostile = bundle.getBoolean(HOSTILE)
    }

    override fun die(cause: Any?) {
        super.die(cause)

        HandleOfAbyss.SetDefeated()
    }

    companion object {
        private const val TIME_LEFT = "time-left"
        private const val ALLY = "ally"
        private const val HOSTILE = "hostile"

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
                attack.frames(frames, 0, 2, 3, 4, 4)

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