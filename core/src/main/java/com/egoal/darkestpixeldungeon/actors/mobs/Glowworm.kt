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
import com.egoal.darkestpixeldungeon.actors.buffs.Venom
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.unclassified.PoisonPowder
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Boomerang
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.TextureFilm
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.HashSet

class Glowworm(private var level: Int = 1) : Mob() {
    init {
        spriteClass = Sprite::class.java

        flying = true

        addResistances(Damage.Element.FIRE, 0.2f)
        addResistances(Damage.Element.POISON, 0.5f)
        addResistances(Damage.Element.ICE, -0.2f)

        setLevel(level)
        Buff.affect(this, Light::class.java).prolong(Float.MAX_VALUE) // for a whole light...

        loot = PoisonPowder()
        lootChance = 0.2f
    }

    fun setLevel(lvl: Int) {
        level = lvl

        HT = 5 * level
        HP = HT
        EXP = level / 3 + 1
        maxLvl = level + 2

        defSkill = 3f + level
        atkSkill = 10f + level
    }

    override fun giveDamage(enemy: Char): Damage =
            Damage(Random.NormalIntRange(1 + level / 2, 2 + level), this, enemy).addElement(Damage.Element.POISON)

    override fun defendDamage(dmg: Damage): Damage = dmg.apply {
        value -= Random.NormalIntRange(1, level)
    }

    override fun defenseProc(dmg: Damage): Damage {
        if (dmg.from is Hero) {
            val hero = dmg.from as Hero
            if (Dungeon.level.adjacent(hero.pos, pos) && Random.Int(4) == 0) {
                val weapon = hero.belongings.weapon
                if (weapon is MeleeWeapon && weapon.enchantment == null) {
                    weapon.enchant(Venomous::class.java, 8f)
                }
            }
        }

        return super.defenseProc(dmg)
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