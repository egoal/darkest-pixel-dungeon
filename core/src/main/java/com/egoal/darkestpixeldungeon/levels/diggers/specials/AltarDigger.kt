package com.egoal.darkestpixeldungeon.levels.diggers.specials

import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.effects.particles.SacrificialParticle
import com.egoal.darkestpixeldungeon.items.weapon.curses.Sacrificial
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Rect
import com.egoal.darkestpixeldungeon.levels.diggers.Wall
import com.egoal.darkestpixeldungeon.levels.diggers.normal.DiamondDigger
import com.watabou.noosa.particles.Emitter
import com.watabou.utils.PathFinder

class AltarDigger: DiamondDigger(){

    override fun dig(level: Level, wall: Wall, rect: Rect): DigResult {
        val dr = super.dig(level, wall, rect)
        
        for(i in rect.getAllPoints().map { level.pointToCell(it) })
            if(level.map[i]==Terrain.EMPTY)
                Set(level, i, Terrain.EMPTY_SP)

        val cen = level.pointToCell(rect.center)
        for(i in PathFinder.NEIGHBOURS8)
            Set(level, cen+i, Terrain.EMBERS)
        
        //todo add sacrificial fire here
        // val fire = level.blobs.get(SacrificialFire)
        
//        val f: Blob? = level.blobs[SacrificialFire::class.java]
//        val fire = if(f==null) SacrificialFire(cen) else f as SacrificialFire
        
        return dr 
    }
    
    companion object {
        class SacrificialFire(val pos: Int): Emitter(){
            init{
                val p = DungeonTilemap.tileCenterToWorld(pos)
                pos(p.x-2, p.y+1, 4f, 0f)
                
                pour(SacrificialParticle.FACTORY, 0.1f)
            }
        }
    }
    
}