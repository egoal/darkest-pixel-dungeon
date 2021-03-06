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
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Roots
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.EarthParticle
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Earthroot
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle

import java.util.ArrayList
import java.util.Collections

class SandalsOfNature : Artifact() {
    private var mode: WndBag.Mode = WndBag.Mode.SEED

    var seeds = ArrayList<Class<*>>()

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
        if (isEquipped(hero) && level() < 3 && !cursed)
            actions.add(AC_FEED)
        if (isEquipped(hero) && charge > 0)
            actions.add(AC_ROOT)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_FEED) {

            GameScene.selectItem(itemSelector, mode, Messages.get(this, "prompt"))

        } else if (action == AC_ROOT && level() > 0) {

            if (!isEquipped(hero))
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (charge == 0)
                GLog.i(Messages.get(this, "no_charge"))
            else {
                Buff.prolong(hero, Roots::class.java, 5f)
                Buff.affect(hero, Earthroot.Armor::class.java).level(charge)
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
        return super.upgrade()
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
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        if (level() > 0) name = Messages.get(this, "name_" + level())

        seeds.addAll(bundle.getClassArray(SEEDS))

        updateImage()
    }

    inner class Naturalism : Artifact.ArtifactBuff() {
        fun charge() {
            if (level() > 0 && charge < target.HT) {
                //gain 1+(1*level)% of the difference between current charge and max HP.
                charge += Math.round((target.HT - charge) * (.01 + level() * 0.01)).toInt()
                updateQuickslot()
            }
        }
    }

    companion object {
        const val AC_FEED = "FEED"
        const val AC_ROOT = "ROOT"

        private const val SEEDS = "seeds"
    }

}
