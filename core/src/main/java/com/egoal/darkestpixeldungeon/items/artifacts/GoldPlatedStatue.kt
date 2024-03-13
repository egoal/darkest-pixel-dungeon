package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random
import java.util.*

/**
 * Created by 93942 on 9/21/2018.
 */

// check Gold::doPickUp
class GoldPlatedStatue : Artifact() {
    init {
        image = ItemSpriteSheet.GOLD_PLATE_STATUE

        levelCap = 10
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (level() < levelCap && !cursed)
            actions.add(AC_INVEST)

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_INVEST && level() < levelCap) {
            if (!isEquipped(hero))
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (cursed)
                GLog.i(Messages.get(this, "cursed"))
            else {
                val goldRequired = (100 * Math.pow(1.27, level().toDouble())).toInt()
                if (Dungeon.gold < goldRequired)
                    GLog.w(Messages.get(GoldPlatedStatue::class.java, "no_enough_gold",
                            goldRequired))
                else {
                    Dungeon.gold -= goldRequired

                    upgrade()
                    GLog.p(Messages.get(GoldPlatedStatue::class.java, "levelup", goldRequired))
                }
            }
        }
    }

    override fun desc(): String {
        var desc = super.desc()

        if (isEquipped(Dungeon.hero)) {
            if (!cursed) {
                if (level() < levelCap)
                    desc += "\n\n" + Messages.get(this, "desc_hint")
            } else {
                desc += "\n\n" + Messages.get(this, "desc_cursed")
            }
        }

        return desc
    }

    override fun passiveBuff() = Greedy()

    inner class Greedy : Artifact.ArtifactBuff() {
        fun extraCollect(gold: Int): Int {
            var ratio = if (cursed) -.3f else level() * .1f

            if (isFullyUpgraded && Random.Float() < .1f) {
                CellEmitter.get(target.pos).burst(Speck.factory(Speck.COIN), Random.IntRange(10, 15))
                ratio += 1f
            }

            return (gold * ratio).toInt()
        }
    }

    companion object {
        private const val AC_INVEST = "INVEST"
    }
}
