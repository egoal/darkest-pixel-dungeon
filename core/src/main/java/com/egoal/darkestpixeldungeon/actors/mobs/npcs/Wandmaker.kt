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
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.quest.CorpseDust
import com.egoal.darkestpixeldungeon.items.quest.Embers
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.MassGraveDigger
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.RitualSiteDigger
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.RotGardenDigger
import com.egoal.darkestpixeldungeon.windows.WndWandmaker
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.quest.CeremonialCandle
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Rotberry
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.WandmakerSprite
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Wandmaker : NPC.Unbreakable() {

    init {
        spriteClass = WandmakerSprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun reset(): Boolean = true

    override fun interact(): Boolean {

        sprite.turnTo(pos, Dungeon.hero.pos)
        if (Quest.given) {

            val item: Item? = Dungeon.hero.belongings.getItem(when (Quest.type) {
                1 -> CorpseDust::class.java
                2 -> Embers::class.java
                3 -> Rotberry.Seed::class.java
                else -> CorpseDust::class.java
            })

            if (item != null) {
                GameScene.show(WndWandmaker(this, item))
            } else {
                var msg = ""
                when (Quest.type) {
                    1 -> msg = M.L(this, "reminder_dust", Dungeon.hero.givenName())
                    2 -> msg = M.L(this, "reminder_ember", Dungeon.hero.givenName())
                    3 -> msg = M.L(this, "reminder_berry", Dungeon.hero.givenName())
                }
                GameScene.show(WndQuest(this, msg))
            }

        } else {
            var msg1 = ""
            var msg2 = ""
            msg1 += when (Dungeon.hero.heroClass) {
                HeroClass.WARRIOR -> M.L(this, "intro_warrior")
                HeroClass.ROGUE -> M.L(this, "intro_rogue")
                HeroClass.MAGE -> M.L(this, "intro_mage", Dungeon.hero.givenName())
                HeroClass.HUNTRESS -> M.L(this, "intro_huntress")
                HeroClass.SORCERESS -> M.L(this, "intro_sorceress")
                HeroClass.EXILE -> M.L(this, "intro_exile")
            }
            msg1 += M.L(this, "intro_1")

            when (Quest.type) {
                1 -> msg2 += M.L(this, "intro_dust")
                2 -> msg2 += M.L(this, "intro_ember")
                3 -> msg2 += M.L(this, "intro_berry")
            }
            msg2 += M.L(this, "intro_2")

            GameScene.show(object : WndQuest(this, msg1) {
                override fun hide() {
                    super.hide()
                    GameScene.show(WndQuest(this@Wandmaker, msg2))
                }
            })

            Journal.add(name)
            Quest.given = true
        }

        return false
    }

    object Quest {
        var type: Int = 0
        // 1 = corpse dust quest
        // 2 = elemental embers quest
        // 3 = rotberry quest

        var spawned: Boolean = false

        var given: Boolean = false

        var wand1: Wand? = null
        var wand2: Wand? = null

        private const val NODE = "wandmaker"

        private const val SPAWNED = "spawned"
        private const val TYPE = "type"
        private const val GIVEN = "given"
        private const val WAND1 = "wand1"
        private const val WAND2 = "wand2"

        private const val RITUALPOS = "ritualpos"

        fun reset() {
            spawned = false
            type = 0

            wand1 = null
            wand2 = null
        }

        fun storeInBundle(bundle: Bundle) {
            val node = Bundle()

            node.put(SPAWNED, spawned)
            if (spawned) {
                node.put(TYPE, type)
                node.put(GIVEN, given)

                node.put(WAND1, wand1)
                node.put(WAND2, wand2)

                if (type == 2) {
                    node.put(RITUALPOS, CeremonialCandle.ritualPos)
                }

            }

            bundle.put(NODE, node)
        }

        fun restoreFromBundle(bundle: Bundle) {

            val node = bundle.getBundle(NODE)

            if (node.isNull) reset()
            else {
                spawned = node.getBoolean(SPAWNED)
                if (!spawned) reset()
                else {
                    type = node.getInt(TYPE)

                    given = node.getBoolean(GIVEN)

                    wand1 = node.get(WAND1) as Wand?
                    wand2 = node.get(WAND2) as Wand?

                    if (type == 2) {
                        CeremonialCandle.ritualPos = node.getInt(RITUALPOS)
                    }
                }
            }
        }

        // new spawn function, in two stages!
        fun GiveDigger(): Digger? {
            if (!spawned && (type != 0 || Dungeon.depth > 6 && Random.Int(10 - Dungeon.depth) == 0)) {
                // now spawn
                if (type == 0) type = Random.Int(3) + 1

                // give digger
                return when (type) {
                    1 -> MassGraveDigger()
                    2 -> RitualSiteDigger()
                    3 -> RotGardenDigger()
                    else -> null
                }

                // remember to add wand maker outside!
            }

            return null
        }

        fun Spawn(level: Level, rect: Rect) {
            val w = Wandmaker()
            do {
                w.pos = level.pointToCell(rect.random(0))
            } while (level.map[w.pos] == Terrain.ENTRANCE || level.map[w.pos] == Terrain.DOOR ||
                    Terrain.flags[level.map[w.pos]] and Terrain.PASSABLE == 0)
            level.mobs.add(w)

            spawned = true
            given = false
            wand1 = (Generator.WAND.generate() as Wand).apply {
                cursed = false
                identify()
                upgrade()
            }

            do {
                wand2 = Generator.WAND.generate() as Wand
            } while (wand2!!.javaClass == wand1!!.javaClass)
            wand2!!.apply {
                cursed = false
                identify()
                upgrade()
            }
        }

        //
        fun complete() {
            wand1 = null
            wand2 = null

            Journal.remove(M.L(Wandmaker::class.java, "name"))
        }
    }
}
