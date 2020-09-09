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
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.BlackSmithDigger
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.windows.WndBlacksmith
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.quest.DarkGold
import com.egoal.darkestpixeldungeon.items.quest.Pickaxe
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.BlacksmithSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class Blacksmith : NPC.Unbreakable() {

    init {
        spriteClass = BlacksmithSprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (!Quest.given) {
            val msg = if (Quest.alternative) M.L(this, "blood_1") else M.L(this, "gold_1")

            GameScene.show(object : WndQuest(this, msg) {
                override fun onBackPressed() {
                    super.onBackPressed()

                    Quest.given = true
                    Quest.completed = false

                    val pick = Pickaxe()
                    if (pick.doPickUp(Dungeon.hero)) {
                        GLog.i(M.L(Dungeon.hero, "you_now_have", pick.name()))
                    } else {
                        Dungeon.level.drop(pick, Dungeon.hero.pos).sprite.drop()
                    }
                }
            })

            Journal.add(M.L(this, "name"))
        } else if (!Quest.completed) {
            if (Quest.alternative) {

                val pick = Dungeon.hero.belongings.getItem(Pickaxe::class.java)
                if (pick == null) {
                    tell(M.L(this, "lost_pick"))
                } else if (!pick.bloodStained) {
                    tell(M.L(this, "blood_2"))
                } else {
                    if (pick.isEquipped(Dungeon.hero)) {
                        pick.doUnequip(Dungeon.hero, false)
                    }
                    pick.detach(Dungeon.hero.belongings.backpack)
                    tell(M.L(this, "completed"))

                    Quest.completed = true
                    Quest.reforged = false
                }

            } else {
                val pick = Dungeon.hero.belongings.getItem(Pickaxe::class.java)
                val gold = Dungeon.hero.belongings.getItem(DarkGold::class.java)
                if (pick == null) {
                    tell(M.L(this, "lost_pick"))
                } else if (gold == null || gold.quantity() < 12) {
                    tell(M.L(this, "gold_2"))
                } else {
                    if (pick.isEquipped(Dungeon.hero)) {
                        pick.doUnequip(Dungeon.hero, false)
                    }
                    pick.detach(Dungeon.hero.belongings.backpack)
                    gold.detachAll(Dungeon.hero.belongings.backpack)
                    tell(M.L(this, "completed"))

                    Quest.completed = true
                    Quest.reforged = false
                }

            }
        } else if (!Quest.reforged) {
            GameScene.show(WndBlacksmith(this, Dungeon.hero))
        } else {
            tell(M.L(this, "get_lost"))
        }

        return false
    }

    override fun reset(): Boolean = true

    object Quest {
        var spawned: Boolean = false

        var alternative: Boolean = false
        var given: Boolean = false
        var completed: Boolean = false
        var reforged: Boolean = false

        private const val NODE = "blacksmith"

        private const val SPAWNED = "spawned"
        private const val ALTERNATIVE = "alternative"
        private const val GIVEN = "given"
        private const val COMPLETED = "completed"
        private const val REFORGED = "reforged"

        fun reset() {
            spawned = false
            given = false
            completed = false
            reforged = false
        }

        fun storeInBundle(bundle: Bundle) {
            val node = Bundle()

            node.put(SPAWNED, spawned)

            if (spawned) {
                node.put(ALTERNATIVE, alternative)
                node.put(GIVEN, given)
                node.put(COMPLETED, completed)
                node.put(REFORGED, reforged)
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
                    alternative = node.getBoolean(ALTERNATIVE)
                    given = node.getBoolean(GIVEN)
                    completed = node.getBoolean(COMPLETED)
                    reforged = node.getBoolean(REFORGED)
                }
            }
        }

        // call these like you did with wand maker
        fun GiveDigger(): Digger? {
            return if (!spawned && Dungeon.depth > 11 &&
                    Random.Int(15 - Dungeon.depth) == 0) {
                BlackSmithDigger()
            } else null
        }

        fun Spawn() {
            spawned = true
            alternative = Random.Int(2) == 0
            given = false
        }
    }

    companion object {
        fun verify(item1: Item, item2: Item): String? = when {
            item1 === item2 -> M.L(Blacksmith::class.java, "same_item")
            item1.javaClass != item2.javaClass -> M.L(Blacksmith::class.java, "diff_type")
            !item1.isIdentified || !item2.isIdentified -> M.L(Blacksmith::class.java, "un_ided")
            item1.cursed || item2.cursed -> M.L(Blacksmith::class.java, "cursed")
            item1.level() < 0 || item2.level() < 0 -> M.L(Blacksmith::class.java, "degraded")
            !item1.isUpgradable || !item2.isUpgradable -> M.L(Blacksmith::class.java, "cant_reforge")
            else -> null
        }

        fun upgrade(item1: Item, item2: Item) {
            val first: Item
            val second: Item
            if (item2.level() > item1.level()) {
                first = item2
                second = item1
            } else {
                first = item1
                second = item2
            }

            Sample.INSTANCE.play(Assets.SND_EVOKE)
            ScrollOfUpgrade.upgrade(Dungeon.hero)
            Item.evoke(Dungeon.hero)

            if (first.isEquipped(Dungeon.hero)) {
                (first as EquipableItem).doUnequip(Dungeon.hero, true)
            }
            first.level(first.level() + 1) //prevents on-upgrade effects like
            // enchant/glyph removal
            Dungeon.hero.spendAndNext(2f)
            Badges.validateItemLevelAquired(first)

            if (second.isEquipped(Dungeon.hero)) {
                (second as EquipableItem).doUnequip(Dungeon.hero, false)
            }
            second.detachAll(Dungeon.hero.belongings.backpack)

            Quest.reforged = true

            Journal.remove(M.L(Blacksmith::class.java, "name"))
        }
    }
}
