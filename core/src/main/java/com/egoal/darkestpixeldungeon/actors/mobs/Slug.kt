package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Terror
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm
import kotlin.math.max

class Slug : Mob() {
    init {
        spriteClass = SlugSprite::class.java
    }

    override fun giveDamage(enemy: Char): Damage {
        return super.giveDamage(enemy).addElement(Damage.Element.POISON)
    }

    override fun defendDamage(dmg: Damage): Damage {
        if (dmg.type == Damage.Type.NORMAL)
            dmg.value = max(dmg.value * 3 / 5, 1)

        return super.defendDamage(dmg)
    }

    override fun immunizedBuffs() = IMMUNS

    companion object {
        private val IMMUNS = hashSetOf<Class<*>>(Bleeding::class.java, Terror::class.java)
    }
}

class SlugSprite : MobSprite() {
    init {
        texture(Assets.SLUG)

        val frames = TextureFilm(texture, 16, 12)

        idle = Animation(2, true)
        idle.frames(frames, 0, 0, 0, 1)

        run = Animation(5, true)
        run.frames(frames, 0, 1, 2)

        attack = Animation(15, false)
        attack!!.frames(frames, 3, 4, 5, 6, 7)

        die = Animation(10, false)
        die.frames(frames, 8, 9, 10, 11)

        play(idle)
    }

    override fun blood(): Int = 0x825131
}