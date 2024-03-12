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
package com.egoal.darkestpixeldungeon.actors.mobs

import android.util.Log
import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.Assassin
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.Ability
import com.egoal.darkestpixeldungeon.effects.Surprise
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.items.rings.RingOfAccuracy
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.Random
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

abstract class Mob : Char() {
    var SLEEPING: AiState = Sleeping()
    var HUNTING: AiState = Hunting()
    var WANDERING: AiState = Wandering()
    var FLEEING: AiState = Fleeing()
    var PASSIVE: AiState = Passive()
    private var FOLLOW_HERO: AiState = FollowHero()
    private var following: Boolean = false

    var state = SLEEPING

    lateinit var spriteClass: Class<out CharSprite> // leave to subclass

    protected var target = -1

    protected var enemy: Char? = null
    protected var enemySeen: Boolean = false
    protected var alerted = false

    val isLiving get() = !properties.contains(Property.STATIC)

    private lateinit var _config: Database.MobsLine

    var Config: Database.MobsLine
        get() = _config
        protected set(value) {
            _config = value

            HT = _config.MaxHealth
            HP = HT
            SHLD = _config.Shield
            atkSkill = _config.AttackSkill
            defSkill = _config.DefendSkill

            magicalResistance = _config.Resistance[0].Magic
            elementalResistance[0] = _config.Resistance[0].Fire
            elementalResistance[1] = _config.Resistance[0].Poison
            elementalResistance[2] = _config.Resistance[0].Ice
            elementalResistance[3] = _config.Resistance[0].Light
            elementalResistance[4] = _config.Resistance[0].Shadow
            elementalResistance[5] = _config.Resistance[0].Holy

            properties.clear()
            properties.addAll(_config.MobProperties)
        }

    var abilities: ArrayList<Ability> = arrayListOf()
    private var cntAbilities_ = 0 // number of inherent abilities

    val DebugString
        get() = "$HP+$SHLD/$HT\n" +
                properties.joinToString { it.name } + "\n" +
                (0 until cntAbilities_).joinToString { abilities[it].javaClass.simpleName } + "-||-" +
                (cntAbilities_ until abilities.size).joinToString { abilities[it].javaClass.simpleName }

    init {
        name = M.L(this, "name")
        actPriority = 2 //hero gets priority over mobs.

        camp = Camp.ENEMY

        // by default, just class name.
        val con = Database.ConfigOfMob(javaClass.simpleName)
        if (con != null) Config = con
        else {
            Config = Database.DummyMobConfig.copy()
            Log.w("dpd", "missing mob config of ${javaClass.simpleName}.")
        }
    }

    fun initialize(): Mob {
        cntAbilities_ = abilities.size

        // chance to lift as an elite
        val chance = .1f + (max(0, Dungeon.depth - 5) / 10) * .05f   // 10%, 15%, 15%, 20%, 20%
        if (Dungeon.hero.challenge == Challenge.Outrage || Random.Float() < chance) {
            val cnt = Random.chances(floatArrayOf(.6f, .3f, .1f)) + 1

            abilities.addAll(randomAbilities(cnt))
            if (cntAbilities_ < abilities.size) {
                //with extra ability, becomes an elite
                properties.add(Property.ELITE)
                name = abilities[cntAbilities_].prefix() + name
            }
        }

        abilities.forEach { it.onInitialize(this) }
        abilities.forEach { it.onReady(this) }

        return this
    }

    /**
     * extra {atMost} random abilities
     */
    protected open fun randomAbilities(atMost: Int): List<Ability> {
        val cnt = min(atMost, Config.Ability.size)

        val map = HashMap<String, Float>()
        for (al in Config.Ability) map[al.Name] = al.Chance

        return List(cnt) {
            val key = Random.chances(map)
            map[key] = 0f
            key
        }.mapNotNull {
            try {
                Class.forName("com.egoal.darkestpixeldungeon.actors.mobs.abilities.$it").newInstance() as Ability
            } catch (e: ClassNotFoundException) {
                Log.d("dpd", "missing ability: $it.")
                null
            }
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        val stateTag = when (state) {
            SLEEPING -> AI_SLEEPING
            WANDERING -> AI_WANDERING
            HUNTING -> AI_HUNTING
            FLEEING -> AI_FLEEING
            PASSIVE -> AI_PASSIVE
            FOLLOW_HERO -> AI_FOLLOW_HERO
            else -> ""
        }
        if (stateTag.isNotEmpty()) bundle.put(STATE, stateTag)

        bundle.put(SEEN, enemySeen)
        bundle.put(TARGET, target)
        bundle.put(FOLLOWING, following)
        bundle.put(ABILITIES, abilities)
        bundle.put("cnt-abilities", cntAbilities_)
    }

    override fun restoreFromBundle(bundle: Bundle) {

        super.restoreFromBundle(bundle)

        when (bundle.getString(STATE)) {
            AI_SLEEPING -> state = SLEEPING
            AI_WANDERING -> state = WANDERING
            AI_HUNTING -> state = HUNTING
            AI_FLEEING -> state = FLEEING
            AI_PASSIVE -> state = PASSIVE
            AI_FOLLOW_HERO -> state = FOLLOW_HERO
        }

        enemySeen = bundle.getBoolean(SEEN)
        target = bundle.getInt(TARGET)
        following = bundle.getBoolean(FOLLOWING)
        abilities.clear()
        abilities.addAll(bundle.getCollection(ABILITIES).map { it as Ability })
        abilities.forEach { it.onReady(this) }

        cntAbilities_ = bundle.getInt("cnt-abilities")
        if (cntAbilities_ < abilities.size) {
            properties.add(Property.ELITE)
            name = abilities[cntAbilities_].prefix() + name
        }
    }

    open fun sprite(): CharSprite = spriteClass.newInstance()

    override fun act(): Boolean {
        super.act()  // update field of view

        val justAlerted = alerted
        alerted = false

        sprite.hideAlert()

        if (paralysed > 0) {
            enemySeen = false
            spend(TICK)
            return true
        }

        enemy = chooseEnemy()

        val enemyInFOV = enemy != null && enemy!!.isAlive && Level.fieldOfView[enemy!!.pos] && enemy!!.invisible <= 0

        return state.act(enemyInFOV, justAlerted)
    }

    override fun giveDamage(enemy: Char): Damage {
        // default normal damage
        val damage = Damage(Random.NormalIntRange(Config.MinDamage, Config.MaxDamage), this, enemy).type(Config.DamageType)
        if (Random.Float() < Config.CritChance) {
            damage.value = round(damage.value * Config.CritRatio).toInt()
            damage.addFeature(Damage.Feature.CRITICAL)
        }

        for (a in abilities) a.procGivenDamage(this, damage)

        return damage
    }

    override fun attackProc(dmg: Damage): Damage {
        for (a in abilities) a.onAttack(this, dmg)
        return super.attackProc(dmg)
    }

    override fun defendDamage(dmg: Damage): Damage = dmg.apply { value -= Random.NormalIntRange(Config.MinDefend, Config.MaxDefend) }

    protected open fun chooseEnemy(): Char? {
        val terror = buff(Terror::class.java)
        if (terror != null) {
            val source = findById(terror.objectid) as Char?
            if (source != null) {
                return source
            }
        }

        //find a new enemy if..
        var newEnemy = false
        //we have no enemy, or the current one is dead
        if (enemy == null || !enemy!!.isAlive || state === WANDERING)
            newEnemy = true
        else if (buff(Corruption::class.java) != null && (enemy === Dungeon.hero || enemy!!.buff(Corruption::class.java) != null))
            newEnemy = true
        else if (buff(Amok::class.java) != null && enemy === Dungeon.hero)
            newEnemy = true//We are amoked and current enemy is the hero
        //We are corrupted, and current enemy is either the hero or another
        // corrupted character.

        if (newEnemy) {

            val enemies = HashSet<Char>()

            // if amok
            if (buff(Amok::class.java) != null) {

                //try to find an enemy mob to attack first.
                for (mob in Dungeon.level.mobs)
                    if (mob !== this && Level.fieldOfView[mob.pos] && mob.camp === Char.Camp.ENEMY)
                        enemies.add(mob)
                if (enemies.size > 0) return Random.element(enemies)

                //try to find ally mobs to attack second.
                for (mob in Dungeon.level.mobs)
                    if (mob !== this && Level.fieldOfView[mob.pos] && mob.camp === Char.Camp.HERO)
                        enemies.add(mob)
                return if (enemies.size > 0)
                    Random.element(enemies)
                else
                    Dungeon.hero//if there is nothing, go for the hero
            } else {
                // try to find mobs not on its side
                // if the mob is corrupted, then it on Camp.HERO
                for (mob in Dungeon.level.mobs) {
                    if (mob.camp !== Char.Camp.NEUTRAL && mob.camp !== camp && Level.fieldOfView[mob.pos])
                        enemies.add(mob)
                }
                if (Dungeon.hero.camp !== camp) enemies.add(Dungeon.hero)

                //targetpos one at random.
                return if (enemies.size > 0) Random.element(enemies) else null
            }

        } else
            return enemy
    }

    protected fun moveSprite(from: Int, to: Int): Boolean {
        if (sprite.isVisible && (Dungeon.visible[from] || Dungeon.visible[to])) {
            sprite.move(from, to)
            return true
        } else {
            sprite.place(to)
            return true
        }
    }

    fun swapPosition(hero: Hero) {
        val curpos = pos
        moveSprite(pos, hero.pos)
        move(hero.pos)

        hero.sprite.move(pos, curpos)
        hero.move(curpos)
    }

    override fun add(buff: Buff) {
        super.add(buff)
        when (buff) {
            is Amok -> {
                if (hasSprite) sprite.showStatus(CharSprite.NEGATIVE, M.L(this, "rage"))
                state = HUNTING
            }
            is Terror -> state = FLEEING
            is Sleep -> {
                state = SLEEPING
                sprite.showSleep()
                postpone(Sleep.SWS)
            }
        }
    }

    override fun remove(buff: Buff) {
        super.remove(buff)
        if (buff is Terror) {
            sprite.showStatus(CharSprite.NEGATIVE, M.L(this, "rage"))
            state = HUNTING
        }
    }

    fun followHero() {
        following = true
        state = FOLLOW_HERO
    }

    fun unfollowHero() {
        following = false
        state = WANDERING
    }

    fun resetTarget() {
        target = -1
    }

    protected open fun canAttack(enemy: Char): Boolean = Dungeon.level.adjacent(pos, enemy.pos)

    protected open fun getCloser(target: Int): Boolean {
        if (rooted || target == pos) return false

        var step = -1

        if (Dungeon.level.adjacent(pos, target)) {

            path = null

            if (Actor.findChar(target) == null && Level.passable[target])
                step = target

        } else {

            var newPath = false
            if (path == null || path!!.isEmpty() ||
                    !Dungeon.level.adjacent(pos, path!!.first) ||
                    path!!.size > 2 * Dungeon.level.distance(pos, target))
                newPath = true
            else if (path!!.last != target) {
                //if the new targetpos is adjacent to the end of the path, adjust for that
                //rather than scrapping the whole path. Unless the path is very long,
                //in which case re-checking will likely result in a much better path
                if (Dungeon.level.adjacent(target, path!!.last)) {
                    val last = path!!.removeLast()

                    if (path!!.isEmpty()) {
                        if (Dungeon.level.adjacent(target, pos))
                            path!!.add(target)
                        else {
                            path!!.add(last)
                            path!!.add(target)
                        }
                    } else {
                        if (path!!.last == target) {
                        } else if (Dungeon.level.adjacent(target, path!!.last))
                            path!!.add(target)
                        else {
                            path!!.add(last)
                            path!!.add(target)
                        }

                    }
                } else {
                    newPath = true
                }
            }


            if (!newPath) {
                //checks the next 4 cells in the path for validity
                val lookAhead = GameMath.clamp(path!!.size - 1, 1, 4)
                for (i in 0 until lookAhead) {
                    val c = path!![i]
                    if (!Level.passable[c] || Dungeon.visible[c] && Actor.findChar(c) != null) {
                        newPath = true
                        break
                    }
                }
            }

            if (newPath)
                path = Dungeon.findPath(this, pos, target, Level.passable,
                        Level.fieldOfView)

            // if the path is too long, don't go there
            if (path == null || state === HUNTING && path!!.size > Math.max(9, 2 * Dungeon.level.distance(pos, target)))
                return false

            step = path!!.removeFirst()
        }
        if (step != -1) {
            move(step)
            return true
        } else {
            return false
        }
    }

    protected open fun getFurther(target: Int): Boolean {
        val step = Dungeon.flee(this, pos, target, Level.passable, Level.fieldOfView)
        if (step != -1) {
            move(step)
            return true
        } else {
            return false
        }
    }

    override fun updateSpriteState() {
        super.updateSpriteState()
        if (Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze::class.java) != null)
            sprite.add(CharSprite.State.PARALYSED)

        if (properties.contains(Property.ELITE)) sprite.add(CharSprite.State.ELITE_HALO)
    }

    override fun move(step: Int) {
        super.move(step)

        if (!flying) {
            Dungeon.level.mobPress(this)
        }
    }

    protected fun attackDelay(): Float = (if (Config.AttackDelay == 0f) 1f else Config.AttackDelay) / attackSpeed()

    protected open fun attackSpeed(): Float {
        var b = 1f
        if (buff(Rage::class.java) != null) b *= .5f
        return b
    }

    protected open fun doAttack(enemy: Char): Boolean {

        val visible = Dungeon.visible[pos]

        if (visible) {
            sprite.attack(enemy.pos)
        } else {
            attack(enemy)
        }

        spend(attackDelay())

        return !visible
    }

    override fun onAttackComplete() {
        attack(enemy!!)
        super.onAttackComplete()
    }

    override fun dexRoll(damage: Damage): Float {
        val attacker = damage.from as Char
        val seen = enemySeen || attacker === Dungeon.hero && !attacker.canSurpriseAttack()

        return if (seen && paralysed == 0) {
            var dex = super.dexRoll(damage)
            val penalty = Ring.getBonus(attacker, RingOfAccuracy.Accuracy::class.java)
            if (penalty != 0 && attacker === Dungeon.hero) dex *= 0.75f.pow(penalty)
            dex

        } else 0f
    }

    override fun defenseProc(dmg: Damage): Damage {
        val enemy = dmg.from as Char
        if (!enemySeen && enemy === Dungeon.hero && Dungeon.hero.canSurpriseAttack()) {
            // surprise attack!
            if (enemy.heroPerk.has(Assassin::class.java)) {
                // assassin perk
                dmg.value += dmg.value / 4
                Wound.hit(this)
            } else
                Surprise.hit(this)
        }

        // attack by a closer but not current enemy, switch
        if (this.enemy == null || enemy !== this.enemy && Dungeon.level.distance(pos, enemy.pos) < Dungeon.level.distance(pos, this.enemy!!.pos)) {
            aggro(enemy)
            target = enemy.pos // enemy set, not null
        }

        // process buff: soul mark
        buff(SoulMark::class.java)?.onDamageToken(this, dmg)

        for (a in abilities) a.onDefend(this, dmg)

        return dmg
    }

    fun surprisedBy(enemy: Char): Boolean {
        return !enemySeen && enemy === Dungeon.hero
    }

    fun aggro(ch: Char?) {
        enemy = ch
        if (state !== PASSIVE) {
            state = HUNTING
        }
    }

    override fun resistDamage(dmg: Damage): Damage {
        if (dmg.isFeatured(Damage.Feature.DEATH) && properties.contains(Char.Property.BOSS))
            dmg.value /= 4
        return super.resistDamage(dmg)
    }

    override fun takeDamage(dmg: Damage): Int {
        Terror.recover(this)

        if (state === SLEEPING)
            state = WANDERING
        if (state !== HUNTING)
            alerted = true

        return super.takeDamage(dmg)
    }

    override fun destroy() {

        super.destroy()

        Dungeon.level.mobs.remove(this)

        if (Dungeon.hero.isAlive) {
            if (camp === Char.Camp.ENEMY) {
                Statistics.EnemiesSlain = Statistics.EnemiesSlain + 1
                Badges.validateMonstersSlain()
                Statistics.QualifiedForNoKilling = false

                if (Dungeon.level.feeling === Level.Feeling.DARK) {
                    Statistics.NightHunt = Statistics.NightHunt + 1
                } else {
                    Statistics.NightHunt = 0
                }
                Badges.validateNightHunter()
            }

            Dungeon.hero.onMobDied(this)
            // give exp
            val exp = exp()
            if (exp > 0) {
                Dungeon.hero.sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "exp", exp))
                Dungeon.hero.earnExp(exp)
            }
        }
    }

    fun exp(): Int {
        val dlvl = Dungeon.hero.lvl - Config.MaxLevel
        var exp = if (dlvl < 0) Config.EXP else Config.EXP / (2 + dlvl)
        if (properties.contains(Property.ELITE)) exp = exp * 3 / 2
        return exp
    }

    override fun die(cause: Any?) {
        var justDie = true
        for (a in abilities) justDie = a.onDying(this) && justDie

        if (justDie) {
            if (Dungeon.hero.lvl <= Config.MaxLevel + 2) {
                val chance = Config.LootChance * 1.15f.pow(Dungeon.hero.wealthBonus()) *
                        (if (properties.contains(Property.ELITE)) 2f else 1f)
                if (Random.Float() < chance) {
                    createLoot()?.let { Dungeon.level.drop(it, pos).sprite.drop() }
                }
            }

            if (Dungeon.hero.isAlive && !Dungeon.visible[pos])
                GLog.i(M.L(this, "died"))

            super.die(cause)
        }
    }

    protected open fun createLoot(): Item? {
        val total = Config.Loot.sumOf { it.Chance.toDouble() }.toFloat()
        if (total <= 0f) return null

        var p = Random.Float(total)
        for (l in Config.Loot) {
            p -= l.Chance
            if (p <= 0f) return GenerateItemFromString(l.Name)
        }

        TODO()
    }

    protected fun GenerateItemFromString(name: String): Item? {
        try {
            //todo: someday i'll get rid of this
            if (name.all { it.isUpperCase() }) {
                // should be a generator
                val gen = Class.forName("com.egoal.darkestpixeldungeon.items.Generator$$name").kotlin.objectInstance as Generator.ItemGenerator?
                return gen?.generate()
            }

            // normal item instance
            val item = FindItemClassByName(name)?.newInstance()?.random()
            if (item == null) Log.w("dpd", "failed to create item for $name.")
            return item
        } catch (e: java.lang.Exception) {
            Log.w("dpd", "failed to create item for $name: " + e.toString())
        }
        return null
    }

    open fun reset(): Boolean {
        return false
    }

    open fun beckon(cell: Int) {

        notice()

        if (state !== HUNTING) {
            state = WANDERING
        }
        target = cell
    }

    open fun description(): String {
        return Messages.get(this, "desc")
    }

    open fun notice() {
        sprite.showAlert()
    }

    fun yell(str: String) {
        GLog.n("%s: \"%s\"", name, str)
    }

    //returns true when a mob sees the hero, and is currently targeting them.
    fun focusingHero(): Boolean {
        return enemySeen && target == Dungeon.hero.pos
    }

    // state machine

    interface AiState {
        fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean

        fun status(): String
    }

    protected inner class Sleeping : AiState {

        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            if (enemyInFOV && alert(enemy!!)) {
                enemySeen = true

                notice()
                state = HUNTING
                target = enemy!!.pos

                spend(TIME_TO_WAKE_UP)

            } else {

                enemySeen = false

                spend(Actor.TICK)
            }
            return true
        }

        private fun alert(ch: Char): Boolean {
            if (ch.flying || ch.buff(Catwalk::class.java) != null) return false
            if (Random.Int(distance(ch) / 2 + 1 + ch.stealth()) != 0) return false

            return true
        }

        override fun status(): String = M.L(this, "status", name)
    }

    protected open inner class Wandering : AiState {

        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            if (enemyInFOV && (justAlerted || Random.Float(distance(enemy!!) / 2f + enemy!!.stealth()) < 1f)) {

                enemySeen = true

                notice()
                state = HUNTING
                target = enemy!!.pos
            } else {

                enemySeen = false

                val oldPos = pos
                if (target != -1 && getCloser(target)) {
                    spend(1 / speed())
                    return moveSprite(oldPos, pos)
                } else {
                    target = Dungeon.level.randomDestination()
                    spend(Actor.TICK)
                }

            }
            return true
        }

        override fun status(): String {
            return Messages.get(this, "status", name)
        }
    }

    open inner class Hunting : AiState {

        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            enemySeen = enemyInFOV
            if (enemyInFOV && !isCharmedBy(enemy!!) && buff(Disarm::class.java) == null && canAttack(enemy!!)) {

                return doAttack(enemy!!)

            } else {

                if (enemyInFOV) {
                    target = enemy!!.pos
                } else if (enemy == null) {
                    state = WANDERING
                    target = Dungeon.level.randomDestination()
                    return true
                }

                val oldPos = pos
                if (target != -1 && getCloser(target)) {

                    spend(1 / speed())
                    return moveSprite(oldPos, pos)

                } else {

                    spend(Actor.TICK)
                    if (following) state = FOLLOW_HERO
                    else {
                        state = WANDERING
                        target = Dungeon.level.randomDestination()
                    }
                    return true
                }
            }
        }

        override fun status(): String = M.L(this, "status", name)
    }

    protected open inner class Fleeing : AiState {

        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            enemySeen = enemyInFOV
            //loses targetpos when 0-dist rolls a 6 or greater.
            if (enemy == null || !enemyInFOV && 1 + Random.Int(Dungeon.level
                            .distance(pos, target)) >= 6) {
                target = -1
            } else {
                target = enemy!!.pos
            }

            val oldPos = pos
            if (target != -1 && getFurther(target)) {

                spend(1 / speed())
                return moveSprite(oldPos, pos)

            } else {

                spend(Actor.TICK)
                nowhereToRun()

                return true
            }
        }

        protected open fun nowhereToRun() {}

        override fun status(): String = M.L(this, "status", name)
    }

    protected inner class Passive : AiState {

        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            enemySeen = false
            spend(Actor.TICK)
            return true
        }

        override fun status(): String = M.L(this, "status", name)
    }

    protected inner class FollowHero : AiState {

        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            if (enemyInFOV && (justAlerted || Random.Float(distance(enemy!!) / 2f + enemy!!.stealth()) < 1f)) {
                // switch to hunting
                enemySeen = true
                notice()
                state = HUNTING
                target = enemy!!.pos
            } else {
                enemySeen = false

                if (Dungeon.level.distance(Dungeon.hero.pos, pos) > 2) {
                    target = Dungeon.hero.pos
                }

                val oldPos = pos
                if (target != -1 && getCloser(target)) {
                    spend(1 / speed())
                    return moveSprite(oldPos, pos)
                } else {
                    // follow hero
                    target = Dungeon.hero.pos
                    spend(Actor.TICK)
                }
            }

            return true
        }

        override fun status(): String = M.L(this, "status", name)
    }

    companion object {
        protected const val TIME_TO_WAKE_UP = 1f

        private const val STATE = "state"
        private const val SEEN = "seen"
        private const val TARGET = "targetpos"
        private const val FOLLOWING = "following"
        private const val ABILITIES = "abilities"

        private const val AI_SLEEPING = "SLEEPING"
        private const val AI_WANDERING = "WANDERING"
        private const val AI_HUNTING = "HUNTING"
        private const val AI_FLEEING = "FLEEING"
        private const val AI_PASSIVE = "PASSIVE"
        private const val AI_FOLLOW_HERO = "FOLLOW_HERO"

        //todo: use package list reflection
        private val itempacks = listOf(
                "com.egoal.darkestpixeldungeon.items.",
                "com.egoal.darkestpixeldungeon.items.armor.",
                "com.egoal.darkestpixeldungeon.items.artifacts.",
                "com.egoal.darkestpixeldungeon.items.bags.",
                "com.egoal.darkestpixeldungeon.items.books.",
                "com.egoal.darkestpixeldungeon.items.food.",
                "com.egoal.darkestpixeldungeon.items.helmets.",
                "com.egoal.darkestpixeldungeon.items.keys.",
                "com.egoal.darkestpixeldungeon.items.potions.",
                "com.egoal.darkestpixeldungeon.items.quest.",
                "com.egoal.darkestpixeldungeon.items.rings.",
                "com.egoal.darkestpixeldungeon.items.scrolls.",
                "com.egoal.darkestpixeldungeon.items.specials.",
                "com.egoal.darkestpixeldungeon.items.unclassified.",
                "com.egoal.darkestpixeldungeon.items.wands.",
                "com.egoal.darkestpixeldungeon.items.weapon.",
                "com.egoal.darkestpixeldungeon.items.weapon.melee.",
                "com.egoal.darkestpixeldungeon.items.weapon.missiles.",
        )

        private fun FindItemClassByName(name: String): Class<out Item>? {
            return itempacks.firstNotNullOfOrNull {
                try {
                    Class.forName(it + name) as Class<out Item>
                } catch (e: ClassNotFoundException) {
                    null
                }
            }
        }
    }
}

