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
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Chains
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Callback
import com.watabou.utils.Random
import java.util.*
import kotlin.math.round

class EtherealChains : Artifact() {

    private val caster = object : CellSelector.Listener {

        override fun onSelect(target: Int?) {
            if (target != null && (Dungeon.level.visited[target] || Dungeon.level.mapped[target])) {

                // ballistica does not go through walls on pre-rework boss arenas
                // egoal: no, ur free now
                //        int missileProperties = Dungeon.bossLevel() ? Ballistica.PROJECTILE :
                //                Ballistica.STOP_CHARS | Ballistica.STOP_TARGET;
                val missileProperties = Ballistica.STOP_CHARS or Ballistica.STOP_TARGET

                val chain = Ballistica(curUser.pos, target, missileProperties)

                //determine if we're grabbing an enemy, pulling to a location, or
                // doing nothing.
                if (Actor.findChar(chain.collisionPos) != null) {
                    val pulltarget = Actor.findChar(chain.collisionPos)!!

                    val props = pulltarget.properties()
                    val pullhero = props.contains(Char.Property.IMMOVABLE) || props.contains(Char.Property.BOSS) ||
                            props.contains(Char.Property.STATIC) || props.contains(Char.Property.HEAVY)

                    val newpos = if (pullhero) {
                        var pos = -1
                        for (i in chain.subPath(0, chain.dist - 1).reversed()) {
                            if (!Level.solid[i] && Actor.findChar(i) == null) {
                                pos = i;
                                break
                            }
                        }
                        pos
                    } else {
                        var pos = -1
                        for (i in chain.subPath(1, chain.dist)) {
                            if (!Level.solid[i] && Actor.findChar(i) == null) {
                                pos = i
                                break
                            }
                        }
                        pos
                    }

                    // cannot go there
                    if (newpos == -1) {
                        GLog.w(Messages.get(EtherealChains::class.java, "does_nothing"))
                        return
                    }

                    val affected = if (pullhero) curUser else pulltarget

                    val chargeUse = Dungeon.level.distance(affected.pos, newpos)
                    if (chargeUse > charge) {
                        GLog.w(M.L(EtherealChains::class.java, "no_charge"))
                        return
                    }

                    charge -= chargeUse
                    updateQuickslot()

                    curUser.busy()
                    curUser.sprite.parent.add(Chains(curUser.pos, pulltarget.pos, Callback {
                        Actor.add(Pushing(affected, affected.pos, newpos, Callback { Dungeon.level.press(newpos, affected) }))
                        affected.pos = newpos
                        if (pullhero) {
                            Wound.hit(pulltarget.pos)
                            curUser.sprite.turnTo(affected.pos, pulltarget.pos)
                            Char.ProcessAttackDamage(affected.giveDamage(pulltarget).addFeature(Damage.Feature.ACCURATE))
                        }
                        else Buff.prolong(affected, Cripple::class.java, 2f + level() / 2f)
                        Dungeon.observe()
                        GameScene.updateFog()
                        curUser.spendAndNext(1f)
                    }))

                } else if (Level.solid[chain.path[chain.dist]]
                        || chain.dist > 0 && Level.solid[chain.path[chain.dist - 1]]
                        || chain.path.size > chain.dist + 1 && Level.solid[chain
                                .path[chain.dist + 1]]
                        //if the player is trying to grapple the edge of the map, let
                        // them.
                        || chain.path.size == chain.dist + 1) {
                    var newPos = -1
                    for (i in chain.subPath(1, chain.dist)) {
                        if (!Level.solid[i] && Actor.findChar(i) == null) newPos = i
                    }
                    if (newPos == -1) {
                        GLog.w(Messages.get(EtherealChains::class.java, "does_nothing"))
                    } else {
                        val newHeroPos = newPos
                        val chargeUse = Dungeon.level.distance(curUser.pos, newHeroPos)
                        if (chargeUse > charge) {
                            GLog.w(Messages.get(EtherealChains::class.java, "no_charge"))
                            return
                        } else {
                            charge -= chargeUse
                            updateQuickslot()
                        }
                        curUser.busy()
                        curUser.sprite.parent.add(Chains(curUser.pos, target, Callback {
                            Actor.add(Pushing(curUser, curUser.pos, newHeroPos, Callback { Dungeon.level.press(newHeroPos, curUser) }))
                            curUser.spendAndNext(1f)
                            curUser.pos = newHeroPos
                            Dungeon.observe()
                            GameScene.updateFog()
                        }))
                    }
                } else {
                    GLog.i(Messages.get(EtherealChains::class.java, "nothing_to_grab"))
                }

            }
        }

        override fun prompt(): String {
            return Messages.get(EtherealChains::class.java, "prompt")
        }
    }

    init {
        image = ItemSpriteSheet.ARTIFACT_CHAINS

        levelCap = 5
        exp = 0

        charge = 5

        defaultAction = AC_CAST
        usesTargeting = true
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && charge > 0 && !cursed)
            actions.add(AC_CAST)
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action == AC_CAST) {
            curUser = hero

            if (!isEquipped(hero)) {
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
                QuickSlotButton.cancel()

            } else if (charge < 1) {
                GLog.i(Messages.get(this, "no_charge"))
                QuickSlotButton.cancel()

            } else if (cursed) {
                GLog.w(Messages.get(this, "cursed"))
                QuickSlotButton.cancel()

            } else {
                GameScene.selectCell(caster)
            }

        }
    }

    override fun passiveBuff() = chainsRecharge()

    override fun desc(): String {
        var desc = super.desc()

        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n"
            if (cursed)
                desc += Messages.get(this, "desc_cursed")
            else
                desc += Messages.get(this, "desc_equipped")
        }
        return desc
    }

    inner class chainsRecharge : Artifact.ArtifactBuff() {

        override fun act(): Boolean {
            val chargeTarget = 5 + level() * 2
            val lock = target.buff(LockedFloor::class.java)
            if (charge < chargeTarget && !cursed && (lock == null || lock.regenOn())) {
                partialCharge += 1 / (40f - (chargeTarget - charge) * 2f)
            } else if (cursed && Random.Int(100) == 0) {
                Buff.prolong(target, Cripple::class.java, 10f)
            }

            if (partialCharge >= 1) {
                partialCharge--
                charge++
            }

            updateQuickslot()

            spend(TICK)

            return true
        }

        fun gainExp(levelPortion: Float) {
            var levelPortion = levelPortion
            if (cursed) return

            exp += round(levelPortion * 100).toInt()

            //past the soft charge cap, gaining  charge from leveling is slowed.
            if (charge > 5 + level() * 2) {
                levelPortion *= (5 + level().toFloat() * 2) / charge
            }
            partialCharge += levelPortion * 10f

            if (exp > 100 + level() * 50 && level() < levelCap) {
                exp -= 100 + level() * 50
                GLog.p(Messages.get(this, "levelup"))
                upgrade()
            }

        }
    }

    companion object {
        const val AC_CAST = "CAST"
    }
}
