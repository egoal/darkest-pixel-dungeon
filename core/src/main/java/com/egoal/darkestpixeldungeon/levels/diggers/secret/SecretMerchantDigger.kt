package com.egoal.darkestpixeldungeon.levels.diggers.secret

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.UndeadShopkeeper
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.watabou.utils.PathFinder
import com.watabou.utils.Point
import com.watabou.utils.Random

class SecretMerchantDigger : RectDigger() {
    private lateinit var shopkeeper: UndeadShopkeeper

    override fun chooseRoomSize(wall: Wall): Point = Point(Random.Int(3, 6), Random.Int(3, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.GRASS)
        val door = level.pointToCell(overlappedWall(wall, rect).random())
        Set(level, door, Terrain.SECRET_DOOR)

        if(!::shopkeeper.isInitialized){
            shopkeeper = UndeadShopkeeper()
            shopkeeper.initSellItems()
        }

        shopkeeper.pos = level.pointToCell(rect.center)
        for (i in PathFinder.NEIGHBOURS8)
            if (Random.Int(2) == 0) Set(level, shopkeeper.pos, Terrain.WATER)
        level.mobs.add(shopkeeper)

        return DigResult(rect, DigResult.Type.Secret)
    }
}