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
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.unclassified.DewVial
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.ArrayList
import kotlin.math.min

class ChaliceOfBlood : Artifact() {

    init {
        image = ItemSpriteSheet.ARTIFACT_CHALICE1

        levelCap = 10
        defaultAction = AC_DRINK
    }

    private var volume = 0f

    private val MaxVolume: Float get() = level() + 5f

    override fun status(): String = "${volume.toInt()}/${MaxVolume.toInt()}"

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && !cursed && volume >= 1f)
            actions.add(AC_DRINK)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_DRINK) {
            if (!isEquipped(hero)) GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (volume < 1f)
                GLog.i(Messages.get(this, "empty"))
            else {
                // consume 
                consume(hero)
            }
        }
    }

    private fun consume(hero: Hero) {
        // like dew vial, but more powerful
        val dp = hero.HT.toFloat() * 0.1f + 1f
        val need = ((hero.HT - hero.HP).toFloat() / dp).toInt()
        val consumed = if (volume > need) need else volume.toInt()

        val effect = min(hero.HT - hero.HP, consumed * dp.toInt())
        with(hero) {
            HP += effect
            sprite.emitter().burst(Speck.factory(Speck.HEALING), if (consumed > 5) 2 else 1)
            sprite.showStatus(CharSprite.POSITIVE, Messages.get(DewVial::class.java, "value", effect))

            spend(TIME_TO_DRINK)
            sprite.operate(pos)
            busy()
        }
        Sample.INSTANCE.play(Assets.SND_DRINK)

        // cost
        volume -= consumed

        updateSprite()
        updateQuickslot()
    }

    // sprite 
    private fun updateSprite() {
        val ratio = volume / MaxVolume
        image = when {
            (ratio < 0.1f) -> ItemSpriteSheet.ARTIFACT_CHALICE1
            (ratio < 0.5f) -> ItemSpriteSheet.ARTIFACT_CHALICE2
            else -> ItemSpriteSheet.ARTIFACT_CHALICE3
        }
    }

    // dont remove curse
    override fun upgrade(): Item {
        val isCursed = cursed
        return super.upgrade().apply { cursed = isCursed }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(VOLUME, volume)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        volume = bundle.getFloat(VOLUME)
        updateSprite()
    }

    override fun passiveBuff(): ArtifactBuff = Store()

    override fun desc(): String {
        var desc = super.desc()

        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n" + Messages.get(this, "desc_hint")

            if (cursed) desc += "\n\n" + Messages.get(this, "desc_cursed")
        }

        return desc
    }

    inner class Store : Artifact.ArtifactBuff() {
        fun onEnemySlayed(ch: Char) {
            var d = if (ch is Mob && ch.exp() > 0) Random.Float(0.2f, 0.8f) else 0.1f
            if (cursed) d += 0.1f
            volume = min(volume + d, MaxVolume)

            // gain exp on kill mobs
            if (ch is Mob && ch.exp() > 0 && level() < levelCap) {
                exp += Math.round(ch.exp().toFloat() / Dungeon.hero.maxExp().toFloat() * 100f) // according to level percent
                if (exp > 100 + level() * 20) {
                    exp -= 100 + level() * 20
                    GLog.p(Messages.get(ChaliceOfBlood::class.java, "levelup"))
                    upgrade()
                }
            }

            updateSprite()
            updateQuickslot()
        }
    }

    companion object {
        private const val AC_DRINK = "drink"
        private const val VOLUME = "volume"

        private const val TIME_TO_DRINK = 1f
    }

}
