package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Random

class Glowworm(private var level: Int = 1) : Mob() {
    init {
        spriteClass = Sprite::class.java

        flying = true

        addResistances(Damage.Element.FIRE, 1.25f)
        addResistances(Damage.Element.POISON, 2f)
        addResistances(Damage.Element.ICE, 0.75f)

        setLevel(level)
    }

    fun setLevel(lvl: Int) {
        level = lvl

        HT = 5 * level
        HP = HT
        EXP = level / 3 + 1
        maxLvl = level + 2

        defenseSkill = 3 + level
    }

    override fun attackSkill(target: Char): Int = 10 + level

    override fun giveDamage(enemy: Char): Damage = Damage(Random.NormalIntRange(1 + level / 2, 2 + level), this, enemy)

    override fun defendDamage(dmg: Damage): Damage = dmg.apply {
        value -= Random.NormalIntRange(0, level)
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.GLOWWORM)

            val frames = TextureFilm(texture, 16, 16)

            idle = Animation(5, true)
            idle.frames(frames, 0, 1)

            run = idle.clone()

            attack = Animation(15, false)
            attack.frames(frames, 2, 3, 4)

            die = Animation(9, false)
            die.frames(frames, 5, 6, 7)

            play(idle)
        }

        override fun blood(): Int = 0xFF8BA077.toInt()
    }
}