package com.egoal.darkestpixeldungeon.windows

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.ExtraPerkChoice
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk
import com.egoal.darkestpixeldungeon.actors.hero.perks.PerkImageSheet
import com.egoal.darkestpixeldungeon.effects.PerkGain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.ui.PerkSlot
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.ui.Window
import com.watabou.noosa.ColorBlock
import com.watabou.utils.Bundle

class WndGainNewPerk(title: String, perks: List<Perk>) : WndSelectPerk(title, perks) {

    override fun onPerkSelected(perk: Perk) {
        if (perk !is RandomAnotherPerk) {
            Dungeon.hero.heroPerk.add(perk)
            Dungeon.hero.reservedPerks -= 1
            Dungeon.hero.spawnedPerks.clear()
            Dungeon.hero.perkGained += 1
            PerkGain.Show(Dungeon.hero!!, perk)
            return
        }

        val perks = perkButtons.map { it.perk().javaClass }

        val count = perks.size / 2 // 5->3, 3->2, todo: fix this.
        val alterperks = HashSet<Class<*>>()
        while (alterperks.size < count) {
            val p = Perk.RandomPositive(Dungeon.hero)
            if (p !is Perk.Companion.LuckFromAuthor &&
                    (p.javaClass in perks || p.javaClass in alterperks)) continue

            alterperks.add(p.javaClass)
        }
        Dungeon.hero.spawnedPerks.clear()
        Dungeon.hero.spawnedPerks.addAll(alterperks.map { it.newInstance() as Perk })

        GameScene.show(WndGainNewPerk(M.L(WndGainNewPerk::class.java, "title"), Dungeon.hero.spawnedPerks))
    }

    class RandomAnotherPerk : Perk() {
        override fun image(): Int = PerkImageSheet.REROLL
    }

    companion object {
        fun Show(hero: Hero) {
            // spawn
            if (hero.spawnedPerks.isEmpty()) {
                val cnt = if (hero.heroPerk.has(ExtraPerkChoice::class.java)) 5 else 3
                hero.spawnedPerks.addAll(Perk.RandomPositives(hero, cnt))
                hero.spawnedPerks.add(RandomAnotherPerk())
            }

            GameScene.show(WndGainNewPerk(M.L(WndGainNewPerk::class.java, "title"), hero.spawnedPerks))
        }
    }
}