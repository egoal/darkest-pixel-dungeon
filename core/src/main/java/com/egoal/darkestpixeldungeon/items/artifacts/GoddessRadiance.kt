package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.ExpandHalo
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.unclassified.DewVial
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import kotlin.math.pow

class GoddessRadiance : Artifact() {
    init {
        image = ItemSpriteSheet.GODESS_RADIANCE

        levelCap = 10
        chargeCap = 100
        charge = chargeCap

        defaultAction = AC_ACTIVATE

        exp = 0
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero))
            if (!cursed) {
                if (level() < levelCap) desc += "\n\n" + M.L(this, "desc_hint")
                else desc += "\n\n" + M.L(this, "desc_max")
            } else desc += "\n\n" + M.L(this, "desc_cursed")

        return desc
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val acts = super.actions(hero).apply { add(AC_ACTIVATE) }
        if (level() < levelCap && hero.belongings.getItem(DewVial::class.java) != null) {
            acts.add(AC_BLESS)
        }
        return acts
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_ACTIVATE) {
            if (!isEquipped(hero)) GLog.w(M.L(Artifact::class.java, "need_to_equip"))
            else if (cursed) GLog.w(M.L(this, "cursed"))
            else if (charge < chargeCap) GLog.w(M.L(this, "no_charge"))
            else radiance(hero)
        } else if (action == AC_BLESS) {
            if (!isEquipped(hero)) GLog.w(M.L(Artifact::class.java, "need_to_equip"))
            else if (cursed) GLog.w(M.L(this, "cursed"))
            else hero.belongings.getItem(DewVial::class.java)?.let {
                if (it.Volume < 10) GLog.w(M.L(this, "too_less"))
                else {
                    hero.sprite.operate(hero.pos)
                    hero.spendAndNext(1f)

                    earnExp(it.Volume / 2)
                    it.empty()
                }
            }
        }
    }

    private fun radiance(hero: Hero) {
        // copy from ScrollOfLight
        // light!
        ExpandHalo(4f, 48f).show(hero.sprite, 0.75f)
        Sample.INSTANCE.play(Assets.SND_BLAST)
        Invisibility.dispel()

        // give light, shock nearby mobs
        Buff.affect(Item.curUser, Light::class.java).prolong(10f + level() * 3f)

        val range = 4 + level() / 2
        Dungeon.level.mobs.filter { Level.fieldOfView[it.pos] }.forEach {
            val dis = Dungeon.level.distance(it.pos, hero.pos)
            if (dis <= range && it.isAlive) {
                Buff.prolong(it, Blindness::class.java, 3f + level() / 2f)
                Buff.prolong(it, Shock::class.java, (range - dis).toFloat())

                val b = Ballistica(hero.pos, it.pos, Ballistica.MAGIC_BOLT)
                if (b.path.size > b.dist + 1) {
                    val bb = Ballistica(it.pos, b.path[b.dist + 1], Ballistica.MAGIC_BOLT)
                    WandOfBlastWave.throwChar(it, bb, range - dis)
                }
            }
        }

        charge = 0
        hero.sprite.operate(hero.pos)
        hero.spendAndNext(1f)
        updateQuickslot()
    }

    private fun earnExp(e: Int) {
        if (level() >= levelCap) return

        exp += e
        val mexp = 15 + 10 * level() //
        while (exp >= mexp && level() < levelCap) {
            exp -= mexp
            upgrade()

            GLog.p(M.L(GoddessRadiance::class.java, "levelup"))
        }
    }

    override fun passiveBuff(): ArtifactBuff = Recharge()

    inner class Recharge : ArtifactBuff() {
        fun viewAmend() = if (level() == levelCap) 1 else 0
        fun evadeRatio() = 0.4f - 0.4f * 0.9f.pow(level()) // +6 ~= level 2 optimistic perk

        override fun act(): Boolean {
            // recharge
            if (charge < chargeCap && !cursed) {
                partialCharge += 100f / (4f + level() * 0.6f) // cd: 40+ lvl* 6 for each level
                if (partialCharge > 1f) {
                    charge += partialCharge.toInt()
                    partialCharge -= partialCharge.toInt()
                    if (charge >= chargeCap) {
                        charge = chargeCap
                        partialCharge = 0f
                        GLog.p(M.L(GoddessRadiance::class.java, "charged"))
                    }
                }
            }

            // grow
            earnExp(1)

            updateQuickslot()
            spend(10f)
            return true
        }
    }

    companion object {
        private const val AC_ACTIVATE = "activate"
        private const val AC_BLESS = "bless"
    }
}