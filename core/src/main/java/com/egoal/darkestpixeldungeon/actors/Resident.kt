package com.egoal.darkestpixeldungeon.actors

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Statistics

class Resident: Actor(){
    init {
        // default actPriority, act at last
        
        Instance = this        
    }
    
    override fun act(): Boolean {
        Statistics.Clock.spend(TICK* TIME_SCALE)
        
        spend(TICK)
        return true 
    }
    
    companion object {
        lateinit var Instance: Resident
        
        private const val TICK = 1f
        private const val TIME_SCALE = 3f
    }
}