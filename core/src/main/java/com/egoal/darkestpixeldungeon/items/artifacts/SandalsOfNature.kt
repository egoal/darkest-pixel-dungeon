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

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.EarthParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Earthroot
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import java.util.*

class SandalsOfNature : Artifact() {
    var seeds = ArrayList<Class<*>>()
    private var fixed = false //fixme:

    private var itemSelector: WndBag.Listener = WndBag.Listener { item ->
        if (item != null && item is Plant.Seed) {
            if (seeds.contains(item.javaClass)) {
                GLog.w(Messages.get(SandalsOfNature::class.java, "already_fed"))
            } else {
                seeds.add(item.javaClass)

                val hero = Dungeon.hero
                hero.sprite.operate(hero.pos)
                Sample.INSTANCE.play(Assets.SND_PLANT)
                hero.busy()
                hero.spend(2f)
                if (seeds.size >= 3 + level() * 3) {
                    seeds.clear()
                    upgrade()
                    if (level() in 1..3) {
                        GLog.p(Messages.get(SandalsOfNature::class.java, "levelup"))
                    }

                } else {
                    GLog.i(Messages.get(SandalsOfNature::class.java, "absorb_seed"))
                }
                item.detach(hero.belongings.backpack)
            }
        }
    }

    init {
        image = ItemSpriteSheet.ARTIFACT_SANDALS

        levelCap = 3

        charge = 0

        defaultAction = AC_ROOT
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && level() < 3 && !cursed) actions.add(AC_FEED)
        if (isEquipped(hero)) actions.add(AC_ROOT)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_FEED) {
            GameScene.selectItem(itemSelector, M.L(this, "prompt"), WndBag.Filter {
                it is Plant.Seed && !seeds.contains(it.javaClass)
            })
        } else if (action == AC_ROOT && level() > 0) {
            if (!isEquipped(hero))
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (hero.buff(Rooted::class.java) != null) {
                val rooted = hero.buff(Rooted::class.java)!!
                charge = rooted.level * 9 / 10
                Buff.detach(rooted)
                updateQuickslot()
            } else if (charge == 0)
                GLog.i(Messages.get(this, "no_charge"))
            else {
//                Buff.prolong(hero, Roots::class.java, 5f)
//                Buff.affect(hero, Earthroot.Armor::class.java).level(charge)
                Buff.affect(hero, Rooted::class.java).level(charge)
                CellEmitter.bottom(hero.pos).start(EarthParticle.FACTORY, 0.05f, 8)
                Camera.main.shake(1f, 0.4f)
                charge = 0
                updateQuickslot()
            }
        }
    }

    override fun passiveBuff(): Artifact.ArtifactBuff {
        return Naturalism()
    }

    override fun desc(): String {
        var desc = Messages.get(this, "desc_" + (level() + 1))

        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n"

            if (!cursed)
                desc += Messages.get(this, "desc_hint")
            else
                desc += Messages.get(this, "desc_cursed")

            if (level() > 0)
                desc += "\n\n" + Messages.get(this, "desc_ability")
        }

        if (!seeds.isEmpty()) {
            desc += "\n\n" + Messages.get(this, "desc_seeds", seeds.size)
        }

        return desc
    }

    override fun upgrade(): Item {
        updateImage()
        name = Messages.get(this, "name_" + (level() + 1))
        super.upgrade()

        // get to level 10 when equipped, trigger modification
        if (isFullyUpgraded && passiveBuff != null) fixOn(Dungeon.hero)

        return this
    }

    private fun updateImage() {
        image = when (level()) {
            0 -> ItemSpriteSheet.ARTIFACT_SHOES
            1 -> ItemSpriteSheet.ARTIFACT_BOOTS
            2, 3 -> ItemSpriteSheet.ARTIFACT_GREAVES
            else -> ItemSpriteSheet.ARTIFACT_SANDALS
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(SEEDS, seeds.toTypedArray())
        bundle.put(FIX_ON, fixed)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        if (level() > 0) name = Messages.get(this, "name_" + level())

        seeds.addAll(bundle.getClassArray(SEEDS))
        fixed = bundle.getBoolean(FIX_ON)

        updateImage()
    }

    private fun fixOn(ch: Char) {
        if (!fixed) {
            fixed = true
            for (i in ch.elementalResistance.indices) ch.elementalResistance[i] += .1f
        }
    }

    private fun fixOff(ch: Char) {
        if (fixed) {
            fixed = false
            for (i in ch.elementalResistance.indices) ch.elementalResistance[i] -= .1f
        }
    }

    inner class Naturalism : Artifact.ArtifactBuff() {
        fun charge() {
            if (level() > 0 && charge < target.HT) {
                //gain 1+(1*level)% of the difference between current charge and max HP.
                charge += Math.round((target.HT - charge) * (.01 + level() * 0.01)).toInt()
                updateQuickslot()
            }
        }

        override fun attachTo(target: Char): Boolean {
            return if (super.attachTo(target)) {
                if (isFullyUpgraded) fixOn(target)
                true
            } else false
        }

        override fun detach() {
            if (isFullyUpgraded) fixOff(target)

            target.buff(Rooted::class.java)?.let {
                charge = it.level * 9 / 10
                it.detach()
            }

            super.detach()
        }
    }

    class Rooted : Earthroot.Armor() {
        init {
            type = buffType.POSITIVE
        }

        override fun attachTo(target: Char): Boolean {
            target.rooted = true
            return super.attachTo(target)
        }

        override fun detach() {
            target.rooted = false
            super.detach()
        }

        override fun icon(): Int = BuffIndicator.ROOT_ARMOR

        override fun heroMessage(): String? = M.L(this, "heromsg")
    }

    companion object {
        const val AC_FEED = "FEED"
        const val AC_ROOT = "ROOT"

        private const val SEEDS = "seeds"
        private const val FIX_ON = "fix_on"
    }

}
