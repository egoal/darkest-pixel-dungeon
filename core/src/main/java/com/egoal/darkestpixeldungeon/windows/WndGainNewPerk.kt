package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.actors.hero.perks.PerkImageSheet
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.PerkSlot
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.ColorBlock

class WndGainNewPerk(title: String, perks: List<Perk>) :
        WndSelectPerk(title, listOf(*perks.toTypedArray(), RandomAnotherPerk())) {
    override fun onPerkSelected(perk: Perk) {
        if (perk !is RandomAnotherPerk) {
            Dungeon.hero.heroPerk.add(perk)
            Dungeon.hero.perkGained += 1
            PerkGain.Show(Dungeon.hero!!, perk)
            return
        }

        // use another
        val perks = perkButtons.map { it.perk().javaClass }
        for (i in 1..20) {
            val newPerk = Perk.RandomPositive(Dungeon.hero)
            if (newPerk.javaClass !in perks) {
                onPerkSelected(newPerk)
                break
            }
        }
    }

    override fun onBackPressed() {
        // cannot be cancelled
    }

    class RandomAnotherPerk : Perk() {
        override fun image(): Int = PerkImageSheet.REROLL
    }

    companion object {
        fun CreateWithRandomPositives(count: Int): WndGainNewPerk {
            return WndGainNewPerk(M.L(WndGainNewPerk::class.java, "title"), Perk.RandomPositives(Dungeon.hero, count))
        }
    }
}