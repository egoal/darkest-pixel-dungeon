package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.books.textbook.CallysDiary
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.JessicaSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.watabou.utils.Bundle
import com.watabou.utils.Random

/**
 * Created by 93942 on 5/5/2018.
 */

class Jessica : NPC.Unbreakable() {
    init {
        spriteClass = JessicaSprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    /// do something
    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        // give, complete,
        if (!Quest.given_) {
            WndDialogue.Show(this, M.L(this, "please"), M.L(this, "ok"), M.L(this, "sorry")) {
                if (it == 0) {
                    Quest.given_ = true
                    tell(M.L(Jessica::class.java, "good-luck"))
                }
            }
        } else if (!Quest.completed_) {
            val content = M.L(this, "how-is-it-going")
            if (Dungeon.hero.lvl == 1) { //todo: should use Statistics::DeepestFloor
                // not into the dungeon
                WndDialogue.Show(this, content, M.L(this, "ok")) {}
            } else {
                val cd = Dungeon.hero.belongings.getItem(CallysDiary::class.java)

                if (cd == null) WndDialogue.Show(this, content, M.L(this, "no-found")) {
                    Quest.completed_ = true
                    tell(M.L(Jessica::class.java, "pity-no-found"))
                }
                else {
                    WndDialogue.Show(this, content, M.L(this, "found"), M.L(this, "found-lie")) {
                        if (it == 0) {
                            cd.detach(Dungeon.hero.belongings.backpack)
                            GLog.w(M.L(Jessica::class.java, "return_book"))
                            Quest.completed_ = true
                            tell(M.L(Jessica::class.java, "thank_you"))
                        } else if (it == 1) {
                            Quest.completed_ = true
                            tell(M.L(Jessica::class.java, "thank_you_lie"))
                        }
                    }
                }
            }
        } else {
            tell(Messages.get(this, "farewell"))
        }

        return false
    }

    // unbreakable
    override fun reset(): Boolean {
        return true
    }

    override fun description(): String {
        return Messages.get(this, if (Quest.completed_) "desc_2" else "desc")
    }

    object Quest {

        var spawned_: Boolean = false
        var given_: Boolean = false
        var completed_: Boolean = false

        // bundle
        private const val NODE = "jessica"

        private const val SPAWNED = "spawned"
        private const val GIVEN = "given"
        private const val COMPLETED = "completed"

        fun reset() {
            spawned_ = false
            given_ = false
            completed_ = false
        }

        fun storeInBundle(bundle: Bundle) {
            val node = Bundle()
            node.put(SPAWNED, spawned_)
            node.put(GIVEN, given_)
            node.put(COMPLETED, completed_)

            bundle.put(NODE, node)
        }

        fun restoreFromBundle(bundle: Bundle) {
            val node = bundle.getBundle(NODE)
            if (!node.isNull) {
                spawned_ = node.getBoolean(SPAWNED)
                given_ = node.getBoolean(GIVEN)
                completed_ = node.getBoolean(COMPLETED)
            } else {
                reset()
            }
        }

        // prison level indeed
        fun spawnBook(level: Level): Boolean {
            if (!given_ || spawned_)
                return true

            if (Dungeon.depth > 5 && Random.Int(10 - Dungeon.depth) == 0) {
                val heap = Heap()
                heap.type = Heap.Type.SKELETON
                // heap.drop(new Book().setTitle(Book.Title.COLLIES_DIARY));
                heap.drop(CallysDiary())
                heap.drop(Generator.RING.generate())

                heap.pos = level.randomRespawnCell()
                level.heaps.put(heap.pos, heap)

                //        level.heaps.put(level.randomRespawnCell(), heap);
                spawned_ = true
            }

            return true
        }
    }

}
