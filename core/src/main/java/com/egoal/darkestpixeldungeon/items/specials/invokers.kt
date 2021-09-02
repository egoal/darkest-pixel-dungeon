package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.BlastParticle
import com.egoal.darkestpixeldungeon.effects.particles.SmokeParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

abstract class Invoker(val positive: Boolean, private val maxlevel: Int) : Bundlable {
    protected var level = 1

    val IsUpgradable get() = level < maxlevel
    protected open val TimeToInvoke get() = 1f
    open val InvokeCD get() = 20

    fun status(): String {
        return if (level > 1) M.L(this, "name") + "+${level - 1}"
        else M.L(this, "name")
    }

    fun desc(): String = M.L(this, "desc")

    fun color(): Int = if (positive) 0xCC5252 else 0x000026

    abstract fun invoke(hero: Hero, astrolabe: Astrolabe)

    protected fun spendAndOperate(hero: Hero) {
        hero.spend(TimeToInvoke)
        hero.busy()
        hero.sprite.operate(hero.pos)
    }

    open fun upgrade() {
        level++
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put("level", level)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        level = bundle.getInt("level")
    }
}

abstract class DirectInvoker(positive: Boolean, maxlevel: Int = 1) : Invoker(positive, maxlevel) {
    override fun invoke(hero: Hero, astrolabe: Astrolabe) {
        onInvoked(hero, astrolabe)
        spendAndOperate(hero)
    }

    protected abstract fun onInvoked(hero: Hero, astrolabe: Astrolabe)
}

abstract class SelectInvoker(positive: Boolean, maxlevel: Int = 1) : Invoker(positive, maxlevel), CellSelector.Listener {
    protected lateinit var hero: Hero
    protected lateinit var astrolabe: Astrolabe

    override fun invoke(hero: Hero, astrolabe: Astrolabe) {
        this.hero = hero
        this.astrolabe = astrolabe
        GameScene.selectCell(this)
    }

    override fun onSelect(cell: Int?) {
        if (cell != null) {
            val shot = Ballistica(hero.pos, cell, Ballistica.MAGIC_BOLT)
            val c = Actor.findChar(shot.collisionPos)
            onInvoked(hero, astrolabe, c)

            spendAndOperate(hero)
        }
    }

    override fun prompt(): String = M.L(this, "prompt")

    protected abstract fun onInvoked(hero: Hero, astrolabe: Astrolabe, char: Char?)

    protected fun isOther(c: Char?) = c != null && c != hero
}

// positive
class blessed_grant : DirectInvoker(true) {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        astrolabe.doUpgrade()
    }
}

class foresight : DirectInvoker(true, maxlevel = 3) {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        Buff.prolong(hero, MustDodge::class.java, 2f + 1.5f * level)
    }
}

class purgation : SelectInvoker(true, maxlevel = 3) {
    override val InvokeCD: Int
        get() = super.InvokeCD - (level - 1) * 5

    override fun onInvoked(hero: Hero, astrolabe: Astrolabe, char: Char?) {
        if (isOther(char)) {
            val value = ((char!!.HT - char.HP) * .75f).toInt() + level
            char.takeDamage(Damage(value, hero, char).addFeature(Damage.Feature.PURE or Damage.Feature.ACCURATE))
        } else hero.sayShort(HeroLines.I_CANT)
    }
}

class life_link : SelectInvoker(true) {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe, char: Char?) {
        if (isOther(char))
            Buff.prolong(hero, LifeLink::class.java, 3f).linker = char!!.id()
    }
}

class extremely_lucky : DirectInvoker(true, maxlevel = 3) {
    override val InvokeCD: Int
        get() = super.InvokeCD - (level - 1) * 2

    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        val heal = (hero.HT - hero.HP) / 10 * level + 1
        hero.recoverHP(heal, astrolabe)

        astrolabe.blockNextNegative = true
    }

}

class pardon : SelectInvoker(true, maxlevel = 3) {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe, char: Char?) {
        if (isOther(char)) {
            char!!.recoverHP(char.HP / 4 + 1)
            Buff.prolong(char, Vulnerable::class.java, Vulnerable.DURATION).ratio = 1.5f + 0.5f * level
        } else hero.sayShort(HeroLines.I_CANT)
    }
}

class faith : DirectInvoker(true, maxlevel = 5) {
    override val InvokeCD: Int
        get() = super.InvokeCD / 2 - level

    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        hero.recoverSanity(Random.Int(level, 4 + level * 2))
    }
}

class overload : SelectInvoker(true, maxlevel = 5) {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe, char: Char?) {
        if (isOther((char))) {
            val cost = min(hero.HT / 10, hero.HP - 1)
            val dmg = cost * 2.5f

            char!!.takeDamage(Damage(round(dmg * (0.8f + level * 0.2f)).toInt(), hero, char).type(Damage.Type.MAGICAL))
            hero.takeDamage(Damage(round(cost * (1.1f - level * 0.1f)).toInt(), astrolabe, hero)
                    .addFeature(Damage.Feature.PURE or Damage.Feature.ACCURATE))
        } else hero.sayShort(HeroLines.I_CANT)
    }
}

class guide : SelectInvoker(true) {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe, char: Char?) {
        if (isOther((char))) {
            val shot = Ballistica(hero.pos, char!!.pos, Ballistica.MAGIC_BOLT)
            if (shot.path.size > shot.dist + 1)
                WandOfBlastWave.throwChar(char, Ballistica(char.pos, shot.path[shot.dist + 1], Ballistica.MAGIC_BOLT), 3)
            Buff.prolong(char, Vertigo::class.java, 8f)
        } else hero.sayShort(HeroLines.I_CANT)
    }
}

class prophesy : DirectInvoker(true, maxlevel = 3) {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        for (i in PathFinder.NEIGHBOURS8) {
            val ch = Actor.findChar(hero.pos + i)
            if (ch != null) {
                Buff.prolong(ch, Paralysis::class.java, 2f + 1.5f * level)
                ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12)
            }
        }
    }
}

class sun_strike : SelectInvoker(true, maxlevel = 5) {

    override val InvokeCD: Int
        get() = super.InvokeCD - (level - 1) * 3

    override val TimeToInvoke: Float
        get() = 3f

    override fun onSelect(cell: Int?) {
        if (cell != null) {
            if (Dungeon.level.visited[cell] || Dungeon.level.mapped[cell]) {
                val b = Buff.prolong(hero, buff::class.java, 2f)
                b.targetpos = cell
                b.level = level

                spendAndOperate(hero)
            } else
                GLog.w(M.L(SelectInvoker::class.java, "not_select_target"))
        }
    }

    override fun onInvoked(hero: Hero, astrolabe: Astrolabe, char: Char?) {
        //* do nothing
    }

    class buff : FlavourBuff() {
        var targetpos = 0
        var level = 1

        override fun act(): Boolean {
            // cast! like bomb...
            Sample.INSTANCE.play(Assets.SND_BLAST)
            if (Dungeon.visible[targetpos]) {
                CellEmitter.center(targetpos).burst(BlastParticle.FACTORY, 50)
            }

            var terrainAffected = false
            val enemies = ArrayList<Char>()
            for (n in PathFinder.NEIGHBOURS9) {
                val c = targetpos + n
                if (c >= 0 && c < Dungeon.level.length()) {
                    if (Dungeon.visible[c]) {
                        CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4 + level * 4)
                    }

                    if (Level.flamable[c]) {
                        Dungeon.level.destroy(c)
                        GameScene.updateMap(c)
                        terrainAffected = true
                    }

                    //destroys items / triggers bombs caught in the blast.
                    val heap = Dungeon.level.heaps.get(c)
                    heap?.explode()

                    val ch = Actor.findChar(c)
                    if (ch != null && ch !== Dungeon.hero) {
                        enemies.add(ch)
                    }
                }
            }

            if (enemies.isNotEmpty()) {
                var totalDamage = 0
                for (ch in enemies) if (ch.HT > totalDamage) totalDamage = ch.HT
                totalDamage = max(30 + level * 20, totalDamage)

                val dmg = totalDamage / enemies.size
                for (ch in enemies) {
                    val d = Damage(Random.IntRange(dmg * 7 / 10, dmg * 12 / 10), Item.curUser, ch).addFeature(Damage.Feature.DEATH)
                    //^ cannot be pure, which will kill boss directly.
                    if (ch.pos == targetpos) d.value += d.value / 4
                    ch.defendDamage(d)
                    ch.takeDamage(d)
                }
            }

            if (terrainAffected)
                Dungeon.observe()

            return super.act()
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(TARGET, targetpos)
            bundle.put("level", level)

        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            targetpos = bundle.getInt(TARGET)
            level = bundle.getInt("level")
        }

        companion object {
            private const val TARGET = "targetpos"
        }
    }
}

// negative
abstract class NegativeInvoker : DirectInvoker(false)

class punish : NegativeInvoker() {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        hero.takeDamage(Damage(Random.NormalIntRange(hero.HP / 10, hero.HP / 4), astrolabe, hero)
                .type(Damage.Type.MAGICAL).addFeature(Damage.Feature.ACCURATE))
    }
}

class vain : NegativeInvoker() {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        Buff.prolong(hero, Vertigo::class.java, 5f)
        Buff.prolong(hero, Weakness::class.java, 5f)
    }
}

class feedback : NegativeInvoker() {
    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        hero.takeDamage(Damage(Random.Int(1, 10), astrolabe, hero).type(Damage.Type.MENTAL)
                .addFeature(Damage.Feature.ACCURATE))
    }
}

class imprison : NegativeInvoker() {
    override val InvokeCD: Int
        get() = super.InvokeCD * 2

    override fun onInvoked(hero: Hero, astrolabe: Astrolabe) {
        Buff.prolong(hero, Roots::class.java, 3f)
    }
}