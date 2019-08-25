package com.egoal.darkestpixeldungeon

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.artifacts.Artifact
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.watabou.noosa.Game
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.io.IOException
import java.lang.Exception
import kotlin.math.min

object Bones {
    private const val BONES_FILE = "bones.dat"

    private const val LEVEL = "level"
    private const val ITEM = "item"

    private var depth = -1
    private var item: Item? = null

    fun leave() {
        depth = Dungeon.depth

        // those who won, die far above their max depth, or who are challenged drop no bones.
        if (Statistics.AmuletObtained || Statistics.DeepestFloor - 5 >= depth || Dungeon.challenges > 0) {
            depth = -1
            return
        }

        item = pickItem(Dungeon.hero)

        val b = Bundle().apply {
            put(LEVEL, depth)
            put(ITEM, item)
        }
        try {
            val fout = Game.instance.openFileOutput(BONES_FILE, Game.MODE_PRIVATE)
            Bundle.write(b, fout)
            fout.close()
        } catch (e: IOException) {
            DarkestPixelDungeon.reportException(e)
        }
    }

    fun get(): Item? {
        if (depth == -1 && !loadBones()) return null

        if (depth != Dungeon.depth || Dungeon.challenges != 0) return null

        // drop
        Game.instance.deleteFile(BONES_FILE)
        depth = 0

        if (item is Artifact) {
            return if (Generator.ARTIFACT.remove(item as Artifact)) {
                try {
                    (item!!.javaClass.newInstance() as Artifact).apply {
                        transferUpgrade(min(item!!.visiblyUpgraded(), 1 + Dungeon.depth * 3 / 10))
                        cursed = true
                        cursedKnown = true
                    }
                } catch (e: Exception) {
                    DarkestPixelDungeon.reportException(e)
                    Gold(item!!.price())
                }
            } else Gold(item!!.price()) // artifact already dropped.
        }
        if (item!!.isUpgradable) {
            item!!.apply {
                cursed = true
                cursedKnown = true

                val lvl = 1 + Dungeon.depth * 3 / 10
                if (lvl < level())
                    degrade(level() - lvl)
                levelKnown = false
            }
        }

        item!!.reset()

        return item
    }

    private fun pickItem(hero: Hero): Item {
        var item: Item? = null
        if (Random.Int(2) == 0) {
            item = when (Random.Int(7)) {
                0 -> hero.belongings.weapon
                1 -> hero.belongings.armor
                2 -> hero.belongings.helmet
                3 -> hero.belongings.misc1
                4 -> hero.belongings.misc2
                5 -> hero.belongings.misc3
                else -> Dungeon.quickslot.randomNonePlaceholder()
            }

            if (item != null && !item.bones) return pickItem(Dungeon.hero)
        } else {
            val items = hero.belongings.backpack.filter { it.bones }

            if (Random.Int(3) < items.size) {
                item = items.random().apply {
                    if (stackable) {
                        if (this is MissileWeapon) quantity(Random.NormalIntRange(1, quantity()))
                        else quantity(Random.NormalIntRange(1, (quantity() + 1) / 2))
                    }
                }
            }
        }

        if (item == null) {
            val gold = if (Dungeon.gold > 50) Random.NormalIntRange(50, Dungeon.gold) else 50
            item = Gold(gold)
        }

        return item
    }

    private fun loadBones(): Boolean {
        try {
            val fin = Game.instance.openFileInput(BONES_FILE)
            val bundle = Bundle.read(fin)
            fin.close()

            depth = bundle.getInt(LEVEL)
            item = bundle.get(ITEM) as Item

            return true
        } catch (e: IOException) {
            return false
        }
    }
}