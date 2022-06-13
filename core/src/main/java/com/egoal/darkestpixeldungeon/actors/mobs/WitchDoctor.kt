package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Decayed
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.items.potions.ReagentOfHealing
import com.egoal.darkestpixeldungeon.items.weapon.missiles.CeremonialDagger
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle
import com.watabou.utils.Callback
import com.watabou.utils.Random

class WitchDoctor : Mob() {
    init {
        spriteClass = Sprite::class.java

        PropertyConfiger.set(this, "WitchDoctor")
        HUNTING = HuntingAI()

        loot = Generator.POTION // see creatLoot
    }

    private var decayCD = 0f
    private var healCD = 0f

    override fun act(): Boolean {
        if (decayCD > 0f) decayCD -= TICK
        if (healCD > 0f) healCD -= TICK
        return super.act()
    }

    override fun canAttack(enemy: Char): Boolean = Dungeon.level.distance(pos, enemy.pos) <= 2

    // 1. try decay enemy
    // 2. try heal: ally > enemy > self
    // 3. normal attack or run
    //todo: refactor to behaviour list as i do in cloud-dungeon
    inner class HuntingAI : Hunting() {
        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            // try heal allys
            if (healCD < 0f) {
                val allyToHeal = Dungeon.level.mobs.filter {
                    it !== this@WitchDoctor && it.camp == camp &&
                            Level.fieldOfView[it.pos] && (it.HP.toFloat() / it.HT) < 0.75f
                }.minByOrNull { it.HP.toFloat() / it.HT }
                if (allyToHeal != null) {
                    heal(allyToHeal)
                    return false
                }
            }

            if (enemyInFOV) {
                val enemy = enemy!!

                val enemyIsDecayed = enemy.buff(Decayed::class.java) != null

                if (!enemyIsDecayed && decayCD <= 0f && Dungeon.level.distance(pos, enemy.pos) <= 4) {
                    sprite.zap(enemy.pos, Callback {
                        decayCD = DECAY_CD
                        Buff.prolong(enemy, Decayed::class.java, 15f)
                        spend(TIME_TO_DECAY)
                        next()
                    })
                    return false
                }

                if (healCD <= 0f) {
                    // damage enemy
                    if (enemyIsDecayed && HP >= HT / 5) {
                        heal(enemy)
                        return false
                    }

                    // heal self if needed
                    if (HP <= HT / 2) {
                        heal(this@WitchDoctor)
                        return false
                    }
                }

                // no skill to cast && face to face, run if low health...
//                if (HP <= HT / 2 && Dungeon.level.adjacent(enemy.pos, pos)) {
//                    if (getFurther(enemy.pos))
//                        return true
//                }
            }

            return super.act(enemyInFOV, justAlerted)
        }

        private fun heal(ch: Char) {
            sprite.showStatus(CharSprite.POSITIVE, M.L(WitchDoctor::class.java, "heal"))
            healCD = HEAL_CD
            sprite.zap(ch.pos, Callback {
                var value = Random.IntRange(8, ch.HT / 10 + 5)
                if (ch.camp == camp) value += 5 // heal allys 
                ch.recoverHP(value, this@WitchDoctor)
                spend(TIME_TO_HEAL)
                next()
            })
        }
    }

    override fun createLoot(): Item? {
        val p = Random.Float()
        return when {
            p < 0.05f -> ReagentOfHealing()
            p < 0.1f -> CeremonialDagger()
            p < 0.2f -> Generator.POTION.generate()
            else -> null
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(HEAL_CD_STR, healCD)
        bundle.put(DECAY_CD_STR, decayCD)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        healCD = bundle.getFloat(HEAL_CD_STR)
        decayCD = bundle.getFloat(DECAY_CD_STR)
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.WITCH_DOCTOR)

            val frames = TextureFilm(texture, 16, 16)
            idle = Animation(4, true)
            idle.frames(frames, 0, 0, 0, 1)

            run = Animation(5, true)
            run.frames(frames, 2, 3, 4, 5, 6)

            attack = Animation(8, false)
            attack!!.frames(frames, 7, 8, 9, 10)

            zap = attack!!.clone()

            die = Animation(8, false)
            die.frames(frames, 11, 12, 13, 14, 15)

            play(idle)
        }

        override fun zap(cell: Int) {
            turnTo(ch.pos, cell)
            play(zap)
        }

        override fun onComplete(anim: Animation) {
            if (anim === zap) idle()
            super.onComplete(anim)
        }
    }

    companion object {
        private const val DECAY_CD = 20f
        private const val HEAL_CD = 3f

        private const val TIME_TO_DECAY = 1f
        private const val TIME_TO_HEAL = 1f

        private const val HEAL_CD_STR = "heal-cd"
        private const val DECAY_CD_STR = "decay-cd"
    }
}