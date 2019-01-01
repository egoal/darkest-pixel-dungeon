package com.egoal.darkestpixeldungeon.levels.diggers.secret

import com.egoal.darkestpixeldungeon.actors.blobs.Foliage
import com.egoal.darkestpixeldungeon.items.wands.WandOfRegrowth
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.PatchDigger
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.plants.Starflower
import com.watabou.utils.Point
import com.watabou.utils.Random

class SecretGardenDigger : PatchDigger() {
    override fun chooseRoomSize(wall: Wall) = Point(Random.IntRange(6, 8), Random.IntRange(6, 8))

    override fun patchTile(): Int = Terrain.GRASS

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        super.dig(level, wall, rect)

        // make secret
        //fixme: remove this bad design. i may set a door type in Diggers,  
        for (cell in overlappedWall(wall, rect).getAllPoints().map { level.pointToCell(it) })
            if (level.map[cell] == Terrain.DOOR)
                level.map[cell] = Terrain.SECRET_DOOR

        // give plant
        val randomPlant = { seed: Plant.Seed ->
            var pos = level.pointToCell(rect.random())
            while (level.plants.get(pos) != null)
                pos = level.pointToCell(rect.random())

            level.plant(seed, pos)
        }

        randomPlant(Starflower.Seed())
        randomPlant(WandOfRegrowth.Dewcatcher.Seed())
        randomPlant(WandOfRegrowth.Seedpod.Seed())
        randomPlant(if (Random.Int(2) == 0) WandOfRegrowth.Dewcatcher.Seed()
        else WandOfRegrowth.Seedpod.Seed())

        // effects, as in garden 
        val l = level.blobs[Foliage::class.java]
        val light: Foliage = if(l==null) Foliage() else l as Foliage

        for (p in rect.getAllPoints())
            light.seed(level, level.pointToCell(p), 1)
        level.blobs[Foliage::class.java] = light
        
        return DigResult(rect, DigResult.Type.Secret)
    }
}