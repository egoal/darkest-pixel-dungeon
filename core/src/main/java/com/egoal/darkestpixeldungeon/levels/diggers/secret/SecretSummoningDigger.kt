package com.egoal.darkestpixeldungeon.levels.diggers.secret


import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap
import com.watabou.utils.Point
import com.watabou.utils.Random

class SecretSummoningDigger : RectDigger() {

    override fun chooseRoomSize(wall: Wall) = Point(Random.Int(3, 6), Random.Int(3, 6))

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        Fill(level, rect, Terrain.SECRET_TRAP)
        Set(level, overlappedWall(wall, rect).random(), Terrain.SECRET_DOOR)

        level.drop(Generator.generate(), level.pointToCell(rect.center)).type = Heap.Type.SKELETON

        for (cell in rect.getAllPoints().map { level.pointToCell(it) })
            if (level.map[cell] == Terrain.SECRET_TRAP)
                level.setTrap(SummoningTrap().hide(), cell)

        return DigResult(rect, DigResult.Type.Secret)
    }
}