package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Light
import com.egoal.darkestpixeldungeon.actors.buffs.Poison
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.EnchantDefend_Venomous
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.*

class Glowworm(private var level: Int = 1) : Mob() {
    init {
        spriteClass = Sprite::class.java

        flying = true

        abilities.add(EnchantDefend_Venomous())

        setLevel(level)
        Buff.affect(this, Light::class.java).prolong(Float.MAX_VALUE) // for a whole light...
    }

    fun setLevel(lvl: Int) {
        level = lvl

        Config = Config.copy(
                MaxHealth = 5 * level,
                EXP = level / 3 + 1,
                MaxLevel = level + 2,
                DefendSkill = 3f + level,
                AttackSkill = 10f + level)
    }

    override fun giveDamage(enemy: Char): Damage =
            Damage(Random.NormalIntRange(1, level / 2), this, enemy)
                    .setAdditionalDamage(Damage.Element.Poison, Random.NormalIntRange(1, level))

    override fun defendDamage(dmg: Damage): Damage = dmg.apply {
        value -= Random.NormalIntRange(1, level)
    }

    override fun die(cause: Any?) {
        super.die(cause)

        // poison & light nearby
        GameScene.add(Blob.seed(pos, 20, ToxicGas::class.java))

        for (i in PathFinder.NEIGHBOURS8) {
            Actor.findChar(pos + i)?.let { ch ->
                if (ch.isAlive) {
                    Buff.affect(ch, Light::class.java).prolong(20f)
                    if (ch === Dungeon.hero) {
                        GLog.w(M.L(Glowworm::class.java, "light"))
                        Buff.affect(ch, Poison::class.java).set(
                                (Random.Float(1f, 3f) + level / 3f) * Poison.durationFactor(ch))
                    }
                }
            }
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STR_LEVEL, level)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        val hp = HP
        level = bundle.getInt(STR_LEVEL)
        setLevel(level)
        HP = hp
    }

    //fixme: bad design, to avoid duplicate lights
    override fun immunizedBuffs(): HashSet<Class<*>> {
        return if (buff(Light::class.java) != null) hashSetOf(Light::class.java) else hashSetOf()
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.GLOWWORM)

            val frames = TextureFilm(texture, 16, 16)

            idle = Animation(5, true)
            idle.frames(frames, 0, 1)

            run = idle.clone()

            attack = Animation(15, false)
            attack!!.frames(frames, 2, 3, 4)

            die = Animation(9, false)
            die.frames(frames, 5, 6, 7)

            play(idle)
        }

        override fun blood(): Int = 0xFF8BA077.toInt()
    }

    companion object {
        private const val STR_LEVEL = "level"
    }
}