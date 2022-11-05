package com.egoal.darkestpixeldungeon.actors.blobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.BlobEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.windows.WndActionList
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.utils.Bundle
import kotlin.math.min

class MagicWellWater : Blob() {
    private var pos: Int = 0

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        for (i in cur.indices)
            if (cur[i] > 0) {
                pos = i
                break
            }
    }

    // do not spread
    override fun evolve() {
        off[pos] = cur[pos]
        volume = off[pos]

        area.union(pos % Dungeon.level.width(), pos / Dungeon.level.width())

        if (Dungeon.visible[pos]) {
            Journal.add(M.L(this, "name"))
        }
    }

    override fun seed(level: Level, cell: Int, amount: Int) {
        super.seed(level, cell, amount)

        cur[pos] = 0
        pos = cell
        cur[pos] = amount
        volume = cur[pos]

        area.setEmpty()
        area.union(cell % level.width(), cell / level.width())
    }

    override fun use(emitter: BlobEmitter) {
        super.use(emitter)
        emitter.start(Speck.factory(Speck.LIGHT), 0.5f, 0)
    }

    private fun consume(amount: Int) {
        val amount = min(amount, cur[pos])
        cur[pos] -= amount
        off[pos] = cur[pos]
        volume = off[pos]

        if (volume <= 0) {
            Level.set(pos, Terrain.EMPTY_WELL)
            GameScene.updateMap(pos)
        }
    }

    fun interact(hero: Hero) {
        consume(3)
    }

    override fun tileDesc(): String = M.L(this, "desc")

    companion object {
        fun Affect(cell: Int) {
            val water = Dungeon.level.blobs[MagicWellWater::class.java] as MagicWellWater?
            if (water != null && Dungeon.hero.pos == cell) {
                water.interact(Dungeon.hero)
            }
        }
    }

    inner abstract class WaterAction(val cost: Int) : WndActionList.Action() {
        override fun Name(): String = M.L(this, "name") + "($cost)"
        override fun Info(): String = M.L(this, "info")
        override fun Disabled(): Boolean = cost > cur[pos]

        override fun Execute() {
            val hero = Dungeon.hero
            if (Execute(hero)) {
                consume(cost)

                hero.spend(1f)
                hero.busy()
                hero.sprite.operate(hero.pos)
            }
        }

        protected abstract fun Execute(hero: Hero): Boolean
    }

    inner class Transform : WaterAction(5) {
        override fun Execute(hero: Hero): Boolean {
            GameScene.selectItem(WndBag.Listener {
                val new = changeItem(it)
                if (new != null && !(new === it)) {
                    it.detach(hero.belongings.backpack)
                }
            }, WndBag.Mode.ALL, M.L(this, "prompt"))
            return true
        }

        private fun changeItem(item: Item): Item? = null
    }
}