/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.actors

import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.actors.hero.perks.ArcaneCrit
import com.egoal.darkestpixeldungeon.actors.hero.perks.WandPerception
import com.egoal.darkestpixeldungeon.actors.hero.perks.WandPiercing
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.items.rings.RingOfAccuracy
import com.egoal.darkestpixeldungeon.items.rings.RingOfSharpshooting
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.features.Door
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.*

import java.util.Arrays
import java.util.HashSet
import kotlin.math.*

abstract class Char : Actor() {
    interface IIncomingDamageProc {
        abstract fun procIncommingDamage(damage: Damage)
    }

    //
    enum class Camp {
        HERO, NEUTRAL, ENEMY
    }

    var pos = 0

    lateinit var sprite: CharSprite
    val hasSprite: Boolean
        get() = ::sprite.isInitialized

    var name = "mob"

    var HT: Int = 0    // max hp
    var HP: Int = 0
    var SHLD: Int = 0

    var atkSkill = 0f
    var defSkill = 0f

    protected var baseSpeed = 1f
    protected var path: PathFinder.Path? = null

    var paralysed = 0
    var rooted = false
    var flying = false
    var invisible = 0

    // resistances
    var magicalResistance = 0f
    var elementalResistance = FloatArray(Damage.Element.ELEMENT_COUNT)

    private val buffs = HashSet<Buff>()

    var camp: Camp = Camp.NEUTRAL

    open val isAlive: Boolean
        get() = HP > 0

    protected val properties = HashSet<Property>()

    init {
        Arrays.fill(elementalResistance, 0f)
    }

    override fun act(): Boolean {
        Dungeon.level.updateFieldOfView(this, Level.fieldOfView)
        return false
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        bundle.put(POS, pos)
        bundle.put(TAG_HP, HP)
        bundle.put(TAG_HT, HT)
        bundle.put(TAG_SHLD, SHLD)
        bundle.put(BUFFS, buffs)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        pos = bundle.getInt(POS)
        HP = bundle.getInt(TAG_HP)
        HT = bundle.getInt(TAG_HT)
        SHLD = bundle.getInt(TAG_SHLD)

        for (b in bundle.getCollection(BUFFS)) {
            if (b != null) (b as Buff).attachTo(this)
        }
    }

    open fun magicalResistance(): Float = magicalResistance

    open fun viewDistance(): Int {
        if (buff(SharpVision::class.java) != null) return seeDistance()

        var vd = when (Statistics.Clock.state) {
            Statistics.ClockTime.State.Day -> 6
            Statistics.ClockTime.State.Night -> 3
            Statistics.ClockTime.State.MidNight -> 2
        }

        if (buff(Drunk::class.java) != null) vd -= 1

        return vd
    }

    open fun seeDistance(): Int = 8

    open fun say(text: String) {
        say(text, CharSprite.DEFAULT)
    }

    fun say(text: String, color: Int) {
        sprite.showSentence(color, text)
    }

    open fun attack(enemy: Char): Boolean {
        if (!enemy.isAlive) return false

        var hit = true
        ProcessAttackDamage(giveDamage(enemy), { hit = it })

        return hit
    }

    open fun checkHit(dmg: Damage): Boolean = CheckDamageHit(dmg)

    open fun giveDamage(enemy: Char): Damage = Damage(1, this, enemy)

    open fun defendDamage(dmg: Damage): Damage = dmg

    open fun attackProc(dmg: Damage): Damage = dmg

    open fun defenseProc(dmg: Damage): Damage = dmg

    open fun takeDamage(dmg: Damage): Int {
        // time freeze
        Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze::class.java)?.let {
            it.addDelayedDamage(dmg)
            return -1 //! be negative
        }

        if (dmg.type != Damage.Type.MENTAL && dmg.from !is Hunger)
            buff(ResistAny::class.java)?.let {
                it.resist()
                return 0
            }

        // life link
        buff(LifeLink::class.java)?.let {
            val ch = Actor.findById(it.linker)
            if (ch is Char) {
                ch.takeDamage(dmg)
                ch.sprite.showStatus(0x000000, M.L(LifeLink::class.java, "transform"))
                return 0
            }
        }

        // currently, only hero suffer from mental damage
        if (!isAlive || dmg.value < 0 || (dmg.type == Damage.Type.MENTAL && this !is Hero))
            return 0

        buffs.filterIsInstance<IIncomingDamageProc>().forEach { it.procIncommingDamage(dmg) }

        // buffs shall remove when take damage
        buff(Frost::class.java)?.let { Buff.detach(this, Frost::class.java) }
        buff(MagicalSleep::class.java)?.let {
            if (it is MagicalSleep.Deep) it.damage = dmg
            Buff.detach(it)
        }

        if (dmg.from is Char && isCharmedBy(dmg.from as Char)) Buff.detach(this, Charm::class.java)

        // immunities, resistance
        if (!dmg.isFeatured(Damage.Feature.PURE)) resistDamage(dmg)

        // buffs when take damage
        buff(Paralysis::class.java)?.let {
            if (Random.Int(dmg.value) >= Random.Int(HP)) {
                Buff.detach(this, Paralysis::class.java)
                if (Dungeon.visible[pos])
                    GLog.i(M.L(Char::class.java, "out_of_paralysis", name))
            }
        }

        // deal with types
        when (dmg.type) {
            Damage.Type.NORMAL ->
                // physical
                if (SHLD >= dmg.value)
                    SHLD -= dmg.value
                else {
                    HP -= dmg.value - SHLD
                    SHLD = 0
                }
            Damage.Type.MAGICAL -> HP -= dmg.value
        }

        if (HP <= 0) {
            val ten = buff(Tenacity::class.java)
            if (ten != null) {
                HP = 1
                GameScene.flash(0xFF0000)
                ten.detach()
            } else
                HP = 0 //note: this is a important setting
        }

        // show damage value
        if (buff(Ignorant::class.java) == null) {
            if (dmg.value > 0 || dmg.from is Char) {
                var number = "${dmg.value}"
                if (dmg.isFeatured(Damage.Feature.CRITICAL)) number += "!"

                var color = 0x8c8c8c // gray
                if (dmg.type == Damage.Type.MAGICAL) color = 0x3b94ff // blue for magical damage.
                if (HP < HT / 4) color = CharSprite.NEGATIVE

                sprite.showStatus(color, number)
            }
        }

        if (!isAlive) die(dmg.from)

        return dmg.value
    }

    fun addResistances(element: Int, r: Float) {
        for (i in 0 until Damage.Element.ELEMENT_COUNT)
            if (element and (0x01 shl i) != 0)
                elementalResistance[i] = r
    }

    protected open fun resistDamage(dmg: Damage): Damage {
        if (immunizedBuffs().any { it == dmg.from.javaClass }) {
            dmg.value = 0
            return dmg
        }

        // elemental resistance
        for (of in 0 until Damage.Element.ELEMENT_COUNT)
            if (dmg.hasElement(0x01 shl of))
                dmg.value -= round(dmg.value * elementalResistance[of]).toInt()

        if (dmg.type == Damage.Type.MAGICAL)
            dmg.value -= round(dmg.value * magicalResistance()).toInt()

        if (dmg.value < 0) dmg.value = 0

        return dmg
    }

    // attack or edoge ratio
    open fun accRoll(damage: Damage): Float {
        var acc = Random.Float(atkSkill)

        if (buff(Bless::class.java) != null) acc *= 1.2f

        return acc
    }

    open fun dexRoll(damage: Damage): Float {
        var dex = Random.Float(defSkill)
        if (buff(Bless::class.java) != null) dex *= 1.2f
        if (buff(Roots::class.java) != null) dex *= 0.5f

        return dex
    }

    fun defenseVerb(): String = M.L(this, "def_verb")

    open fun speed(): Float {
        return if (buff(Cripple::class.java) == null) baseSpeed else baseSpeed * 0.5f
    }

    open fun destroy() {
        HP = 0
        Actor.remove(this)
    }

    open fun recoverHP(dhp: Int, src: Any? = null) {
        if (dhp == 0) return

        if (dhp > 0) {
            if (buff(Decayed::class.java) != null) {
                recoverHP(-dhp, src)
                return
            }

            if (HP < HT) HP = min(HT, HP + dhp)
        } else HP += dhp

        // heal to death...
        if (HP < 0) HP = 0

        if (dhp > 0) sprite.showStatus(CharSprite.POSITIVE, "+$dhp")
        else sprite.showStatus(CharSprite.NEGATIVE, "$dhp")

        if (!isAlive) die(src)
    }

    open fun die(src: Any?) {
        destroy()
        sprite.die()
    }

    override fun spend(time: Float) {
        var timeScale = 1f
        if (buff(Slow::class.java) != null) {
            timeScale *= 0.5f
            //slowed and chilled do not stack
        } else if (buff(Chill::class.java) != null) {
            timeScale *= buff(Chill::class.java)!!.speedFactor()
        }
        if (buff(Speed::class.java) != null) {
            timeScale *= 2.0f
        }

        super.spend(time / timeScale)
    }

    @Synchronized
    fun buffs(): HashSet<Buff> = HashSet(buffs)

    @Synchronized
    fun <T : Buff> buffs(c: Class<T>): HashSet<T> = buffs.filterIsInstance(c).toHashSet()

    @Synchronized
    fun <T : Buff> buff(c: Class<T>): T? = buffs.find { c.isInstance(it) } as T?

    @Synchronized
    fun isCharmedBy(ch: Char): Boolean = buffs.filterIsInstance<Charm>().any { it.objectid == ch.id() }

    open fun add(buff: Buff) {
        buffs.add(buff)
        Actor.add(buff)

        if (hasSprite)
            when (buff.type) {
                Buff.buffType.POSITIVE -> sprite.showStatus(CharSprite.POSITIVE, buff.toString())
                Buff.buffType.NEGATIVE -> sprite.showStatus(CharSprite.NEGATIVE, buff.toString())
                Buff.buffType.NEUTRAL -> sprite.showStatus(CharSprite.NEUTRAL, buff.toString())
                Buff.buffType.SILENT -> {
                }
            }
    }

    open fun remove(buff: Buff) {
        buffs.remove(buff)
        Actor.remove(buff)

    }

    fun remove(buffClass: Class<out Buff>) {
        for (buff in buffs(buffClass)) remove(buff)
    }

    override fun onRemove() {
        for (buff in buffs.toTypedArray())
            buff.detach()
    }

    open fun updateSpriteState() {
        for (buff in buffs) buff.fx(true)
    }

    open fun stealth(): Int = 0

    open fun move(step: Int) {
        var dst = step
        if (Dungeon.level.adjacent(dst, pos) && buff(Vertigo::class.java) != null) {
            sprite.interruptMotion()

            val newpos = pos + PathFinder.NEIGHBOURS8[Random.Int(8)]
            if (!(Level.passable[newpos] || Level.avoid[newpos]) || findChar(newpos) != null) return

            sprite.move(pos, newpos)
            dst = newpos
        }

        if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) {
            Door.Leave(pos, this)
        }

        pos = dst

        if (flying && Dungeon.level.map[pos] == Terrain.DOOR) Door.Enter(pos, this)

        if (this !== Dungeon.hero) sprite.visible = Dungeon.visible[pos]
    }

    fun distance(other: Char): Int = Dungeon.level.distance(pos, other.pos)

    open fun onMotionComplete() {
        //Does nothing by default
        //The main actor thread already accounts for motion,
        // so calling next() here isn't necessary (see Actor.process)
    }

    //note: called when the animation is done
    open fun onAttackComplete() {
        next()
    }

    open fun onOperateComplete() {
        next()
    }

    open fun immunizedBuffs(): HashSet<Class<*>> = EMPTY

    fun properties(): HashSet<Property> = properties

    enum class Property {
        BOSS,
        MINIBOSS,
        UNDEAD,
        DEMONIC,
        MACHINE,
        IMMOVABLE,
        PHANTOM
    }

    // used on damage attacker, avoid null usage...
    object Nobody : Char()

    companion object {
        private const val POS = "pos"
        private const val TAG_HP = "HP"
        private const val TAG_HT = "HT"
        private const val TAG_SHLD = "SHLD"
        private const val BUFFS = "buffs"

        private val EMPTY = HashSet<Class<*>>()

        // split the whole attack process
        // hit check, but considered the visibility
        fun CheckDamageHit(damage: Damage): Boolean {
            val attacker = damage.from as Char
            val defender = damage.to as Char

            if (attacker.buff(Shock::class.java) != null || defender.buff(MustDodge::class.java) != null) return false

            if (defender.buff(Unbalance::class.java) != null) return true

            // mob attack from no where, accurate
            if (attacker is Mob && !Dungeon.visible[attacker.pos]) damage.addFeature(Damage.Feature.ACCURATE)

            if (damage.isFeatured(Damage.Feature.ACCURATE)) return true

            val acc = attacker.accRoll(damage)
            val dex = defender.dexRoll(damage)

            // type bonus
            var bonus = 1f
            if (damage.type == Damage.Type.MAGICAL || damage.type == Damage.Type.MENTAL)
                bonus = 2f

            return bonus * acc >= dex
        }

        // attack process
        //note: if you want to skip the hit check process someday, just make the damage ACCURATE.
        fun ProcessAttackDamage(damage: Damage, onHit: ((Boolean) -> Unit)? = null, onKilled: (() -> Unit)? = null) {
            val attacker = damage.from as Char
            val defender = damage.to as Char
            val attackerIsHero = attacker === Dungeon.hero
            val defenderIsHero = defender === Dungeon.hero
            val visibleFight = Dungeon.visible[attacker.pos] || Dungeon.visible[defender.pos]

            val hit = CheckDamageHit(damage)
            onHit?.invoke(hit)

            if (!hit) {
                if (defenderIsHero) Dungeon.hero.onEvasion(damage)

                if (visibleFight) {
                    defender.sprite.showStatus(CharSprite.NEUTRAL, defender.defenseVerb())
                    Sample.INSTANCE.play(Assets.SND_MISS)
                }

                return
            }

            var dmg = damage

            // armor
            if (dmg.type != Damage.Type.MAGICAL && !dmg.isFeatured(Damage.Feature.PURE) &&
                    !(attackerIsHero && (attacker as Hero).rangedWeapon != null && attacker.subClass === HeroSubClass.SNIPER)) // sniper's perk
                dmg = defender.defendDamage(dmg)

            dmg = attacker.attackProc(dmg)

            // manifold shield resist
            if (dmg.type != Damage.Type.MENTAL)
                defender.buff(ResistAny::class.java)?.let {
                    it.resist()
                    dmg.value = 0
                }

            dmg = defender.defenseProc(dmg)

            // sound
            if (visibleFight && !TimekeepersHourglass.IsTimeStopped()) {
                if (dmg.type == Damage.Type.NORMAL && dmg.isFeatured(Damage.Feature.CRITICAL) && dmg.value > 0)
                    Sample.INSTANCE.play(Assets.SND_CRITICAL, 1f, 1f, 1f)
                else Sample.INSTANCE.play(Assets.SND_HIT, 1f, 1f, Random.Float(0.8f, 1.25f))
            }

            if (!defender.isAlive) return // already died in procs

            // camera shake
            if (defenderIsHero) { //  || dmg.isFeatured(Damage.Feature.CRITICAL)
                val shake = dmg.value * 4f / defender.HT
                if (shake > 1f) Camera.main.shake(GameMath.clampf(shake, 1f, 5f), 0.3f)
            }

            // take
            val value = defender.takeDamage(dmg)
            if (value < 0) {
                //fixme: value only be nagetive when time stop. this is fragile
                defender.sprite.flash() // let the player know he/she hit it.
                return
            }

            attacker.buff(FireImbue::class.java)?.proc(defender)
            attacker.buff(EarthImbue::class.java)?.proc(defender)

            if (attackerIsHero)
                Statistics.HighestDamage = max(Statistics.HighestDamage, value)

            // efx
            if (dmg.isFeatured(Damage.Feature.CRITICAL)) {
                defender.sprite.bloodBurstB(attacker.sprite.center(), value)
                defender.sprite.spriteBurst(attacker.sprite.center(), value)
                defender.sprite.flash()
            } else {
                defender.sprite.bloodBurstA(attacker.sprite.center(), value)
                defender.sprite.flash()
            }

            if (!defender.isAlive) {
                // killed
                onKilled?.invoke()

                if (visibleFight) {
                    if (defender === Dungeon.hero) {
                        Dungeon.fail(attacker.javaClass)
                        GLog.n(M.CL(Char::class.java, "kill", attacker.name))
                    } else if (attackerIsHero) {
                        (attacker as Hero).onKillChar(defender)
                    }
                }
            }
        }
    }
}
