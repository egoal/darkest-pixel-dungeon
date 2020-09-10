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
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.items.Item
import com.watabou.noosa.Game
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Callback
import com.watabou.utils.PathFinder

import java.util.ArrayList

class LloydsBeacon : Artifact() {

    var returnDepth = -1
    var returnPos: Int = 0

    private var zapper: CellSelector.Listener = object : CellSelector.Listener {
        override fun onSelect(target: Int?) {

            if (target == null) return

            Invisibility.dispel()
            charge -= if (Dungeon.depth > 20) 2 else 1
            updateQuickslot()

            if (Actor.findChar(target) === curUser) {
                ScrollOfTeleportation.teleportHero(curUser)
                curUser.spendAndNext(1f)
            } else {
                val bolt = Ballistica(curUser.pos, target,
                        Ballistica.MAGIC_BOLT)
                val ch = Actor.findChar(bolt.collisionPos)

                if (ch === curUser) {
                    ScrollOfTeleportation.teleportHero(curUser)
                    curUser.spendAndNext(1f)
                } else {
                    Sample.INSTANCE.play(Assets.SND_ZAP)
                    curUser.sprite.zap(bolt.collisionPos)
                    curUser.busy()

                    MagicMissile.force(curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos) {
                        if (ch != null) {
                            var count = 10
                            var pos: Int
                            do {
                                pos = Dungeon.level.randomRespawnCell()
                                if (count-- <= 0) {
                                    break
                                }
                            } while (pos == -1)


                            if (pos == -1 || Dungeon.bossLevel()) {

                                GLog.w(Messages.get(ScrollOfTeleportation::class.java, "no_tele"))

                            } else if (ch.properties().contains(Char.Property.IMMOVABLE)) {

                                GLog.w(Messages.get(LloydsBeacon::class.java, "tele_fail"))

                            } else {

                                ch.pos = pos
                                if (ch is Mob && ch.state === ch.HUNTING)
                                    ch.state = ch.WANDERING

                                ch.sprite.place(ch.pos)
                                ch.sprite.visible = Dungeon.visible[pos]

                            }
                        }
                        curUser.spendAndNext(1f)
                    }

                }


            }

        }

        override fun prompt(): String {
            return Messages.get(LloydsBeacon::class.java, "prompt")
        }
    }

    init {
        image = ItemSpriteSheet.ARTIFACT_BEACON

        levelCap = 3

        charge = 0
        chargeCap = 3 + level()

        defaultAction = AC_ZAP
        usesTargeting = true
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DEPTH, returnDepth)
        if (returnDepth != -1) {
            bundle.put(POS, returnPos)
        }
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        returnDepth = bundle.getInt(DEPTH)
        returnPos = bundle.getInt(POS)
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_ZAP)
        actions.add(AC_SET)
        if (returnDepth != -1) {
            actions.add(AC_RETURN)
        }
        return actions
    }

    override fun execute(hero: Hero, action: String) {

        super.execute(hero, action)

        if (action === AC_SET || action === AC_RETURN) {

            if (Dungeon.bossLevel()) {
                hero.spend(LloydsBeacon.TIME_TO_USE)
                GLog.w(Messages.get(this, "preventing"))
                return
            }

            for (i in PathFinder.NEIGHBOURS8.indices) {
                if (Actor.findChar(hero.pos + PathFinder.NEIGHBOURS8[i]) != null) {
                    GLog.w(Messages.get(this, "creatures"))
                    return
                }
            }
        }

        if (action === AC_ZAP) {
            curUser = hero
            val chargesToUse = if (Dungeon.depth > 20) 2 else 1

            if (!isEquipped(hero)) {
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
                QuickSlotButton.cancel()

            } else if (charge < chargesToUse) {
                GLog.i(Messages.get(this, "no_charge"))
                QuickSlotButton.cancel()

            } else {
                GameScene.selectCell(zapper)
            }

        } else if (action === AC_SET) {

            returnDepth = Dungeon.depth
            returnPos = hero.pos

            hero.spend(LloydsBeacon.TIME_TO_USE)
            hero.busy()

            hero.sprite.operate(hero.pos)
            Sample.INSTANCE.play(Assets.SND_BEACON)

            GLog.i(Messages.get(this, "return"))

        } else if (action === AC_RETURN) {

            if (returnDepth == Dungeon.depth) {
                ScrollOfTeleportation.appear(hero, returnPos)
                Dungeon.level.press(returnPos, hero)
                Dungeon.observe()
                GameScene.updateFog()
            } else {

                val buff = Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze::class.java)
                buff?.detach()

                for (mob in Dungeon.level.mobs.toTypedArray())
                    if (mob is GhostHero) mob.destroy()

                InterlevelScene.mode = InterlevelScene.Mode.RETURN
                InterlevelScene.returnDepth = returnDepth
                InterlevelScene.returnPos = returnPos
                Game.switchScene(InterlevelScene::class.java)
            }


        }
    }

    override fun passiveBuff() = beaconRecharge()

    override fun upgrade(): Item {
        if (level() == levelCap) return this
        chargeCap++
        GLog.p(Messages.get(this, "levelup"))
        return super.upgrade()
    }

    override fun desc(): String {
        var desc = super.desc()
        if (returnDepth != -1) {
            desc += "\n\n" + Messages.get(this, "desc_set", returnDepth)
        }
        return desc
    }

    override fun glowing(): ItemSprite.Glowing? {
        return if (returnDepth != -1) WHITE else null
    }

    inner class beaconRecharge : Artifact.ArtifactBuff() {
        override fun act(): Boolean {
            val lock = target.buff(LockedFloor::class.java)
            if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
                partialCharge += 1 / (100f - (chargeCap - charge) * 10f)

                if (partialCharge >= 1) {
                    partialCharge--
                    charge++

                    if (charge == chargeCap) {
                        partialCharge = 0f
                    }
                }
            }

            updateQuickslot()
            spend(Actor.TICK)
            return true
        }
    }

    companion object {
        const val TIME_TO_USE = 1f

        const val AC_ZAP = "ZAP"
        const val AC_SET = "SET"
        const val AC_RETURN = "RETURN"

        private const val DEPTH = "depth"
        private const val POS = "pos"

        private val WHITE = ItemSprite.Glowing(0xFFFFFF)
    }
}
