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

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.mobs.Monk
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.windows.WndImp
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.mobs.Golem
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.quest.DwarfToken
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ImpSprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Imp : NPC.Unbreakable() {
    private var seenBefore = false

    init {
        spriteClass = ImpSprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun act(): Boolean {
        if (!Quest.given && Dungeon.visible[pos]) {
            if (!seenBefore)
                say(M.L(this, "hey", Dungeon.hero.givenName()))
            seenBefore = true
        } else {
            seenBefore = false
        }

        return super.act()
    }

    override fun reset(): Boolean = true

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)
        if (Quest.given) {

            val tokens = Dungeon.hero.belongings.getItem(DwarfToken::class.java)
            if (tokens != null && tokens.quantity() >= 8) {
                GameScene.show(WndImp(this, tokens))
            } else {
                tell(M.L(this, "quest_2", Dungeon.hero.givenName()))
            }

        } else {
            tell(M.L(this, "quest_1"))
            Quest.given = true
            Quest.isCompleted = false

            Journal.add(name)
        }

        return false
    }

    fun flee() {
        yell(M.L(this, "cya", Dungeon.hero.givenName()))

        destroy()
        sprite.die()
    }

    object Quest {
        var spawned: Boolean = false
        var given: Boolean = false
        var isCompleted: Boolean = false

        var reward: Ring? = null

        private const val NODE = "demon"

        private const val SPAWNED = "spawned"
        private const val GIVEN = "given"
        private const val COMPLETED = "completed"
        private const val REWARD = "reward"

        fun reset() {
            spawned = false
            reward = null
        }

        fun storeInBundle(bundle: Bundle) {

            val node = Bundle()

            node.put(SPAWNED, spawned)

            if (spawned) {
                node.put(GIVEN, given)
                node.put(COMPLETED, isCompleted)
                node.put(REWARD, reward)
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
                    given = node.getBoolean(GIVEN)
                    isCompleted = node.getBoolean(COMPLETED)
                    reward = node.get(REWARD) as Ring?
                }
            }
        }

        // actually in city level
        fun Spawn(level: Level) {
            if (!spawned && Dungeon.depth > 16 && Random.Int(20 - Dungeon.depth) == 0) {

                val npc = Imp()
                do {
                    npc.pos = level.randomRespawnCell()
                } while (npc.pos == -1 || level.heaps.get(npc.pos) != null)
                level.mobs.add(npc)

                spawned = true
                given = false

                do {
                    reward = Generator.RING.generate() as Ring
                } while (reward!!.cursed)
                reward!!.upgrade(2)
                reward!!.cursed = true
            }
        }

        fun process(mob: Mob) {
            if (!isCompleted) {
                if ((spawned && given) || Random.Float() < 0.3f)
                    Dungeon.level.drop(DwarfToken(), mob.pos).sprite.drop()
            }
        }

        fun complete() {
            reward = null
            isCompleted = true

            Journal.remove(M.L(Imp::class.java, "name"))
        }
    }
}
