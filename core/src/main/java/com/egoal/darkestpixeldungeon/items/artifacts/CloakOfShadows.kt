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
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.unclassified.GreatBlueprint
import com.egoal.darkestpixeldungeon.items.unclassified.HasteRune
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.tweeners.AlphaTweener
import com.watabou.utils.Bundle
import java.util.*

class CloakOfShadows : Artifact(), GreatBlueprint.Enchantable {

    private var stealthed = false
    var enhanced = false

    init {
        image = ItemSpriteSheet.ARTIFACT_CLOAK

        exp = 0
        levelCap = 14

        charge = level() + 6
        partialCharge = 0f
        chargeCap = level() + 6

        cooldown = 0

        defaultAction = AC_STEALTH

        unique = true
        bones = false
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && charge > 1)
            actions.add(AC_STEALTH)
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_STEALTH) {

            if (!stealthed) {
                if (!isEquipped(hero))
                    GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
                else if (cooldown > 0)
                    GLog.i(Messages.get(this, "cooldown", cooldown))
                else if (charge <= 1)
                    GLog.i(Messages.get(this, "no_charge"))
                else {
                    stealthed = true
                    hero.spend(if (isFullyUpgraded) .01f else 1f)
                    hero.busy()
                    Sample.INSTANCE.play(Assets.SND_MELD)
                    activeBuff = activeBuff()
                    activeBuff!!.attachTo(hero)
                    if (hero.sprite.parent != null) {
                        hero.sprite.parent.add(AlphaTweener(hero.sprite, 0.4f, 0.4f))
                    } else {
                        hero.sprite.alpha(0.4f)
                    }
                    hero.sprite.operate(hero.pos)
                }
            } else {
                stealthed = false
                activeBuff!!.detach()
                activeBuff = null
                hero.spend(1f)
                hero.sprite.operate(hero.pos)
            }

        }
    }

    override fun activate(ch: Char) {
        super.activate(ch)
        if (stealthed) {
            activeBuff = activeBuff()
            activeBuff!!.attachTo(ch)
        }
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        if (super.doUnequip(hero, collect, single)) {
            stealthed = false
            return true
        } else
            return false
    }

    override fun passiveBuff(): Artifact.ArtifactBuff {
        return cloakRecharge()
    }

    override fun activeBuff(): Artifact.ArtifactBuff? {
        return cloakStealth()
    }

    override fun upgrade(): Item {
        chargeCap++
        return super.upgrade()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STEALTHED, stealthed)
        bundle.put(ENHANCED, enhanced)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        stealthed = bundle.getBoolean(STEALTHED)
        enhanced = bundle.getBoolean(ENHANCED)
        if (enhanced) enchantByBlueprint()

        //for pre-0.4.1 saves which may have over-levelled cloaks
        if (level() == 15) {
            level(14)
            chargeCap = 20
        }
    }

    override fun price(): Int {
        return 0
    }

    override fun enchantByBlueprint() {
        enhanced = true
        image = ItemSpriteSheet.ARTIFACT_CLOAK_ENHANCED
    }

    override fun desc(): String {
        var desc = super.desc()
        if (enhanced) desc += "\n\n" + Messages.get(this, "enhanced_desc")
        return desc
    }

    inner class cloakRecharge : Artifact.ArtifactBuff() {
        override fun act(): Boolean {
            if (charge < chargeCap) {
                val lock = target.buff(LockedFloor::class.java)
                if (!stealthed && (lock == null || lock.regenOn()))
                    partialCharge += 1f / (50 - (chargeCap - charge))

                if (partialCharge >= 1) {
                    charge++
                    partialCharge -= 1f
                    if (charge == chargeCap) {
                        partialCharge = 0f
                    }

                }
            } else
                partialCharge = 0f

            if (cooldown > 0)
                cooldown--

            updateQuickslot()

            spend(Actor.TICK)

            return true
        }

        fun enhanced(): Boolean {
            return enhanced
        }

        override fun fx(on: Boolean) {
            //      if(enhanced) {
            //        if (on) targetpos.sprite.add(CharSprite.State.BLUR);
            //        else targetpos.sprite.remove(CharSprite.State.BLUR);
            //      }
        }
    }

    inner class cloakStealth : Artifact.ArtifactBuff() {
        internal var turnsToCost = 0

        override fun icon(): Int {
            return BuffIndicator.INVISIBLE
        }

        override fun attachTo(target: Char): Boolean {
            if (super.attachTo(target)) {
                target.invisible = target.invisible + 1
                if (isFullyUpgraded) Buff.prolong(target, HasteRune.Haste::class.java, 2f)
                return true
            } else {
                return false
            }
        }

        override fun act(): Boolean {
            if (turnsToCost == 0) charge--
            if (charge <= 0) {
                detach()
                GLog.w(Messages.get(this, "no_charge"))
                (target as Hero).interrupt()
            }

            if (turnsToCost == 0) exp += 10 + (target as Hero).lvl

            if (exp >= (level() + 1) * 40 && level() < levelCap) {
                upgrade()
                exp -= level() * 40
                GLog.p(Messages.get(this, "levelup"))
            }

            if (turnsToCost == 0)
                turnsToCost = 2
            else
                turnsToCost--
            updateQuickslot()

            spend(Actor.TICK)

            return true
        }

        fun dispel() {
            charge--

            exp = exp + 10 + (target as Hero).lvl

            if (exp >= (level() + 1) * 40 && level() < levelCap) {
                upgrade()
                exp = exp - level() * 40
                GLog.p(Messages.get(this, "levelup"))
            }

            updateQuickslot()
            detach()
        }

        override fun fx(on: Boolean) {
            if (on)
                target.sprite.add(CharSprite.State.INVISIBLE)
            else if (target.invisible == 0)
                target.sprite.remove(CharSprite.State.INVISIBLE)
        }

        override fun toString(): String {
            return Messages.get(this, "name")
        }

        override fun desc(): String {
            return Messages.get(this, "desc")
        }

        override fun detach() {
            if (target.invisible > 0) target.invisible = target.invisible - 1
            stealthed = false
            cooldown = 6 - level() / 4

            updateQuickslot()
            super.detach()
        }
    }

    companion object {

        val AC_STEALTH = "STEALTH"

        private val STEALTHED = "stealthed"
        private val ENHANCED = "enhanced"
    }
}
