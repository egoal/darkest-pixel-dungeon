package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

class ThornsOfPain : Artifact() {
    private var time = 0f
    private var damageTaken = 0
    private var damageTakenTimes = 0
    private var mentalDamageTaken = 0f
    private var eliteKilled = 0
    private var versionStr: String

    init {
        image = ItemSpriteSheet.THORNS_OF_PAIN
        versionStr = DarkestPixelDungeon.version + "-" + SimpleDateFormat("yyyyMdd").format(Date())
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        GLog.n(M.L(this, "cannot_unequip"))
        return false
    }

    override fun desc(): String {
        var desc = super.desc()

        desc += "\n\n" + M.L(this, "desc_statistics", time.roundToInt(),
                damageTaken, damageTakenTimes,
                mentalDamageTaken.roundToInt(),
                eliteKilled,
                versionStr)

        return desc
    }

    override fun passiveBuff(): ArtifactBuff = Pain()

    inner class Pain : Artifact.ArtifactBuff() {
        override fun act(): Boolean {
            time += TICK_STEP
            spend(TICK_STEP)
            return true
        }

        fun onDamageTaken(hero: Hero, damage: Damage) {
            if (damage.value + damage.add_value > 0) {
                damageTaken += damage.value + damage.add_value
                damageTakenTimes++
            }
        }

        fun onMentalDamageTaken(hero: Hero, value: Float) {
            mentalDamageTaken += max(0f, value)
        }

        fun onMobDied(mob: Mob) {
            if (mob.properties().contains(Char.Property.ELITE)) eliteKilled++
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("time", time)
        bundle.put("damageTaken", damageTaken)
        bundle.put("damageTakenTimes", damageTakenTimes)
        bundle.put("mentalDamageTaken", mentalDamageTaken)
        bundle.put("eliteKilled", eliteKilled)
        bundle.put("versionStr", versionStr)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        time = bundle.getFloat("time")
        damageTaken = bundle.getInt("damageTaken")
        damageTakenTimes = bundle.getInt("damageTakenTimes")
        mentalDamageTaken = bundle.getFloat("mentalDamageTaken")
        eliteKilled = bundle.getInt("eliteKilled")
        versionStr = bundle.getString("versionStr")
        if (versionStr.isEmpty()) versionStr = "unknown"
    }

    companion object {
        private const val TICK_STEP = 100f
    }
}