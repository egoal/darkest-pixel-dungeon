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

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random

//* check in Hero::takeDamage
class CapeOfThorns : Artifact() {

    init {
        image = ItemSpriteSheet.ARTIFACT_CAPE

        levelCap = 10

        charge = 0
        chargeCap = 100
        cooldown = 0

        defaultAction = "NONE" //so it can be quickslotted
    }

    override fun passiveBuff(): Artifact.ArtifactBuff {
        return Thorns()
    }

    override fun desc(): String {
        var desc = Messages.get(this, "desc")
        if (isFullyUpgraded) desc += "\n" + Messages.get(this, "desc_max")

        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n"
            if (cooldown == 0)
                desc += Messages.get(this, "desc_inactive")
            else
                desc += Messages.get(this, "desc_active")
        }

        return desc
    }

    inner class Thorns : Artifact.ArtifactBuff() {

        override fun act(): Boolean {
            if (cooldown > 0) {
                cooldown--
                if (cooldown == 0) {
                    BuffIndicator.refreshHero()
                    GLog.w(Messages.get(this, "inert"))
                }
                updateQuickslot()
            }
            spend(Actor.TICK)
            return true
        }

        fun proc(dmg: Damage): Damage {
            val hero = dmg.to as Hero
            if (hero.STR() < 16) return dmg

            if (cooldown == 0) {
                // getting charged
                charge += (dmg.value * (.5 + level() * .05)).toInt()
                if (charge >= chargeCap) {
                    charge = 0
                    cooldown = 10 + level()
                    GLog.p(Messages.get(this, "radiating"))
                    BuffIndicator.refreshHero()
                }
            } else {
                // has the buff
                val deflected = Random.NormalIntRange(1, dmg.value)
                dmg.value -= deflected
                if (Random.Float() < hero.criticalChance()) {
                    dmg.addFeature(Damage.Feature.CRITICAL)
                    dmg.value = dmg.value * 3 / 2
                }

                if (dmg.from is Mob) {
                    val mob = dmg.from as Mob
                    if (isFullyUpgraded || Dungeon.level.adjacent(mob.pos, hero.pos))
                        mob.takeDamage(Damage(deflected, dmg.to, dmg.from))
                }

                exp += deflected
                val requireExp = (level() + 1) * 5
                if (exp >= requireExp && level() < levelCap) {
                    exp -= requireExp
                    upgrade()
                    GLog.p(Messages.get(this, "levelup"))
                }
            }

            updateQuickslot()

            return dmg
        }

        override fun toString(): String {
            return Messages.get(this, "name")
        }

        override fun desc(): String {
            return Messages.get(this, if (isFullyUpgraded) "desc_max" else "desc", dispTurns(cooldown.toFloat()))
        }

        override fun icon(): Int {
            return if (cooldown == 0)
                BuffIndicator.NONE
            else
                BuffIndicator.THORNS
        }

        override fun detach() {
            cooldown = 0
            charge = 0
            super.detach()
        }

    }


}
