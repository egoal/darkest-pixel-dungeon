package com.egoal.darkestpixeldungeon.items.rings

class RingOfArcane : Ring() {

    override fun buff(): RingBuff = Arcane()
    
    inner class Arcane: RingBuff()
}