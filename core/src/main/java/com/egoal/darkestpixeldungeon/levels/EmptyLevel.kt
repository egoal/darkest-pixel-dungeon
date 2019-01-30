package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.Ballista
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.levels.diggers.Digger

class EmptyLevel : KRegularLevel() {
    init {
        color1 = 0x48763c
        color2 = 0x59994a
    }

    override fun tilesTex(): String = Assets.TILES_SEWERS

    override fun waterTex(): String = Assets.WATER_SEWERS

    override fun water(): BooleanArray = BooleanArray(length())

    override fun grass(): BooleanArray = BooleanArray(length())

    override fun decorate() {}

    override fun createMobs() {}

    override fun respawner(): Actor? = null

    override fun createItems() {
        for(i in 1..20) drop(KGenerator.ARTIFACT.generate(), xy2cell(5, 5+i))
        for(i in 1..20) drop(KGenerator.WEAPON.generate(), xy2cell(6, 5+i))
        for(i in 1..20) drop(KGenerator.ARMOR.generate(), xy2cell(7, 5+i))
        for(i in 1..20) drop(KGenerator.WAND.generate(), xy2cell(8, 5+i))
        for(i in 1..20) drop(KGenerator.RING.generate(), xy2cell(9, 5+i))
        for(i in 1..20) drop(KGenerator.POTION.generate(), xy2cell(10, 5+i))
        for(i in 1..20) drop(KGenerator.SCROLL.generate(), xy2cell(11, 5+i))
        for(i in 1..20) drop(KGenerator.SEED.generate(), xy2cell(12, 5+i))
    }

    override fun build(iteration: Int): Boolean {
        Digger.Fill(this, 1, 1, width - 2, height - 2, Terrain.EMPTY)

        entrance = xy2cell(width / 2, height / 2)
        exit = entrance + 1
        map[entrance] = Terrain.ENTRANCE
        map[exit] = Terrain.EXIT

        map[entrance + 3] = Terrain.ALCHEMY
        map[entrance + 4] = Terrain.ENCHANTING_STATION

        return true
    }
}