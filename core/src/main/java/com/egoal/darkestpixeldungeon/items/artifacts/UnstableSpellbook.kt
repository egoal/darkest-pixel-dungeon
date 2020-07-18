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
package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfIdentify
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.ArrayList

class UnstableSpellbook : Artifact() {
    private val scrolls = ArrayList<Class<out Scroll>>()

    private val mode: WndBag.Mode = WndBag.Mode.SCROLL

    private val itemSelector: WndBag.Listener = WndBag.Listener { item ->
        if (item !is Scroll) return@Listener

        if (!item.isIdentified) GLog.w(M.L(UnstableSpellbook::class.java, "unknown_scroll"))
        else {
            val hero = Dungeon.hero
            var i = 0
            while (i <= 2 && i < scrolls.size) {
                if (scrolls[i] == item.javaClass) {
                    hero.sprite.operate(hero.pos)
                    hero.busy()
                    hero.spend(2f)
                    Sample.INSTANCE.play(Assets.SND_BURNING)
                    hero.sprite.emitter().burst(ElmoParticle.FACTORY, 12)

                    scrolls.removeAt(i)
                    item.detach(hero.belongings.backpack)

                    upgrade()
                    GLog.i(Messages.get(UnstableSpellbook::class.java, "infuse_scroll"))
                    return@Listener
                }
                i++
            }
            GLog.w(Messages.get(UnstableSpellbook::class.java, "unable_scroll"))
        }
    }

    init {
        image = ItemSpriteSheet.ARTIFACT_SPELLBOOK

        levelCap = 10

        charge = level() / 2 + 3
        partialCharge = 0f
        chargeCap = level() / 2 + 3

        defaultAction = AC_READ


        // "sort" by prob
        val scrollprobs = HashMap<Class<out Scroll>, Float>()
        for (pr in Generator.SCROLL.initialProbs) scrollprobs[pr.key.java as Class<out Scroll>] = pr.value

        var cls = Random.chances(scrollprobs)
        while (cls != null) {
            scrolls.add(cls)
            scrollprobs[cls] = 0f
            cls = Random.chances(scrollprobs)
        }
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && charge > 0 && !cursed)
            actions.add(AC_READ)
        if (isEquipped(hero) && level() < levelCap && !cursed)
            actions.add(AC_ADD)
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_READ) {

            if (hero.buff(Blindness::class.java) != null)
                GLog.w(Messages.get(this, "blinded"))
            else if (!isEquipped(hero))
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (charge == 0)
                GLog.i(Messages.get(this, "no_charge"))
            else if (cursed)
                GLog.i(Messages.get(this, "cursed"))
            else {
                val (first, second) = hero.canRead()
                if (!first)
                    GLog.n(second)
                else {
                    charge--

                    var scroll: Scroll?
                    do {
                        scroll = Generator.SCROLL.generate() as Scroll
                    } while (scroll == null ||
                            //gotta reduce the rate on these scrolls or that'll be all
                            // the item does.
                            (scroll is ScrollOfIdentify ||
                                    scroll is ScrollOfRemoveCurse ||
                                    scroll is ScrollOfMagicMapping) && Random.Int(2) == 0)

                    scroll.ownedByBook = true
                    scroll.execute(hero, AC_READ)
                }
            }

        } else if (action == AC_ADD) {
            GameScene.selectItem(itemSelector, mode, Messages.get(this, "prompt"))
        }
    }

    override fun passiveBuff(): ArtifactBuff? = bookRecharge()

    override fun upgrade(): Item {
        super.upgrade()

        chargeCap = (level() + 1) / 2 + 3
        if (level() >= levelCap)
            scrolls.clear()

        return this
    }

    override fun desc(): String {
        var desc = super.desc()

        if (cursed && isEquipped(Dungeon.hero)) {
            desc += "\n\n" + Messages.get(this, "desc_cursed")
        }

        if (level() < levelCap)
            if (scrolls.size > 0) {
                desc += "\n\n" + Messages.get(this, "desc_index")
                var i = 0
                while (i < 3 && i < scrolls.size) {
                    desc += "\n" + Messages.get(scrolls[i], "name")
                    ++i
                }
            }

        return desc
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(SCROLLS, scrolls.toTypedArray())
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        scrolls.clear()
        scrolls.addAll(bundle.getClassArray(SCROLLS).map { it as Class<out Scroll> })
    }

    inner class bookRecharge : Artifact.ArtifactBuff() {
        override fun act(): Boolean {
            val lock = target.buff(LockedFloor::class.java)
            if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
                partialCharge += 1 / (150f - (chargeCap - charge) * 15f)

                if (partialCharge >= 1) {
                    partialCharge--
                    charge++

                    if (charge == chargeCap) {
                        partialCharge = 0f
                    }
                }
            }

            updateQuickslot()

            spend(TICK)

            return true
        }
    }

    companion object {
        private const val AC_READ = "READ"
        private const val AC_ADD = "ADD"

        private const val SCROLLS = "scrolls"
    }
}
