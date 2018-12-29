package com.egoal.darkestpixeldungeon.levels.diggers.unordinary

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.mobs.Skeleton
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Gold
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLiquidFlame
import com.egoal.darkestpixeldungeon.items.quest.CorpseDust
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual
import com.watabou.utils.Point
import com.watabou.utils.Random

import java.util.ArrayList

/**
 * Created by 93942 on 2018/12/18.
 */

class MassGraveDigger : RectDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(5, 9), Random.IntRange(5, 9))


    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.EMPTY_SP)

        val door = overlappedWall(wall, rect).random()
        Set(level, door, Terrain.BARRICADE)
        level.addItemToSpawn(PotionOfLiquidFlame())

        level.customTiles.addAll(CustomTileVisual.CustomTilesForRect(rect, Bones::class.java))
        
        // 50% 1 skeleton, 50% 2 skeletons
        for (i in 0..Random.Int(2)) {
            val s = Skeleton()
            do {
                s.pos = level.pointToCell(rect.random())
            } while (level.map[s.pos] != Terrain.EMPTY_SP || level.findMob(s.pos) != null)
            level.mobs.add(s)
        }

        val items = ArrayList<Item>()
        //100% corpse dust, 2x100% 1 coin, 2x30% coins, 1x60% random item, 1x30% 
        // armor
        items.add(CorpseDust())
        items.add(Gold(1))
        items.add(Gold(1))
        if (Random.Float() <= 0.3f) items.add(Gold())
        if (Random.Float() <= 0.3f) items.add(Gold())
        if (Random.Float() <= 0.6f) items.add(Generator.random())
        if (Random.Float() <= 0.3f) items.add(Generator.randomArmor())

        for (i in items) {
            var pos: Int
            do {
                pos = level.pointToCell(rect.random())
            } while (level.map[pos] != Terrain.EMPTY_SP || level.heaps.get(pos) != null)
            val h = level.drop(i, pos)
            h.type = Heap.Type.SKELETON
        }

        return DigResult(rect, DigResult.Type.Locked)
    }

    class Bones : CustomTileVisual() {
        init {
            name = Messages.get(this, "name")

            tx = Assets.PRISON_QUEST
            txX = 3
            txY = 0
        }

        override fun desc(): String? {
            return if (ofsX == 1 && ofsY == 1) {
                Messages.get(this, "desc")
            } else {
                null
            }
        }
    }
}
