package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import kotlin.math.min

class TorsoOfTheElder : Artifact() {
    init {
        image = ItemSpriteSheet.A_TORSO
        levelCap = 10
    }

    override fun desc(): String {
        return super.desc() + "\n\n" + M.L(this, "desc_hint")
    }

    override fun passiveBuff(): ArtifactBuff = HealthChecker()

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        GLog.n(M.L(this, "cannot_unequip"))
        return false
    }

    override fun random(): Item = this.apply {
        cursed = true
    }

    override fun doEquip(hero: Hero): Boolean {
        val re = super.doEquip(hero)
        if (re) {
            hero.MSHLD += shld()
            hero.HP = min(hero.HP, (hero.HT * .3f).toInt())
            hero.magicalResistance = min(hero.magicalResistance + .25f, .5f)
        }
        return re
    }

    inner class HealthChecker : Artifact.ArtifactBuff() {
        override fun act(): Boolean {
            target.HP = min(target.HP, (target.HT * .3f).toInt())
            spend(0.5f) // twice faster
            return true
        }

        fun hit() {
            exp += 1
            val mexp = 5 * level() + 10
            if (exp >= mexp && level() < levelCap) {
                exp -= mexp
                val hero = target as Hero
                hero.MSHLD -= shld()
                upgrade()
                hero.MSHLD += shld()
                GLog.p(M.L(this, "levelup"))
            }
        }
    }

    private fun shld() = level() * 5

    override fun price(): Int = 0
}