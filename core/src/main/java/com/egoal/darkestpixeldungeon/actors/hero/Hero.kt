package com.egoal.darkestpixeldungeon.actors.hero

import android.util.Log
import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.perks.KHeroPerk
import com.egoal.darkestpixeldungeon.actors.hero.perks.Keen
import com.egoal.darkestpixeldungeon.actors.hero.perks.NightVision
import com.egoal.darkestpixeldungeon.actors.hero.perks.Optimistic
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.NPC
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.CheckedCell
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.MageArmor
import com.egoal.darkestpixeldungeon.items.armor.glyphs.*
import com.egoal.darkestpixeldungeon.items.artifacts.*
import com.egoal.darkestpixeldungeon.items.helmets.*
import com.egoal.darkestpixeldungeon.items.rings.*
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping
import com.egoal.darkestpixeldungeon.items.unclassified.Ankh
import com.egoal.darkestpixeldungeon.items.unclassified.CriticalRune
import com.egoal.darkestpixeldungeon.items.unclassified.HasteRune
import com.egoal.darkestpixeldungeon.items.unclassified.MendingRune
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.BattleGloves
import com.egoal.darkestpixeldungeon.items.weapon.melee.Flail
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.features.Chasm
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Earthroot
import com.egoal.darkestpixeldungeon.plants.Sungrass
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.ui.AttackIndicator
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.ui.StatusPane
import com.egoal.darkestpixeldungeon.utils.BArray
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndResurrect
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.*
import kotlin.math.*

// refactor may finish someday...
class Hero : Char() {
    init {
        actPriority = 0
        name = Messages.get(this, "name")

        HT = 20
        HP = HT
    }

    // properties
    var heroClass = HeroClass.ROGUE
    var subClass = HeroSubClass.NONE
    var heroPerk = HeroPerk(0)
    var kHeroPerk = KHeroPerk()

    var atkSkill = 10
    var defSkill = 5
    var STR = STARTING_STR
    var weakened = false
    var awareness = 0.1f
    var lvl = 1
    var exp = 0
    var criticalChance = 0f
    var regeneration = 0.1f

    // behaviour
    var ready = false
    var resting = false
    private var damageInterrupt = true
    var curAction: HeroAction? = null
    var lastAction: HeroAction? = null
    internal var enemy: Char? = null

    private val visibleEnemies = mutableListOf<Mob>()
    val mindVisionEnemies = mutableListOf<Mob>()

    // follower follow hero on level switch, just cache
    private val followers = mutableListOf<Char>()

    // equipments
    var rangedWeapon: MissileWeapon? = null
    val belongings = Belongings(this)

    lateinit var pressure: Pressure

    fun STR(): Int {
        var str = this.STR + if (weakened) -2 else 0
        str += RingOfMight.getBonus(this, RingOfMight.Might::class.java)
        return str
    }

    override fun viewDistance(): Int {
        var vd = super.viewDistance()
        if (kHeroPerk.has(NightVision::class.java) &&
                (Statistics.Clock.state == Statistics.ClockTime.State.Night ||
                        Statistics.Clock.state == Statistics.ClockTime.State.MidNight))
            vd += 1
        vd += belongings.helmet?.viewAmend() ?: 0

        return GameMath.clamp(vd, 1, 9)
    }

    override fun seeDistance(): Int {
        var sd = 8
        sd += belongings.helmet?.viewAmend() ?: 0
        return GameMath.clamp(sd, 1, 9)
    }

    // can, why
    fun canRead(): Pair<Boolean, String> {
        val plevel = pressure.getLevel()
        if (plevel == Pressure.Level.COLLAPSE || plevel == Pressure.Level.NERVOUS)
            return Pair(false, Messages.get(this, "nervous_to_read"))

        if (buff(Drunk::class.java) != null)
            return Pair(false, Messages.get(this, "drunk_to_read"))

        return Pair(true, "")
    }

    fun regenerateSpeed(): Float {
        val hlvl = buff(Hunger::class.java)!!.hunger()
        if (hlvl >= Hunger.STARVING) return 0f

        var reg = regeneration

        // heart
        buff(HeartOfSatan.Regeneration::class.java)?.let {
            if (it.isCursed)
                reg = if (reg > 0f) -0.025f else reg * 1.25f
            else
                reg += HT.toFloat() * 0.004f * Math.pow(1.08, it.itemLevel().toDouble()).toFloat()
        }

        // helmet
        belongings.helmet?.let {
            if (it is HeaddressRegeneration)
                reg += if (it.cursed) -0.1f else (0.05f + reg * 0.2f)
        }

        // rune
        if (buff(MendingRune.Recovery::class.java) != null) reg += 1f

        if (hlvl >= Hunger.HUNGRY) reg *= 0.5f

        return reg
    }

    //todo: refactor helmet immersion
    fun arcaneFactor(): Float {
        if (belongings.helmet is HoodApprentice) return 1.15f
        return 1f
    }

    fun wandChargeFactor(): Float {
        var factor = 1f
        belongings.helmet?.let {
            if (it is WizardHat)
                factor = if (it.cursed) 0.9f else 1.15f
        }
        return factor
    }

    fun mentalFactor(): Float {
        var factor = 1f
        belongings.helmet?.let {
            if (it is CircletEmerald)
                factor = if (it.cursed) 0.95f else 1.1f
        }
        return factor
    }

    // followers
    fun holdFollowers(level: Level) {
        followers.clear()

        level.mobs.filterTo(followers) { it.isFollower }
        level.mobs.removeAll(followers)

        // todo: MAX FOLLOWERS CONTROL

        Log.d("dpd", "${followers.size} followers held.")
    }

    fun restoreFollowers(level: Level, heropos: Int) {
        val avals = PathFinder.NEIGHBOURS8.map { it + heropos }.filter {
            !Level.solid[it] && level.findMob(it) == null
        }.shuffled()

        for (i in 0 until min(avals.size, followers.size)) {
            followers[i].pos = avals[i]
            level.mobs.add(followers[i] as Mob)
        }
        followers.clear()
    }

    fun className(): String = if (subClass == HeroSubClass.NONE) heroClass.title() else subClass.title()

    fun givenName(): String = if (name == Messages.get(this, "name")) className() else name

    // called on enter or resurrect the level
    fun live() {
        Buff.affect(this, Regeneration::class.java)
        Buff.affect(this, Hunger::class.java)
        pressure = Buff.affect(this, Pressure::class.java)
    }

    fun tier(): Int = belongings.armor?.tier ?: 0

    fun shoot(enemy: Char, weapon: MissileWeapon): Boolean {
        rangedWeapon = weapon
        val res = attack(enemy)

        Invisibility.dispel()
        rangedWeapon = null

        return res
    }

    override fun attackSkill(target: Char): Int {
        var accuracy = pressure.accuracyFactor()

        if (buff(Drunk::class.java) != null) accuracy *= 0.75f

        // weapon
        accuracy *= (rangedWeapon ?: belongings.weapon)?.accuracyFactor(Dungeon.hero, target) ?: 1f

        if (belongings.helmet is MaskOfHorror && belongings.helmet.cursed) accuracy *= 0.75f

        return (atkSkill * accuracy).toInt()
    }

    override fun defenseSkill(enemy: Char): Int {
        var bonus = RingOfEvasion.getBonus(this, RingOfEvasion.Evasion::class.java)
        var evasion = Math.pow(1.125, bonus.toDouble()).toFloat()

        if (paralysed > 0) evasion *= 0.5f

        if (heroClass == HeroClass.SORCERESS) evasion *= 0.8f

        evasion *= pressure.evasionFactor()

        if (buff(CloakOfShadows.cloakRecharge::class.java)?.enhanced() == true)
            evasion *= 1.2f

        val estr = (belongings.armor?.STRReq() ?: 10) - STR()
        if (estr > 0) {
            // heavy
            return (defSkill * evasion / Math.pow(1.5, estr.toDouble())).toInt()
        } else {
            // ligh
            bonus = if (heroClass == HeroClass.ROGUE) -estr else 0

            if (belongings.armor?.hasGlyph(Swiftness::class.java) != null)
                bonus += 5 + belongings.armor.level() * 3 / 2
            return Math.round((defSkill + bonus) * evasion)
        }

    }

    fun canSurpriseAttack(): Boolean {
        if (belongings.weapon == null || belongings.weapon !is Weapon) return true
        if (STR() < (belongings.weapon as Weapon).STRReq()) return false

        if (belongings.weapon is Flail && rangedWeapon == null) return false

        return true
    }

    override fun giveDamage(enemy: Char): Damage {
        var dmg = Damage(0, this, enemy)

        // weapon
        val bonus = RingOfForce.getBonus(this, RingOfForce.Force::class.java)

        val wep = rangedWeapon ?: belongings.weapon
        if (wep != null) {
            //fixme
            dmg = wep.giveDamage(Dungeon.hero, enemy)

            // battle gloves
            if (wep is BattleGloves && bonus != 0)
                dmg.value += RingOfForce.damageRoll(Dungeon.hero)
            else
                dmg.value += bonus
        } else {
            // bare hand
            dmg.value = if (bonus != 0) RingOfForce.damageRoll(Dungeon.hero)
            else Random.NormalIntRange(1, Math.max(STR() - 8, 1))
        }

        // helmet
        belongings.helmet?.let { belongings.helmet.procGivenDamage(dmg) }

        // critical
        buff(CriticalRune.Critical::class.java)?.let {
            dmg.value = (dmg.value * 1.4f).toInt()
            dmg.addFeature(Damage.Feature.CRITICAL)
        }
        if (!dmg.isFeatured(Damage.Feature.CRITICAL)) {
            val chance = criticalChance * Math.pow(1.15,
                    RingOfCritical.getBonus(this, RingOfCritical.Critical::class.java).toDouble()).toFloat()
            if (Random.Float() < chance) {
                dmg.value = (dmg.value * 1.5).toInt()
                dmg.addFeature(Damage.Feature.CRITICAL)
            }
        }

        // pressure
        pressure.procGivenDamage(dmg)

        buff(Drunk::class.java)?.let { Drunk.procOutcomingDamage(dmg) }

        if (dmg.value < 0) dmg.value = 0

        if (subClass == HeroSubClass.BERSERKER)
            dmg.value = Buff.affect(this, Berserk::class.java).damageFactor(dmg.value)

        buff(Fury::class.java)?.let { dmg.value = dmg.value * 3 / 2 }

        if (TimekeepersHourglass.IsTimeStopped() && canSurpriseAttack())
            dmg.addFeature(Damage.Feature.ACCURATE)

        return dmg
    }

    override fun defendDamage(dmg: Damage): Damage {
        if (dmg.type == Damage.Type.MENTAL) {
            //todo: mental defense
        } else {
            buff(CapeOfThorns.Thorns::class.java)?.let { it.proc(dmg) }

            belongings.weapon?.let { it.defendDamage(dmg) }

            var dr = 0
            belongings.armor?.let {
                dr = Random.NormalIntRange(it.DRMin(), it.DRMax())

                var estr = belongings.armor.STRReq() - STR()
                if (estr > 0) {
                    // heavy
                    dr = max(dr - 2 * estr, 0)
                }
            }

            // barkskin
            buff(Barkskin::class.java)?.let { dr += Random.NormalIntRange(0, it.level()) }

            dmg.value -= dr
        }

        if (dmg.value < 0) dmg.value = 0

        return dmg
    }

    override fun speed(): Float {
        var speed = super.speed()

        val hlvl = RingOfHaste.getBonus(this, RingOfHaste.Haste::class.java)
        if (hlvl != 0)
            speed *= Math.pow(1.2, hlvl.toDouble()).toFloat()

        belongings.armor?.let {
            if (it.hasGlyph(Swiftness::class.java))
                speed *= (1.1f + 0.01f * it.level())
            else if (it.hasGlyph(Flow::class.java) && Level.water[pos])
                speed *= (1.5f + 0.05f * it.level())
        }

        buff(HasteRune.Haste::class.java)?.let { speed *= 3f }

        val estr = if (belongings.armor != null) belongings.armor.STRReq() - STR() else 0
        if (estr > 0)
            return speed / Math.pow(1.2, estr.toDouble()).toFloat()
        else {
            if ((sprite as HeroSprite).sprint(subClass == HeroSubClass.FREERUNNER && !isStarving()))
                speed *= if (invisible > 0) 2f else 1.5f

            return speed
        }
    }

    fun isStarving(): Boolean = buff(Hunger::class.java)!!.isStarving

    fun canAttack(target: Char): Boolean {
        if (target.pos == pos) return false
        if (Dungeon.level.adjacent(pos, target.pos)) return true

        // weapon range
        var canHit = false
        belongings.weapon?.let {
            val wepRange = it.reachFactor(this)
            if (Dungeon.level.distance(pos, target.pos) <= wepRange) {
                val passable = BArray.not(Level.solid, null)
                for (mob in Dungeon.level.mobs) passable[mob.pos] = false
                PathFinder.buildDistanceMap(target.pos, passable, wepRange)
                canHit = PathFinder.distance[pos] <= wepRange
            }
        }

        return canHit
    }

    fun attackDelay(): Float {
        var speed = if (buff(Rage::class.java) == null) 1f else 0.667f

        val wep = rangedWeapon ?: belongings.weapon
        if (wep != null)
            speed *= wep.speedFactor(Dungeon.hero) //todo: fixme
        else {
            //Normally putting furor speed on unarmed attacks would be unnecessary
            //But there's going to be that one guy who gets a furor+force ring combo
            //This is for that one guy, you shall get your fists of fury!
            speed *= 0.25f + 0.75f * Math.pow(0.8,
                    RingOfFuror.getBonus(this, RingOfFuror.Furor::class.java).toDouble()).toFloat()
        }

        speed *= buff(Combo::class.java)?.speedFactor() ?: 1f

        return speed
    }

    override fun attackProc(dmg: Damage): Damage {
        (rangedWeapon ?: belongings.weapon)?.proc(dmg)

        if (dmg.isFeatured(Damage.Feature.CRITICAL) && dmg.value > 0 && Random.Int(10) == 0)
            recoverSanity(min(Random.Int(dmg.value / 6) + 1, 10).toFloat())

        // snipper perk
        if (subClass == HeroSubClass.SNIPER && rangedWeapon != null) {
            // Buff.prolong(this, SnipersMark::class.java, attackDelay() * 1.1f).`object` = (dmg.to as Char).id()
            Buff.prolong(dmg.to as Char, ViewMark::class.java, attackDelay() * 1.5f).observer = id()
        }

        return dmg
    }

    override fun defenseProc(dmg: Damage): Damage {
        buff(Earthroot.Armor::class.java)?.let { dmg.value = it.absorb(dmg.value) }
        buff(Sungrass.Health::class.java)?.absorb(dmg.value)

        belongings.armor?.let { belongings.armor.proc(dmg) }

        return dmg
    }

    override fun resistDamage(dmg: Damage): Damage {
        if (buff(RiemannianManifoldShield.Recharge::class.java)?.isCursed == true)
            return dmg

        buff(RingOfElements.Resistance::class.java)?.resist(dmg)
        if (dmg.type == Damage.Type.MAGICAL && ((belongings.armor is MageArmor) &&
                        (belongings.armor as MageArmor).enhanced))
            dmg.value = round(dmg.value * 0.9f).toInt()

        return super.resistDamage(dmg)
    }

    override fun takeDamage(dmg: Damage): Int {
        if (damageInterrupt && !(dmg.from is Hunger || dmg.from is Viscosity.DeferedDamage)) {
            interrupt()
            resting = false
        }

        buff(Drowsy::class.java)?.let {
            it.detach()
            GLog.w(Messages.get(this, "pain_resist"))
        }

        belongings.helmet?.procTakenDamage(dmg)

        if (dmg.type == Damage.Type.MENTAL)
            return takeMentalDamage(dmg)

        val tenacity = RingOfTenacity.getBonus(this, RingOfTenacity.Tenacity::class.java)
        if (tenacity != 0)
            dmg.value = ceil(dmg.value.toDouble() *
                    Math.pow(0.85, tenacity * (HT - HP).toDouble() / HT.toDouble())).toInt()

        val dmgToken = super.takeDamage(dmg)
        if (isAlive) {
            // extra mental damage
            val dmgMental = Damage(0, dmg.from, dmg.to).type(Damage.Type.MENTAL)

            if (dmg.from is Char && !Dungeon.visible[(dmg.from as Char).pos]) // attack from nowhere
                dmgMental.value += Random.Int(1, 5)
            if (dmg.isFeatured(Damage.Feature.CRITICAL))
                dmgMental.value += Random.Int(2, 6)

            if (!heroPerk.contain(HeroPerk.Perk.FEARLESS) && HP < HT / 4 && dmg.from is Mob && dmgToken > 0)
                dmgMental.value += Random.Int(1, 5)

            dmg.value = min(dmg.value, 10)
            takeMentalDamage(dmgMental)
        }

        return dmgToken
    }

    private fun takeMentalDamage(dmg: Damage): Int {
        if (dmg.value <= 0) return 0

        if (heroClass == HeroClass.WARRIOR) dmg.value += Random.Int(0, 1)
        if (!dmg.isFeatured(Damage.Feature.ACCURATE)) {
            val chance = kHeroPerk.get(Optimistic::class.java)?.resistChance() ?: 0f
            if (Random.Float() < chance) {
                dmg.value = 0
                sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "mental_resist"))
            }
        }

        // keep in mind that SAN is pressure, it increases
        val rv = pressure.upPressure(dmg.value.toFloat()).toInt()
        val WARNING = 0x0A0A0A

        if (rv > 0 && buff(Ignorant::class.java) == null)
            sprite.showStatus(WARNING, rv.toString())

        return rv
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = HashSet<Class<*>>().apply {
        for (buff in buffs()) addAll(buff.immunities)
    }

    fun recoverSanity(value: Float) {
        val r = pressure.downPressure(value)
        if (r >= 1f)
            sprite.showStatus(0xFFFFFF, r.toInt().toString())
    }

    fun recoverSanity(value: Int) {
        recoverSanity(value.toFloat())
    }

    public override fun spend(time: Float) {
        if (buff(TimekeepersHourglass.TimeFreeze::class.java)?.processTime(time) != true)
            super.spend(time)
    }

    fun busy() {
        ready = false
    }

    fun ready() {
        if (sprite.looping()) sprite.idle()
        curAction = null
        damageInterrupt = true
        ready = true

        AttackIndicator.updateState()
        GameScene.ready()
    }

    fun spendAndNext(time: Float) {
        busy()
        spend(time)
        next()
    }

    fun interrupt() {
        if (isAlive && curAction is HeroAction.Move && (curAction as HeroAction.Move).dst != pos)
            lastAction = curAction
        curAction = null
    }

    fun resume() {
        curAction = lastAction
        lastAction = null
        damageInterrupt = false
        next()
    }

    fun enemy(): Char? = enemy

    private fun checkVisibleMobs() {
        val visible = mutableListOf<Mob>()
        var newFound = false

        var target: Mob? = null
        for (mob in Dungeon.level.mobs.filter { Level.fieldOfView[it.pos] && it.hostile }) {
            visible.add(mob)
            if (!visibleEnemies.contains(mob))
                newFound = true

            if (!mindVisionEnemies.contains(mob) && QuickSlotButton.autoAim(mob) != -1)
                if (target == null) target = mob
                else if (distance(target) > distance(mob)) target = mob
        }

        target?.let {
            if (QuickSlotButton.lastTarget?.isAlive != true || !Dungeon.visible[QuickSlotButton.lastTarget!!.pos])
                QuickSlotButton.target(target)
        }

        if (newFound) {
            interrupt()
            resting = false
        }

        visibleEnemies.clear()
        visibleEnemies.addAll(visible)
    }

    fun visibleEnemies(): Int = visibleEnemies.size
    fun visibleEnemy(index: Int) = visibleEnemies[index % visibleEnemies.size]

    override fun act(): Boolean {
        super.act()

        if (paralysed > 0) {
            curAction = null
            spendAndNext(Actor.TICK)
            return false
        }

        checkVisibleMobs()

        if (curAction == null) {
            if (resting) {
                spend(TIME_TO_REST)
                next()
                return false
            }

            ready()
            return false
        } else {
            resting = false
            ready = false

            return curAction!!.act(Dungeon.hero)// fixme
        }
    }

    fun getCloser(target: Int): Boolean {
        if (target == pos) return false

        if (rooted) {
            Camera.main.shake(1f, 1f)
            return false
        }

        var step = -1
        if (Dungeon.level.adjacent(pos, target)) {
            // no need to perform path searching
            path = null

            if (Actor.findChar(target) == null) {
                if (Level.pit[target] && !flying && !Level.solid[target]) {
                    if (!Chasm.JumpConfirmed) {
                        Chasm.HeroJump(Dungeon.hero)
                        interrupt()
                    } else
                        Chasm.HeroFall(target)

                    return false
                }

                if (Level.passable[target] || Level.avoid[target])
                    step = target
            }
        } else {
            val newPath = if (path == null || path.isEmpty() || !Dungeon.level.adjacent(pos, path.first)) true
            else if (path.last != target) true
            else {
                //checks 2 cells ahead for validity.
                //Note that this is shorter than for mobs, so that mobs usually yield
                // to the hero
                val lookAhead = GameMath.clamp(path.size - 1, 0, 2)
                (0 until lookAhead).map { path.get(it) }.any {
                    !Level.passable[it] || (Dungeon.visible[it] && Actor.findChar(it) != null)
                }
            }

            if (newPath) {
                val len = Dungeon.level.length()
                val passable = BooleanArray(len) { Level.passable[it] && (Dungeon.level.visited[it] || Dungeon.level.mapped[it]) }

                path = Dungeon.findPath(this, pos, target, passable, Level.fieldOfView)
            }

            if (path == null) return false

            step = path.removeFirst()
        }

        if (step == -1) return false

        sprite.move(pos, step)
        move(step)
        spend(1 / speed())

        return true
    }

    // handle player input
    fun handle(cell: Int): Boolean {
        if (cell == -1) return false

        curAction = defineAction(cell)
        if (curAction is HeroAction.Move) lastAction = null // move call last action

        return true
    }

    private fun defineAction(cell: Int): HeroAction {
        // simple tile action
        if (Dungeon.level.map[cell] == Terrain.ALCHEMY && cell != pos) return HeroAction.Cook(cell)
        if (Dungeon.level.map[cell] == Terrain.ENCHANTING_STATION && cell != pos) return HeroAction.Enchant(cell)
        if (Dungeon.level.map[cell] == Terrain.LOCKED_DOOR || Dungeon.level.map[cell] == Terrain.LOCKED_EXIT)
            return HeroAction.Unlock(cell)

        // character there
        val ch = Actor.findChar(cell)
        if (Level.fieldOfView[cell] && ch is Mob) {
            return if (ch is NPC && !ch.hostile) HeroAction.Interact(ch) else HeroAction.Attack(ch)
        }

        // heap there
        val heap = Dungeon.level.heaps.get(cell)
        // moving to an item doesn't auto-pickup when enemies are near...
        // but only for standard heaps, chests and similar open as normal.
        if (heap != null && (visibleEnemies.size == 0 || cell == pos ||
                        (heap.type != Heap.Type.HEAP && heap.type != Heap.Type.FOR_SALE))) {
            return when (heap.type) {
                Heap.Type.HEAP -> HeroAction.PickUp(cell)
                Heap.Type.FOR_SALE ->
                    if (heap.size() == 1 && heap.peek().price() > 0) HeroAction.Buy(cell)
                    else HeroAction.PickUp(cell)
                else -> HeroAction.OpenChest(cell)
            }
        }

        // character may stand on the exit, so this shall be later after character check
        if (cell == Dungeon.level.exit && Dungeon.depth < 26) return HeroAction.Descend(cell)
        if (cell == Dungeon.level.entrance) return HeroAction.Ascend(cell)

        // default, just move there
        return HeroAction.Move(cell)
    }

    fun maxExp(): Int = 4 + lvl * 6

    fun earnExp(gained: Int) {
        exp += gained

        val percent = gained.toFloat() / maxExp().toFloat()
        buff(EtherealChains.chainsRecharge::class.java)?.gainExp(percent)
        buff(HornOfPlenty.hornRecharge::class.java)?.gainCharge(percent)

        if (subClass == HeroSubClass.BERSERKER) Buff.affect(this, Berserk::class.java).recover(percent)

        var upgraded = false
        while (exp >= maxExp()) {
            exp -= maxExp()
            if (lvl < MAX_LEVEL) {
                upgraded = true
                heroClass.upgradeHero(this)
            } else {
                Buff.prolong(this, Bless::class.java, 30f)
                exp = 0

                GLog.p(Messages.get(this, "level_cap"))
                Sample.INSTANCE.play(Assets.SND_LEVELUP)
            }
        }

        if (upgraded) {

            GLog.p(Messages.get(this, "new_level"), lvl)
            sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "level_up"))
            Sample.INSTANCE.play(Assets.SND_LEVELUP)

            Badges.validateLevelReached()
        }
    }

    internal fun updateAwareness() {
        val w = kHeroPerk.get(Keen::class.java)?.baseAwareness() ?: 0.9f
        awareness = (1.0 - Math.pow(w.toDouble(), (1 + Math.min(lvl, 9)).toDouble() * 0.5)).toFloat()
    }

    override fun add(buff: Buff) {
        if (immunizedBuffs().any { buff.javaClass == it }) return

        super.add(buff)

        sprite?.let {
            buff.heroMessage()?.let { GLog.w(it) }

            if (buff is Paralysis || buff is Vertigo) interrupt()
        }

        BuffIndicator.refreshHero()
    }

    override fun remove(buff: Buff) {
        super.remove(buff)
        BuffIndicator.refreshHero()
    }

    override fun stealth(): Int {
        var stealth = super.stealth()

        stealth += RingOfEvasion.getBonus(this, RingOfEvasion.Evasion::class.java)

        if (belongings.armor?.hasGlyph(Obfuscation::class.java) == true)
            stealth += belongings.armor.level()

        return stealth
    }

    override fun die(src: Any?) {
        curAction = null

        var ankh: Ankh? = null
        for (item in belongings)
            if (item is Ankh && (ankh == null || item.isBlessed))
                ankh = item

        if (ankh?.isBlessed == true) {
            HP = HT / 4
            //ensures that you'll get to act first in almost any case, to prevent 
            // reviving and then instantly dieing again.
            Buff.detach(this, Paralysis::class.java)
            spend(-cooldown())

            Flare(8, 32f).color(0xFFFF66, true).show(sprite, 2f)
            CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3)

            ankh.detach(belongings.backpack)

            Sample.INSTANCE.play(Assets.SND_TELEPORT)
            GLog.w(Messages.get(this, "revive"))
            Statistics.AnkhsUsed += 1

            GhostHero.Instance()?.sayAnhk()
            return
        }

        Actor.fixTime()
        super.die(src)

        if (ankh == null) ReallyDie(src)
        else {
            Dungeon.deleteGame(Dungeon.hero.heroClass, false, true)
            GameScene.show(WndResurrect(ankh, src))
        }
    }

    override fun isAlive(): Boolean {
        if (subClass == HeroSubClass.BERSERKER)
            if (buff(Berserk::class.java)?.berserking() == true) return true

        return super.isAlive()
    }

    fun rest(full: Boolean) {
        spendAndNext(TIME_TO_REST)
        if (!full)
            sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "wait"))

        resting = full
    }

    override fun move(step: Int) {
        super.move(step)

        // step sfx 
        if (!flying) {
            if (Level.water[pos]) Sample.INSTANCE.play(Assets.SND_WATER, 1f, 1f, Random.Float(0.8f, 1.25f))
            else Sample.INSTANCE.play(Assets.SND_STEP)

            Dungeon.level.press(pos, this)
        }
    }

    fun search(intentional: Boolean): Boolean {
        var smthFound = false

        val positive = 0
        val negative = 0

        var level = if (intentional) 2 * awareness - awareness * awareness else awareness

        var distance = 1 + positive + negative
        if (distance <= 0) {
            level /= (2 - distance).toFloat()
            distance = 1
        }

        val pt = Dungeon.level.cellToPoint(pos)
        val ax = max(pt.x - distance, 0)
        val bx = min(pt.x + distance, Dungeon.level.width() - 1)
        val ay = max(pt.y - distance, 0)
        val by = min(pt.y + distance, Dungeon.level.height() - 1)

        // cursed talisman of foresight makes unintentionally finding things impossible
        val foresight = buff(TalismanOfForesight.Foresight::class.java)
        if (foresight?.isCursed == true)
            level = -1f

        for (y in ay..by) {
            for (x in ax..bx) {
                val p = Dungeon.level.xy2cell(x, y)
                if (Dungeon.visible[p]) {
                    if (intentional) sprite.parent.addToBack(CheckedCell(p))

                    if (Level.secret[p] && (intentional || Random.Float() < level)) {
                        GameScene.discoverTile(p, Dungeon.level.map[p])
                        Dungeon.level.discover(p)

                        ScrollOfMagicMapping.discover(p)

                        smthFound = true
                        if (foresight != null && !foresight.isCursed) foresight.charge()
                    }
                }
            }
        }

        if (intentional) {
            sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "search"))
            sprite.operate(pos)

            if (foresight?.isCursed == true) {
                GLog.n(Messages.get(this, "search_distracted"))
                spendAndNext(TIME_TO_SEARCH * 3)
            } else spendAndNext(TIME_TO_SEARCH)
        }

        if (smthFound) {
            GLog.w(Messages.get(this, "noticed_smth"))
            Sample.INSTANCE.play(Assets.SND_SECRET)
            interrupt()
        }

        return smthFound
    }

    fun resurrect(resetLevel: Int) {
        HP = HT
        Dungeon.gold = 0
        exp = 0

        belongings.resurrect(resetLevel)

        live()
    }

    override fun next() {
        if (isAlive) super.next()
    }

    fun onMobDied(mob: Mob) {
        belongings.getItem(UrnOfShadow::class.java)?.collectSoul(mob)

        if (mob.properties().contains(Property.BOSS)) GhostHero.Instance()?.sayBossBeaten()
    }

    // called when killed a char by attack
    fun onKillChar(ch: Char) {
        if (ch.properties().contains(Property.PHANTOM)) return

        // may recover sanity
        if (ch.properties().contains(Property.BOSS)) recoverSanity(Random.Float(8f, 15f))
        else if (ch is Mob && ch.maxLvl + 2 >= lvl) {
            val x = pressure.pressure / Pressure.MAX_PRESSURE
            val px = if (x < 0.5f) 0.1f else (0.5f - 0.4f / (1f + exp(10f * (x - 0.5f)) / 10f))
            val y = 1f - HP.toFloat() / HT.toFloat()
            val py = if (y < 0.5f) 1f else (1f + 3f * (y - 0.5f) * (y - 0.5f))

            if (Random.Float() < px * py) recoverSanity(Random.Float(1f, 6f))
        }

        if (belongings.helmet is MaskOfMadness) (belongings.helmet as MaskOfMadness).onEnemySlayed(ch)

        buff(BloodSuck::class.java)?.onEnemySlayed(ch)
        buff(ChaliceOfBlood.Store::class.java)?.onEnemySlayed(ch)

        GLog.i(Messages.capitalize(Messages.get(Char::class.java, "defeat", ch.name)))
    }


    // animation callbacks
    override fun onMotionComplete() {
        Dungeon.observe()
        search(false)
    }

    override fun onAttackComplete() {
        AttackIndicator.target(enemy)

        val hit = attack(enemy)

        if (subClass == HeroSubClass.GLADIATOR) {
            if (hit) Buff.affect(this, Combo::class.java).hit(enemy!!)
            else buff(Combo::class.java)?.miss()
        }

        curAction = null

        super.onAttackComplete()
    }

    override fun onOperateComplete() {
        // may be polymorphic
        if (curAction is HeroAction.Unlock) {
            val doorCell = curAction!!.dst
            val door = Dungeon.level.map[doorCell]
            if (door == Terrain.LOCKED_DOOR) {
                belongings.ironKeys[Dungeon.depth]--
                Level.set(doorCell, Terrain.DOOR)
            } else run {
                belongings.specialKeys[Dungeon.depth]--
                Level.set(doorCell, Terrain.UNLOCKED_EXIT)
            }
            StatusPane.needsKeyUpdate = true

            GameScene.updateMap(doorCell)
        } else if (curAction is HeroAction.OpenChest) {
            Dungeon.level.heaps.get(curAction!!.dst)!!.let {
                when (it.type) {
                    Heap.Type.SKELETON, Heap.Type.REMAINS -> Sample.INSTANCE.play(Assets.SND_BONES)
                    Heap.Type.LOCKED_CHEST, Heap.Type.CRYSTAL_CHEST -> belongings.specialKeys[Dungeon.depth]--
                }
                StatusPane.needsKeyUpdate = true
                it.open(Dungeon.hero) // fixme
            }
        }

        curAction = null
        super.onOperateComplete()
    }

    interface Doom {
        fun onDeath()
    }

    companion object {
        const val MAX_LEVEL = 30
        const val STARTING_STR = 10

        const val TIME_TO_REST = 1f
        const val TIME_TO_SEARCH = 2f

        private const val MAX_FOLLOWERS = 3

        fun Preview(info: GamesInProgress.Info, bundle: Bundle) {
            info.level = bundle.getInt(LEVEL)
        }

        fun ReallyDie(src: Any?) {
            val len = Dungeon.level.length()
            for (i in 0 until len) {
                if (Level.discoverable[i]) {
                    Dungeon.level.visited[i] = true

                    val terr = Dungeon.level.map[i]
                    if ((Terrain.flags[terr] and Terrain.SECRET) != 0) Dungeon.level.discover(i)
                }
            }

            Bones.leave()

            Dungeon.observe()

            Dungeon.hero.belongings.identify()
            val pos = Dungeon.hero.pos
            val avals = PathFinder.NEIGHBOURS8.map { it + pos }.filter {
                (Level.passable[it] || Level.avoid[it]) && Dungeon.level.heaps.get(it) == null
            }.shuffled()

            val items = mutableListOf<Item>()
            items.addAll(Dungeon.hero.belongings)
            for (cell in avals) {
                if (items.isEmpty()) break

                val item = Random.element(items)
                Dungeon.level.drop(item, cell).sprite.drop(pos)
                items.remove(item)
            }

            GameScene.gameOver()

            if (src is Doom) src.onDeath()

            Dungeon.deleteGame(Dungeon.hero.heroClass, true, true)
        }

        //
        private const val ATTACK = "attackSkill"
        private const val DEFENSE = "defenseSkill"
        private const val STRENGTH = "STR"
        private const val LEVEL = "lvl"
        private const val EXPERIENCE = "exp"
        private const val CRITICAL = "critical"
        private const val REGENERATION = "regeneration"
        private const val RESISTANCE_MAGICAL = "resistance_magical"
        private const val RESISTANCE_NORMAL = "resistance_normal"
    }

    // store
    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        heroClass.storeInBundle(bundle)
        subClass.storeInBundle(bundle)
        heroPerk.storeInBundle(bundle)
        kHeroPerk.storeInBundle(bundle)

        bundle.put(ATTACK, atkSkill)
        bundle.put(DEFENSE, defSkill)

        bundle.put(STRENGTH, STR)

        bundle.put(LEVEL, lvl)
        bundle.put(EXPERIENCE, exp)

        bundle.put(CRITICAL, criticalChance)
        bundle.put(REGENERATION, regeneration)

        bundle.put(RESISTANCE_MAGICAL, resistanceMagical)
        bundle.put(RESISTANCE_NORMAL, resistanceNormal)

        belongings.storeInBundle(bundle)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        heroClass = HeroClass.RestoreFromBundle(bundle)
        subClass = HeroSubClass.RestoreFromBundle(bundle)
        heroPerk = HeroPerk.restoreFromBundle(bundle)
        kHeroPerk.restoreFromBundle(bundle)

        atkSkill = bundle.getInt(ATTACK)
        defSkill = bundle.getInt(DEFENSE)

        STR = bundle.getInt(STRENGTH)
        updateAwareness()

        lvl = bundle.getInt(LEVEL)
        exp = bundle.getInt(EXPERIENCE)

        criticalChance = bundle.getFloat(CRITICAL)
        regeneration = bundle.getFloat(REGENERATION)

        resistanceMagical = bundle.getFloatArray(RESISTANCE_MAGICAL)
        resistanceNormal = bundle.getFloatArray(RESISTANCE_NORMAL)

        belongings.restoreFromBundle(bundle)

        val pre = buff(Pressure::class.java)
        if (pre != null) pressure = pre
    }
}