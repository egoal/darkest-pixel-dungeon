package com.egoal.darkestpixeldungeon.actors.hero

import android.util.Log
import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.SacrificialFire
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.perks.*
import com.egoal.darkestpixeldungeon.actors.mobs.DarkSpirit
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.NPC
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.CheckedCell
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.armor.MageArmor
import com.egoal.darkestpixeldungeon.items.armor.glyphs.*
import com.egoal.darkestpixeldungeon.items.artifacts.*
import com.egoal.darkestpixeldungeon.items.helmets.*
import com.egoal.darkestpixeldungeon.items.rings.*
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping
import com.egoal.darkestpixeldungeon.items.unclassified.Ankh
import com.egoal.darkestpixeldungeon.items.unclassified.CriticalRune
import com.egoal.darkestpixeldungeon.items.unclassified.HasteRune
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.*
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.features.Chasm
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Earthroot
import com.egoal.darkestpixeldungeon.plants.Sungrass
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.ui.*
import com.egoal.darkestpixeldungeon.utils.BArray
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndGainNewPerk
import com.egoal.darkestpixeldungeon.windows.WndMasterSubclass
import com.egoal.darkestpixeldungeon.windows.WndResurrect
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.*
import kotlin.math.*

class Hero : Char() {
    init {
        actPriority = 0
        name = Messages.get(this, "name")

        HT = 20
        HP = HT

        camp = Camp.HERO
    }

    var userName = M.L(this, "username")

    // properties
    var heroClass = HeroClass.ROGUE
    var subClass = HeroSubClass.NONE
    var heroPerk = HeroPerk()

    var STR = STARTING_STR
    var weakened = false
    var awareness = 0.1f
    var lvl = 1
    var exp = 0
    var criticalChance = 0f
    var regeneration = 0.1f

    var reservedPerks = 0
    val spawnedPerks = ArrayList<Perk>()

    private var perkGained_ = 0
    var perkGained: Int
        get() = perkGained_
        set(value) {
            perkGained_ = value
            Badges.validateGainPerk()
        }
    var pohDrunk = 0

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
    var challenge: Challenge? = null

    fun STR(): Int {
        var str = this.STR + if (weakened) -2 else 0
        str += Ring.getBonus(this, RingOfMight.Might::class.java)
        return str
    }

    override fun magicalResistance(): Float {
        var mr = super.magicalResistance()
        // mr += belongings.armor?.MRES() ?: 0f // linearly
        if (belongings.armor != null) {
            mr = GameMath.ProbabilityPlus(mr, belongings.armor!!.MRES())
            if (belongings.armor!!.hasGlyph(AntiMagic::class.java))
                mr = GameMath.ProbabilityPlus(mr, 0.25f)
        }
        heroPerk.get(ExtraMagicalResistance::class.java)?.let {
            mr = GameMath.ProbabilityPlus(mr, it.ratio())
        }

        return mr
    }

    override fun viewDistance(): Int {
        var vd = super.viewDistance()
        if (heroPerk.has(NightVision::class.java) &&
                (Statistics.Clock.state == Statistics.ClockTime.State.Night ||
                        Statistics.Clock.state == Statistics.ClockTime.State.MidNight))
            vd += 1
        vd += belongings.helmet?.viewAmend() ?: 0
        vd += buff(GoddessRadiance.Recharge::class.java)?.viewAmend() ?: 0


        return GameMath.clamp(vd, 1, 9)
    }

    override fun seeDistance(): Int {
        var sd = 8
        sd += belongings.helmet?.viewAmend() ?: 0
        return GameMath.clamp(sd, 1, 9)
    }

    // can, why
    fun canRead(): Pair<Boolean, String> {
        val plevel = pressure.level
        if (plevel == Pressure.Level.COLLAPSE || plevel == Pressure.Level.NERVOUS)
            return Pair(false, Messages.get(this, "nervous_to_read"))

        if (buff(Drunk::class.java) != null)
            return Pair(false, Messages.get(this, "drunk_to_read"))

        return Pair(true, "")
    }

    //todo: refactor someday
    fun isUsingPolearm() = rangedWeapon == null && ((belongings.weapon as Weapon?)?.RCH
            ?: 1) > 1 && belongings.weapon !is Whip

    fun regenerateSpeed(): Float {
        if (challenge == Challenge.Immortality) return 0f

        val hlvl = buff(Hunger::class.java)!!.hunger()
        if (hlvl >= Hunger.STARVING) return 0f

        if (buff(Berserk::class.java)?.berserking() == true) return 0f

        var reg = regeneration

        val glp = belongings.armor?.glyph
        if (glp is Healing)
            regeneration += glp.speed(belongings.armor!!)

        // heart
        buff(HeartOfSatan.Regeneration::class.java)?.let {
            if (it.isCursed)
                reg = if (reg >= 0f) -0.025f else reg * 1.5f
            else
                reg += HT.toFloat() * 0.003f * 1.18f.pow(it.itemLevel())
        }

        // ring
        val health = Ring.getBonus(this, RingOfHealth.Health::class.java)
        if (health < 0) reg += 0.05f * health
        else reg += 0.0333f * health

        // helmet
        belongings.helmet?.let {
            if (it is HeaddressRegeneration)
                reg += if (it.cursed) -0.1f else (0.05f + reg * 0.2f)
        }

        if (hlvl >= Hunger.HUNGRY) reg *= 0.5f

        return reg
    }

    //todo: refactor helmet immersion
    fun arcaneFactor(): Float {
        var factor = heroPerk.get(WandArcane::class.java)?.factor() ?: 1f
        if (belongings.helmet is HoodApprentice) factor *= 1.15f

        return factor
    }

    fun wandChargeFactor(): Float {
        var factor = pressure.chargeFactor()

        factor *= heroPerk.get(WandCharger::class.java)?.factor() ?: 1f
        belongings.helmet?.let {
            if (it is WizardHat)
                factor = if (it.cursed) 0.9f else 1.15f
        }
        val bonus = Ring.getBonus(this, RingOfArcane.Arcane::class.java)
        factor *= 1.06f.pow(bonus)
        return factor
    }

    fun mentalFactor(): Float {
        var factor = if (heroClass == HeroClass.EXILE) 0.9f else 1f
        belongings.helmet?.let {
            if (it is CircletEmerald)
                factor *= if (it.cursed) 0.95f else 1.1f
            else if (it is StrawHat) factor *= 1.15f
        }
        if (buff(GoddessRadiance.Recharge::class.java)?.isCursed == true) factor *= 0.5f

        return factor
    }

    fun criticalChance(): Float {
        if (buff(CriticalRune.Critical::class.java) != null) return 1f

        var c = criticalChance
        if (pressure.level == Pressure.Level.CONFIDENT) c += 0.07f

        val level = Ring.getBonus(this, RingOfCritical.Critical::class.java)
        if (level > 0)
            c += 0.01f * level
        c *= 1.15f.pow(level)

        return c
    }

    fun wealthBonus(): Int = min(10, Ring.getBonus(this, RingOfWealth.Wealth::class.java) + if (subClass == HeroSubClass.LANCER) -1 else 0)

    // followers
    fun holdFollowers(level: Level) {
        followers.clear()

        level.mobs.filterTo(followers) { it.camp == Camp.HERO }
        level.mobs.removeAll(followers)

        // todo: MAX FOLLOWERS CONTROL

        Log.d("dpd", "${followers.size} followers held.")
    }

    fun restoreFollowers(level: Level, heropos: Int) {
        val avals = PathFinder.NEIGHBOURS8.map { it + heropos }.filter {
            !Level.solid[it] && level.findMobAt(it) == null
        }.shuffled()

        for (i in 0 until min(avals.size, followers.size)) {
            followers[i].pos = avals[i]
            level.mobs.add(followers[i] as Mob)
        }
        followers.clear()
    }

    fun className(): String = if (subClass == HeroSubClass.NONE) heroClass.title() else subClass.title()

    fun givenName(): String = if (name == Messages.get(this, "name")) className() else name

    fun sayShort(tag: String, vararg args: Any) {
        say(HeroLines.Line(tag, args))
    }

    override fun say(text: String) {
        val color = if (pressure.level > Pressure.Level.NORMAL) CharSprite.NEGATIVE else CharSprite.DEFAULT
        say(text, color)
    }

    // called on enter or resurrect the level
    fun live() {
        Buff.affect(this, Regeneration::class.java)
        Buff.affect(this, Hunger::class.java)
        Buff.affect(this, Protected::class.java)
        pressure = Buff.affect(this, Pressure::class.java)
        challenge?.live(this)
    }

    fun tier(): Int = belongings.armor?.tier ?: 0

    fun shoot(enemy: Char, weapon: MissileWeapon): Boolean {
        rangedWeapon = weapon
        val res = attack(enemy)

        Invisibility.dispel()
        rangedWeapon = null

        return res
    }

    override fun accRoll(damage: Damage): Float {
        var acc = super.accRoll(damage)

        acc *= pressure.accuracyFactor()
        if (buff(Drunk::class.java) != null) acc *= 0.75f

        // weapon
        acc *= (rangedWeapon ?: belongings.weapon)?.accuracyFactor(this, damage.to as Char) ?: 1f

        //todo: refacter this
        if (isUsingPolearm()) acc *= heroPerk.get(PolearmMaster::class.java)?.accFactor() ?: 1f

        if (belongings.helmet is MaskOfHorror && belongings.helmet!!.cursed) acc *= 0.75f

        return acc
    }

    fun evasionProbability(): Float {
        val level = Ring.getBonus(this, RingOfEvasion.Evasion::class.java)
        var e = 1f - 0.01f * level;

        // GameMath.ProbabilityPlus()
        heroPerk.get(ExtraEvasion::class.java)?.let {
            e *= 1f - it.prob()
        }
        heroPerk.get(BaredSwiftness::class.java)?.let {
            e *= 1f - it.evasionProb(this)
        }
        heroPerk.get(LowHealthDexterous::class.java)?.let {
            e *= 1f - it.extraEvasion(this)
        }
        if (buff(CloakOfShadows.cloakRecharge::class.java)?.enhanced() == true)
            e *= 1f - 0.2f

        e *= 0.975f.pow(level)

        return 1 - e
    }

    override fun dexRoll(damage: Damage): Float {
        // evasion
        if (Random.Float() < evasionProbability()) return 1000f

        var dex = super.dexRoll(damage)

        val roe = Ring.getBonus(this, RingOfEvasion.Evasion::class.java)
        var factor = 1.1f.pow(roe)

        if (paralysed > 0) factor *= 0.5f

        if (heroClass == HeroClass.SORCERESS) factor *= 0.8f

        factor *= pressure.evasionFactor()

        val estr = (belongings.armor?.STRReq() ?: 10) - STR()
        if (estr > 0) factor *= 1f / 1.5f.pow(estr)
        else {
            var bonus = if (heroPerk.has(LowWeightDexterous::class.java)) -estr.toFloat() else 0f
            if (belongings.armor?.hasGlyph(Swiftness::class.java) == true)
                bonus += 5f + belongings.armor!!.level() * 1.5f

            dex += Random.Float(bonus)
        }

        return dex * factor
    }

    fun canSurpriseAttack(): Boolean {
        if (belongings.weapon == null || belongings.weapon !is Weapon) return true
        if (STR() < (belongings.weapon as Weapon).STRReq()) return false

        if (rangedWeapon == null &&
                (belongings.weapon is Flail || belongings.weapon is Lance ||
                        belongings.weapon is DriedLeg))
            return false

        return true
    }

    override fun giveDamage(enemy: Char): Damage {
        var dmg = Damage(0, this, enemy)

        // weapon
        val bonus = Ring.getBonus(this, RingOfForce.Force::class.java)

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
        belongings.helmet?.procGivenDamage(dmg)

        heroPerk.get(BaredAngry::class.java)?.procGivenDamage(dmg, this)

        // critical
        if (!dmg.isFeatured(Damage.Feature.CRITICAL) && Random.Float() < criticalChance()) {
            val ratio = if (heroClass == HeroClass.EXILE) 1.75f else 1.5f

            dmg.value = (dmg.value * ratio).toInt()
            dmg.addFeature(Damage.Feature.CRITICAL)
        }

        if (dmg.isFeatured(Damage.Feature.CRITICAL)) {
            heroPerk.get(PureCrit::class.java)?.procCrit(dmg)
            heroPerk.get(HardCrit::class.java)?.procCrit(dmg)
        }

        // pressure
        pressure.procGivenDamage(dmg)

        buff(SeeThrough::class.java)?.processDamage(dmg)

        buff(Drunk::class.java)?.procGivenDamage(dmg)

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
            buff(CapeOfThorns.Thorns::class.java)?.proc(dmg)
            belongings.weapon?.defendDamage(dmg)

            var dr = 0
            belongings.armor?.let {
                dr = Random.NormalIntRange(it.DRMin(), it.DRMax())

                val estr = belongings.armor!!.STRReq() - STR()
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

        val hlvl = Ring.getBonus(this, RingOfHaste.Haste::class.java)
        if (hlvl != 0) speed *= 1.2f.pow(hlvl)

        belongings.armor?.let {
            if (it.hasGlyph(Swiftness::class.java))
                speed *= (1.1f + 0.01f * it.level())
            else if (it.hasGlyph(Flow::class.java) && Level.water[pos])
                speed *= (1.5f + 0.05f * it.level())
        }

        heroPerk.get(BaredSwiftness::class.java)?.let {
            speed *= it.speedFactor(this)
        }

        buff(HasteRune.Haste::class.java)?.let { speed *= 3f }

        var estr = if (belongings.armor != null) belongings.armor!!.STRReq() - STR() else 0
        estr += if (belongings.weapon is Weapon) (belongings.weapon as Weapon).STRReq() - STR() else 0
        if (estr > 0)
            return speed / 1.2f.pow(estr)
        else {
            if ((sprite as HeroSprite).sprint(subClass == HeroSubClass.FREERUNNER && !isStarving()))
                speed *= if (invisible > 0) 2f else 1.5f

            return speed
        }
    }

    private fun isStarving(): Boolean = buff(Hunger::class.java)!!.isStarving

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
            speed *= 0.25f + 0.75f * 0.8f.pow(Ring.getBonus(this, RingOfFuror.Furor::class.java))
        }

        speed *= buff(Combo::class.java)?.speedFactor() ?: 1f
        speed *= heroPerk.get(BaredAngry::class.java)?.speedFactor(this) ?: 1f
        speed *= heroPerk.get(Maniac::class.java)?.speedFactor(this) ?: 1f

        return max(0.25f, speed)
    }

    override fun attackProc(dmg: Damage): Damage {
        val wep = (rangedWeapon ?: belongings.weapon) as Weapon?

        wep?.proc(dmg)
        if (rangedWeapon != null && subClass == HeroSubClass.SNIPER)
        // sniper perk
        // Buff.prolong(this, SnipersMark::class.java, attackDelay() * 1.1f).`objectid` = (dmg.to as Char).id()
            Buff.prolong(dmg.to as Char, ViewMark::class.java, attackDelay() * 2f).observer = id()
        else {
            // exile perk
            if (isUsingPolearm())
                heroPerk.get(PolearmMaster::class.java)?.proc(dmg, belongings.weapon as MeleeWeapon)

            if (subClass == HeroSubClass.WINEBIBBER) buff(Drunk::class.java)?.attackProc(dmg)
        }

        // critical damage
        if (dmg.isFeatured(Damage.Feature.CRITICAL)) {
            // recover sanity
            val str = wep?.STRReq(0) ?: 10
            if (dmg.value > 0 && Random.Int(15 - str / 2) == 0)
                recoverSanity(min(Random.Int(dmg.value / 6) + 1, 10).toFloat())

            heroPerk.get(VampiricCrit::class.java)?.procCrit(dmg)
        }

        return dmg
    }

    override fun defenseProc(dmg: Damage): Damage {
        buff(Earthroot.Armor::class.java)?.procTakenDamage(dmg)
        buff(Sungrass.Health::class.java)?.absorb(dmg.value)

        belongings.armor?.proc(dmg)

        return dmg
    }

    override fun resistDamage(dmg: Damage): Damage {
        if (buff(RiemannianManifoldShield.Recharge::class.java)?.isCursed == true)
            return dmg

        if (dmg.type == Damage.Type.MAGICAL && ((belongings.armor is MageArmor) &&
                        (belongings.armor as MageArmor).enhanced))
            dmg.value = round(dmg.value * 0.9f).toInt()

        heroPerk.get(PressureRelieve::class.java)?.affectDamage(dmg, this, pressure)

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

        buff(CrackedCoin.Shield::class.java)?.procTakenDamage(dmg)
        buff(DragonsSquama.Recharge::class.java)?.procTakenDamage(dmg)

        val drunk = buff(Drunk::class.java)
        drunk?.procTakenDamage(dmg)

        if (dmg.type == Damage.Type.MENTAL) return takeMentalDamage(dmg)

        val dmgToken = super.takeDamage(dmg)
        heroPerk.get(LowHealthRegeneration::class.java)?.onDamageTaken(this)

        if (isAlive) {
            //todo: refactor
            var tag: String? = null
            val maySay = { p: Float, str: String ->
                if (tag == null && Random.Float() < p) tag = str
            }

            // extra mental damage
            var value = 0f
            if (dmg.from is Char && !Dungeon.visible[(dmg.from as Char).pos]) { // attack from nowhere
                value += Random.Float(1f, 7f)
                maySay(0.2f, HeroLines.WHAT)
            }
            if (dmg.isFeatured(Damage.Feature.CRITICAL)) {
                value += Random.Float(2f, 8f)
                maySay(0.2f, HeroLines.DAMN)
            }

            if (!heroPerk.has(Fearless::class.java) && HP < HT / 4 && dmg.from is Mob && dmgToken > 0) {
                value += Random.Float(1f, 5f)
                maySay(0.4f, HeroLines.I_MAY_DIE)
                maySay(0.3f, HeroLines.MY_WEAPON_IS_BAD)
            }

            if (tag != null) sayShort(tag!!)

            // todo: this is fragile
            val mentaldmg = Damage(min(round(value).toInt(), 15), dmg.from, dmg.to).type(Damage.Type.MENTAL)
            drunk?.procTakenDamage(mentaldmg)
            takeMentalDamage(mentaldmg)
        }

        return dmgToken
    }

    private fun takeMentalDamage(dmg: Damage): Int {
        if (dmg.value <= 0) return 0

        var value = dmg.value.toFloat()

        if (heroClass == HeroClass.EXILE) {
            var v = 1f
            if (subClass == HeroSubClass.WINEBIBBER && buff(Drunk::class.java) == null) {
                v += 1.5f
            }


            value += Random.Float(0f, v)
        }

        var chance = GameMath.ProbabilityPlus(
                heroPerk.get(Optimistic::class.java)?.resistChance() ?: 0f,
                buff(GoddessRadiance.Recharge::class.java)?.evadeRatio() ?: 0f)

        if (belongings.helmet is Mantilla && !belongings.helmet!!.cursed)
            chance = GameMath.ProbabilityPlus(chance, 0.1f)

        if (Random.Float() < chance) {
            value = 0f
            sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "mental_resist"))
        } else {
            if (heroClass == HeroClass.EXILE && Random.Int(10) == 0 && buff(Drunk::class.java) == null)
                sayShort("grim_${Random.Int(3)}") //todo: this is fragile
        }

        // keep in mind that SAN is pressure, it increases
        val rv = pressure.upPressure(value).toInt()
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
        if (buff(TimekeepersHourglass.TimeFreeze::class.java)?.processTime(time) != true) {
            if (buff(TimeDilation::class.java) != null) super.spend(time / 3f)
            else super.spend(time)
        }
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
        for (mob in Dungeon.level.mobs.filter { Level.fieldOfView[it.pos] && it.camp == Camp.ENEMY && it.isLiving }) {
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
            val newPath = if (path == null || path!!.isEmpty() || !Dungeon.level.adjacent(pos, path!!.first)) true
            else if (path!!.last != target) true
            else {
                //checks 2 cells ahead for validity.
                //Note that this is shorter than for mobs, so that mobs usually yield
                // to the hero
                val lookAhead = GameMath.clamp(path!!.size - 1, 0, 2)
                (0 until lookAhead).map { path!!.get(it) }.any {
                    !Level.passable[it] || (Dungeon.visible[it] && Actor.findChar(it) != null)
                }
            }

            if (newPath) {
                val len = Dungeon.level.length()
                val passable = BooleanArray(len) { Level.passable[it] && (Dungeon.level.visited[it] || Dungeon.level.mapped[it]) }

                path = Dungeon.findPath(this, pos, target, passable, Level.fieldOfView)
            }

            if (path == null) return false

            step = path!!.removeFirst()
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
            //todo: make it clean
            if (ch.camp == Camp.ENEMY) return HeroAction.Attack(ch)
            if (ch is NPC) return HeroAction.Interact(ch)
            //
            return HeroAction.InteractAlly(ch)
        }

        // heap there
        val heap = Dungeon.level.heaps.get(cell)
        // moving to an item doesn't auto-pickup when enemies are near...
        // but only for standard heaps, chests and similar open as normal.
        if (heap != null && (visibleEnemies.size == 0 || cell == pos ||
                        (heap.type != Heap.Type.HEAP))) {
            return when (heap.type) {
                Heap.Type.HEAP -> HeroAction.PickUp(cell)
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

    //2, 6, 10... gain a perk
    private fun shouldGainPerk() = if (challenge == Challenge.Gifted) (lvl - 2) % 3 == 0 else (lvl - 2) % 4 == 0

    fun earnExp(gained: Int) {
        exp += gained
        exp += heroPerk.get(QuickLearner::class.java)?.extraExp(gained) ?: 0

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

                if (shouldGainPerk()) {
                    interrupt()
                    reservedPerks += 1
                    GLog.p(M.L(this, "perk_gain"))
                }

                if (lvl == 12 && Dungeon.hero.subClass == HeroSubClass.NONE) {
                    Badges.validateMastery()
                    WndMasterSubclass.Show(this)
                }
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

            if (pressure.level > Pressure.Level.NORMAL) sayShort(HeroLines.USELESS)

            Badges.validateLevelReached()
        }
    }

    internal fun updateAwareness() {
        val w = heroPerk.get(Keen::class.java)?.baseAwareness() ?: 0.9f
        awareness = 1f - w.pow((1 + min(lvl, 9)) / 2f)
    }

    override fun add(buff: Buff) {
        if (belongings.armor?.glyph is Tough) {
            if ((belongings.armor!!.glyph as Tough).resist(buff)) return
        }

        if (immunizedBuffs().any { buff.javaClass == it }) return

        super.add(buff)

        if (hasSprite) {
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

        // stealth += RingOfEvasion.getBonus(this, RingOfEvasion.Evasion::class.java)

        if (belongings.armor?.hasGlyph(Obfuscation::class.java) == true)
            stealth += belongings.armor!!.level()

        return stealth
    }

    override fun recoverHP(dhp: Int, src: Any?) {
        super.recoverHP(dhp, src)

        if (!isAlive) Dungeon.fail(src?.javaClass)
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
            recoverSanity(min(20f, pressure.pressure * 0.25f))
            Buff.detach(this, Paralysis::class.java)
            spend(-cooldown())

            Flare(8, 32f).color(0xFFFF66, true).show(sprite, 2f)
            CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3)

            ankh.detach(belongings.backpack)

            Sample.INSTANCE.play(Assets.SND_TELEPORT)
            GLog.w(Messages.get(this, "revive"))
            Statistics.AnkhsUsed += 1

            GhostHero.Instance()?.sayAnhk()
            if (belongings.getItem(Ankh::class.java) == null) sayShort(HeroLines.WHAT_ABOUT_NEXT)
            return
        }

        Actor.fixTime()
        super.die(src)

        if (ankh == null) ReallyDie(src)
        else {
            Dungeon.deleteGame(false, true)
            GameScene.show(WndResurrect(ankh, src))
        }
    }

    override val isAlive: Boolean
        get() {
            if (subClass == HeroSubClass.BERSERKER)
                if (buff(Berserk::class.java)?.berserking() == true) return true
            return super.isAlive
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

        var level = if (intentional) 2 * awareness - awareness * awareness else awareness

        val distance = if (heroPerk.has(EfficientSearch::class.java)) 2 else 1

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
        if (mob.properties().contains(Property.PHANTOM)) return

        belongings.getItem(UrnOfShadow::class.java)?.collectSoul(mob)

        buff(VampiricBite::class.java)?.onEnemySlayed(mob)

        if (mob.properties().contains(Property.BOSS)) GhostHero.Instance()?.sayBossBeaten()
    }

    fun onEvasion(dmg: Damage) {
        heroPerk.get(CounterStrike::class.java)?.procEvasionDamage(dmg)
        heroPerk.get(EvasionTenacity::class.java)?.procEvasionDamage(dmg)
        buff(Blur.Counter::class.java)?.onEvade()

        if (subClass == HeroSubClass.WINEBIBBER) buff(Drunk::class.java)?.onEvade(dmg)
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

            if (Random.Float() < px * py) {
                recoverSanity(Random.Float(1f, 6f))

                if (pressure.level > Pressure.Level.NORMAL) {
                    sayShort(HeroLines.WHAT_ABOUT_NEXT)
                } else if (Random.Float() < 0.3f) sayShort(HeroLines.GRIN)
                else sayShort(HeroLines.DIE)
            }
        }

        if (belongings.helmet is MaskOfMadness) (belongings.helmet as MaskOfMadness).onEnemySlayed(ch)

        buff(BloodSuck::class.java)?.onEnemySlayed(ch)
        buff(ChaliceOfBlood.Store::class.java)?.onEnemySlayed(ch)
        buff(SacrificialFire.Marked::class.java)?.onEnemySlayed(ch)
        if (rangedWeapon != null) {
            // killed by a ranged weapon
            heroPerk.get(FinishingShot::class.java)?.onKilledChar(this, ch, rangedWeapon!!)
        }

        GLog.i(Messages.capitalize(Messages.get(Char::class.java, "defeat", ch.name)))
    }


    // animation callbacks
    override fun onMotionComplete() {
        Dungeon.observe()
        search(false)
    }

    override fun onAttackComplete() {
        AttackIndicator.target(enemy)

        val hit = attack(enemy!!)

        if (subClass == HeroSubClass.GLADIATOR) {
            if (hit) Buff.affect(this, Combo::class.java).hit(enemy!!)
            else buff(Combo::class.java)?.miss()
        } else if (subClass == HeroSubClass.LANCER) {
            if (hit) Buff.affect(this, Penetration::class.java).hit()
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

            info.name = bundle.getString(USER_NAME)
            if (info.name.isEmpty()) info.name = M.L(Hero::class.java, "username")

            info.heroClass = HeroClass.RestoreFromBundle(bundle)
            info.subClass = HeroSubClass.RestoreFromBundle(bundle)

            // info.armorTier =
            val armor = bundle.get("armor") as Armor?
            info.armorTier = armor?.tier ?: 0

            val chastr = bundle.getString(CHALLENGE)
            if (chastr.isNotEmpty()) info.challenge = Challenge.valueOf(chastr)
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
            DarkSpirit.Leave()

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

            if (src is Hero) Badges.validateSuicide()

            if (src is Doom) src.onDeath()

            Dungeon.deleteGame(true, true)
        }

        //
        private const val ATTACK = "atkSkill"
        private const val DEFENSE = "defSkill"
        private const val STRENGTH = "STR"
        private const val LEVEL = "lvl"
        private const val EXPERIENCE = "exp"
        private const val CRITICAL = "critical"
        private const val REGENERATION = "regeneration"
        private const val ELEMENTAL_RESISTANCE = "elemental_resistance"
        private const val MAGICAL_RESISTANCE = "magical_resistance"
        private const val RESERVED_PERKS = "reserved_perks"
        private const val CHALLENGE = "challenge"
        private const val PERK_GAIN = "perk_gain"
        private const val SPAWNED_PERKS = "spawned-perks"
        private const val USER_NAME = "username"
        private const val POH_DRUNK = "poh_drunk"
    }

    // store
    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        bundle.put(USER_NAME, userName)

        heroClass.storeInBundle(bundle)
        subClass.storeInBundle(bundle)
        heroPerk.storeInBundle(bundle)

        bundle.put(ATTACK, atkSkill)
        bundle.put(DEFENSE, defSkill)

        bundle.put(STRENGTH, STR)

        bundle.put(LEVEL, lvl)
        bundle.put(EXPERIENCE, exp)

        bundle.put(CRITICAL, criticalChance)
        bundle.put(REGENERATION, regeneration)

        bundle.put(ELEMENTAL_RESISTANCE, elementalResistance)
        bundle.put(MAGICAL_RESISTANCE, magicalResistance)

        bundle.put(RESERVED_PERKS, reservedPerks)
        if (spawnedPerks.isNotEmpty()) bundle.put(SPAWNED_PERKS, spawnedPerks)
        bundle.put(PERK_GAIN, perkGained_)
        bundle.put(POH_DRUNK, pohDrunk)

        if (challenge != null) bundle.put(CHALLENGE, challenge.toString())

        belongings.storeInBundle(bundle)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        userName = bundle.getString(USER_NAME)

        heroClass = HeroClass.RestoreFromBundle(bundle)
        subClass = HeroSubClass.RestoreFromBundle(bundle)
        heroPerk.restoreFromBundle(bundle)

        atkSkill = bundle.getFloat(ATTACK)
        defSkill = bundle.getFloat(DEFENSE)

        STR = bundle.getInt(STRENGTH)
        updateAwareness()

        lvl = bundle.getInt(LEVEL)
        exp = bundle.getInt(EXPERIENCE)

        criticalChance = bundle.getFloat(CRITICAL)
        regeneration = bundle.getFloat(REGENERATION)

        elementalResistance = bundle.getFloatArray(ELEMENTAL_RESISTANCE)
        magicalResistance = bundle.getFloat(MAGICAL_RESISTANCE)

        reservedPerks = bundle.getInt(RESERVED_PERKS)

        perkGained_ = bundle.getInt(PERK_GAIN)
        pohDrunk = bundle.getInt(POH_DRUNK)
        spawnedPerks.clear()
        if (bundle.contains(SPAWNED_PERKS))
            for (p in bundle.getCollection(SPAWNED_PERKS)) if (p is Perk) spawnedPerks.add(p)
        belongings.restoreFromBundle(bundle)

        val pre = buff(Pressure::class.java)
        if (pre != null) pressure = pre

        val chastr = bundle.getString(CHALLENGE)
        if (chastr.isNotEmpty()) challenge = Challenge.valueOf(chastr)

    }
}