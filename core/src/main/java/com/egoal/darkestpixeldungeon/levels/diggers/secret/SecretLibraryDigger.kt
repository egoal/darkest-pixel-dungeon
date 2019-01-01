package com.egoal.darkestpixeldungeon.levels.diggers.secret

import com.egoal.darkestpixeldungeon.items.scrolls.*
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RoundDigger
import com.watabou.utils.Random

// modified from generator, more likely to give 'useful' scrolls.
val scrollClasses = arrayOf(
        ScrollOfIdentify::class.java, ScrollOfTeleportation::class.java, ScrollOfRemoveCurse::class.java,
        ScrollOfUpgrade::class.java, ScrollOfRecharging::class.java, ScrollOfMagicMapping::class.java,
        ScrollOfRage::class.java, ScrollOfTerror::class.java, ScrollOfLullaby::class.java,
        ScrollOfEnchanting::class.java, ScrollOfPsionicBlast::class.java, ScrollOfMirrorImage::class.java,
        ScrollOfCurse::class.java, ScrollOfLight::class.java)
val scrollChances = floatArrayOf(1f, 1f, 3f, 0f, 1f, 3f, 1f, 1f, 2f, 2f, 4f, 1f, 1f, 2f)

class SecretLibraryDigger : RoundDigger() {
    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        FillEllipse(level, rect, Terrain.EMPTY)

        val door = rect.center
        if (wall.direction.horizontal)
            door.x = wall.x1
        else door.y = wall.y1
        Set(level, door, Terrain.SECRET_DOOR)

        val n = Random.IntRange(2, 3)
        val chances = scrollChances.clone()
        for (i in 1..n) {
            var pos = level.pointToCell(rect.random())
            while (level.map[pos] != Terrain.EMPTY || level.heaps.get(pos) != null)
                pos = level.pointToCell(rect.random())

            val index = Random.chances(chances)
            chances[index] = 0f // unique
            level.drop(scrollClasses[index].newInstance(), pos)
        }

        return DigResult(rect, DigResult.Type.Secret)
    }
}