package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.rings.RingOfMight
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.messages.Messages

// refactor may finish someday...
class KHero : Char() {
    init {
        actPriority = 0
        name = Messages.get(this, "name")

        HT = 22
        HP = HT
    }

    // properties
    var heroClass = HeroClass.ROGUE
    var subClass = HeroSubClass.NONE
    val heroPerk = HeroPerk(0)

    private var attackSkill = 10
    private var defenseSkill = 5
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
    private var enemy: Char? = null

    private val visibleEnemies = mutableListOf<Mob>()

    // follower follow hero on level switch, just cache
    private val followers = mutableListOf<Char>()

    // equipments
    var rangedWeapon: MissileWeapon? = null
    val belongings = Belongings(Dungeon.hero) //fixme

    fun STR(): Int {
        var str = this.STR + if (weakened) -2 else 0
        str += RingOfMight.getBonus(this, RingOfMight.Might::class.java)
        return str
    }

    override fun viewDistance(): Int {
        var vd = super.viewDistance()
        if (heroPerk.contain(HeroPerk.Perk.NIGHT_VISION) &&
                (Statistics.Clock.state == Statistics.ClockTime.State.Night ||
                        Statistics.Clock.state == Statistics.ClockTime.State.MidNight))
            vd += 1
        vd += belongings.helmet?.viewAmend()?: 0

        return vd
    }

    companion object {
        const val MAX_LEVEL = 30
        const val STARTING_STR = 10

        const val TIME_TO_REST = 1f
        const val TIME_TO_SEARCH = 2f

        private const val MAX_FOLLOWERS = 3
    }
}