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
package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.mobs.GreatCrab
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.buffs.Roots
import com.egoal.darkestpixeldungeon.actors.mobs.FetidRat
import com.egoal.darkestpixeldungeon.actors.mobs.GnollTrickster
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.armor.LeatherArmor
import com.egoal.darkestpixeldungeon.items.armor.MailArmor
import com.egoal.darkestpixeldungeon.items.armor.PlateArmor
import com.egoal.darkestpixeldungeon.items.armor.ScaleArmor
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.GhostSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.lang.RuntimeException

import java.util.HashSet

class Ghost : NPC.Unbreakable() {
    init {
        spriteClass = GhostSprite::class.java

        flying = true

        state = WANDERING
    }

    override fun act(): Boolean {
        if (Quest.completed())
            target = Dungeon.hero.pos
        return super.act()
    }

    override fun speed(): Float {
        return if (Quest.completed()) 2f else 0.5f
    }

    override fun chooseEnemy(): Char? = null

    override fun reset(): Boolean = true

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        Sample.INSTANCE.play(Assets.SND_GHOST)

        if (Quest.given) {
            if (Quest.weapon != null) {
                if (Quest.processed) {
                    val content = when (Quest.type) {
                        1 -> M.L(this, "rat")
                        2 -> M.L(this, "gnoll")
                        3 -> M.L(this, "crab")
                        else -> throw RuntimeException("cannot be here")
                    } + M.L(this, "give_item")
                    WndDialogue.Show(this, content,
                            M.L(this, "weapon", Quest.weapon!!.name()),
                            M.L(this, "armor", Quest.armor!!.name())) {
                        val reward = (if (it == 0) Quest.weapon else Quest.armor)!!

                        if (reward.doPickUp(Dungeon.hero)) GLog.i(M.L(Dungeon.hero, "you_now_have", reward.name()))
                        else Dungeon.level.drop(reward, this@Ghost.pos).sprite.drop()

                        yell(M.L(this@Ghost, "farewell"))
                        die(null)
                        Quest.complete()
                    }
                } else {
                    when (Quest.type) {
                        1 -> GameScene.show(WndQuest(this, Messages.get(this, "rat_2")))
                        2 -> GameScene.show(WndQuest(this, Messages.get(this, "gnoll_2")))
                        3 -> GameScene.show(WndQuest(this, Messages.get(this, "crab_2")))
                        else -> GameScene.show(WndQuest(this, Messages.get(this, "rat_2")))
                    }

                    var newPos = -1
                    for (i in 0..9) {
                        newPos = Dungeon.level.randomRespawnCell()
                        if (newPos != -1) {
                            break
                        }
                    }
                    if (newPos != -1) {

                        CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3)
                        pos = newPos
                        sprite.place(pos)
                        sprite.visible = Dungeon.visible[pos]
                    }
                }
            }
        } else {
            val questBoss: Mob
            val txt_quest: String

            when (Quest.type) {
                1 -> {
                    questBoss = FetidRat()
                    txt_quest = Messages.get(this, "rat_1", Dungeon.hero.givenName())
                }
                2 -> {
                    questBoss = GnollTrickster()
                    txt_quest = Messages.get(this, "gnoll_1", Dungeon.hero.givenName())
                }
                3 -> {
                    questBoss = GreatCrab()
                    txt_quest = Messages.get(this, "crab_1", Dungeon.hero.givenName())
                }
                else -> {
                    questBoss = FetidRat()
                    txt_quest = Messages.get(this, "rat_1", Dungeon.hero.givenName())
                }
            }

            questBoss.initialize()
            questBoss.pos = Dungeon.level.randomRespawnCell()

            if (questBoss.pos != -1) {
                GameScene.add(questBoss)
                GameScene.show(WndQuest(this, txt_quest))
                Quest.given = true
                Journal.add(name)
            }

        }

        return false
    }

    override fun immunizedBuffs(): HashSet<Class<*>> {
        return IMMUNITIES
    }

    object Quest {

        var spawned: Boolean = false

        var type: Int = 0

        var given: Boolean = false
        var processed: Boolean = false

        private var depth: Int = 0

        var weapon: Weapon? = null
        var armor: Armor? = null

        fun reset() {
            spawned = false

            weapon = null
            armor = null
        }

        fun storeInBundle(bundle: Bundle) {

            val node = Bundle()

            node.put(SPAWNED, spawned)

            if (spawned) {

                node.put(TYPE, type)

                node.put(GIVEN, given)
                node.put(DEPTH, depth)
                node.put(PROCESSED, processed)

                node.put(WEAPON, weapon)
                node.put(ARMOR, armor)
            }

            bundle.put(NODE, node)
        }

        fun restoreFromBundle(bundle: Bundle) {

            val node = bundle.getBundle(NODE)

            spawned = node?.getBoolean(SPAWNED) == true
            if (spawned) {

                type = node.getInt(TYPE)
                given = node.getBoolean(GIVEN)
                processed = node.getBoolean(PROCESSED)

                depth = node.getInt(DEPTH)

                weapon = node.get(WEAPON) as Weapon?
                armor = node.get(ARMOR) as Armor?
            } else {
                reset()
            }
        }

        fun Spawn(level: Level) {
            if (spawned || Dungeon.depth <= 1 || Random.Int(5 - Dungeon.depth) != 0)
                return

            // spawn
            val ghost = Ghost()
            do {
                ghost.pos = level.randomRespawnCell()
            } while (ghost.pos == -1)
            level.mobs.add(ghost)

            spawned = true
            // 2: fetid rat, 3: gnoll trickster, 4: great crab
            type = Dungeon.depth - 1

            given = false
            processed = false
            depth = Dungeon.depth

            PreparePrize()
        }

        fun process() {
            if (spawned && given && !processed && depth == Dungeon.depth) {
                GLog.n(Messages.get(Ghost::class.java, "find_me"))
                Sample.INSTANCE.play(Assets.SND_GHOST)
                processed = true
                // now the rose can spawn.
                Generator.ARTIFACT.probMap[DriedRose::class] = 1f
            }
        }

        fun complete() {
            weapon = null
            armor = null

            Journal.remove(Messages.get(Ghost::class.java, "name"))
        }

        fun completed(): Boolean = spawned && processed

        private fun PreparePrize() {
            val itemTierRoll = Random.Float()
            val wepTier: Int

            // %10, %35, %30, %20, %5
            when {
                itemTierRoll < 0.1f -> {
                    wepTier = 1
                    armor = LeatherArmor()
                }
                itemTierRoll < 0.45f -> {
                    wepTier = 2
                    armor = LeatherArmor()
                }
                itemTierRoll < 0.75f -> {
                    wepTier = 3
                    armor = MailArmor()
                }
                itemTierRoll < 0.95f -> {
                    wepTier = 4
                    armor = ScaleArmor()
                }
                else -> {
                    wepTier = 5
                    armor = PlateArmor()
                }
            }

            do {
                weapon = Generator.WEAPON.MELEE.tier(wepTier).generate() as Weapon
            } while (weapon !is MeleeWeapon)
            weapon!!.level(0)
            weapon!!.cursed = false
            weapon!!.inscribe(null)

            //40%:+0, 40%:+1, 15%:+2, 5%:+3
            val itemLevelRoll = Random.Float()
            val itemLevel = when {
                itemLevelRoll < 0.4f -> 0
                itemLevelRoll < 0.8f -> 1
                itemLevelRoll < 0.95f -> 2
                else -> 3
            }
            weapon!!.upgrade(itemLevel)
            armor!!.upgrade(itemLevel)

            //10% to be enchanted
            if ((weapon as MeleeWeapon).tier == 1 || Random.Int(10) == 0) weapon!!.inscribe()
            if (Random.Int(10) == 0) armor!!.inscribe()

            weapon!!.identify()
            armor!!.identify()
        }
    }

    companion object {
        private const val NODE = "sadGhost"

        private const val SPAWNED = "spawned"
        private const val TYPE = "type"
        private const val GIVEN = "given"
        private const val PROCESSED = "processed"
        private const val DEPTH = "depth"
        private const val WEAPON = "weapon"
        private const val ARMOR = "armor"

        private val IMMUNITIES = hashSetOf<Class<*>>(Paralysis::class.java, Roots::class.java)
    }
}
