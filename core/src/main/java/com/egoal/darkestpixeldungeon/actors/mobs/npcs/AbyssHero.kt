package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.artifacts.HandleOfAbyss
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle
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

        Instance = this // assign instance 
    }

    private var timeLeft = 0f

    private fun initLevelStatus(lvl: Int) {
        level = lvl

        defenseSkill = 10 + 2 * level
        HT = 20 + level * 5
        HP = HT

        timeLeft = 20f * level + 40f
    }

    private fun imitateHeroStatus() {
        level = Dungeon.hero.lvl / 2
        defenseSkill = 10 + Dungeon.hero.lvl
        HT = Dungeon.hero.HT
        HP = HT

        timeLeft = Float.MAX_VALUE // infinity
    }

    override fun interact(): Boolean {
        return false
    }

    override fun isFollower(): Boolean = true
    
    override fun attackSkill(target: Char): Int = 10 + level * 2

    override fun giveDamage(enemy: Char): Damage {
        val dmg = Damage(Random.NormalIntRange(5 + level, 20 + 4 * level), this, enemy).addElement(Damage.Element.SHADOW)
        if (Random.Float() < 0.15f) {
            dmg.value = dmg.value * 5 / 4
            dmg.addFeature(Damage.Feature.CRITCIAL)
        }

        return dmg
    }

    override fun defendDamage(dmg: Damage): Damage {
        dmg.value -= Random.NormalIntRange(0, level * 2)
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
        if (hostile)
            return super.chooseEnemy()

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
            val porton = dmg.value.toFloat() / HT.toFloat()
            Dungeon.hero.buff(HandleOfAbyss.Recharge::class.java)?.gainExp(porton)
        }

        return super.attackProc(dmg)
    }

    // voice
    fun onSpawned() {
        if (hostile)
            yell(Messages.get(this, "defeat-me"))
        else
            yell(Messages.get(this, "spawned"))

        Dungeon.hero.takeDamage(Damage(Random.Int(level) + 1, this, Dungeon.hero).type(Damage.Type.MENTAL))
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(TIME_LEFT, timeLeft)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        timeLeft = bundle.getFloat(TIME_LEFT)
    }

    override fun die(cause: Any?) {
        super.die(cause)

        HandleOfAbyss.setDefeated()
        Instance = null // clear instance
    }

    companion object {
        private const val TIME_LEFT = "time-left"

        var Instance: AbyssHero? = null

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

                remove(CharSprite.State.SOUL_BURNING)
            }

            override fun link(ch: Char) {
                super.link(ch)

                add(CharSprite.State.SOUL_BURNING)
            }

            override fun blood(): Int = 0x000000
        }
    }
}