package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.*
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

private const val MAP_FILE = "data/VillageLevel.map"

class KVillageLevel : KRegularLevel() {
    init {
        color1 = 0x48763c
        color2 = 0x59994a
        viewDistance = 8
        seeDistance = 8
    }

    override fun tilesTex(): String = Assets.TILES_VILLAGE

    override fun waterTex(): String = Assets.WATER_VILLAGE

    override fun water(): BooleanArray = BooleanArray(length) { false }

    override fun grass(): BooleanArray = Patch.generate(this, 0.5f, 8)

    override fun build(iteration: Int): Boolean {
        loadMapDataFromFile(MAP_FILE)

        // map some terrains
        for (i in 0 until length) {
            when (map[i]) {
                Terrain.EMBERS -> map[i] = Terrain.WATER // water flag
                Terrain.LOCKED_DOOR -> map[i] = Terrain.EMPTY // npc pos flag
            }
        }

        // no luminary& traps
        paintWater()
        paintGrass()

        return true
    }

    override fun decorate() {
        // just put some tiny stone on the floor, some grass on the wall
        for (i in width until length - width) {
            if (map[i] == Terrain.WALL) {
                val nGrass = PathFinder.NEIGHBOURS4.count { map[i + it] == Terrain.GRASS }
                if (Random.Int(5) < nGrass)
                    map[i] = Terrain.WALL_DECO
            }
            else if(map[i]==Terrain.EMPTY){
                val nWall = PathFinder.NEIGHBOURS4.count { map[i + it] == Terrain.WALL }
                if(Random.Int(16)< nWall* nWall)
                    map[i] = Terrain.EMPTY_DECO
            }
        }
    }

    override fun nMobs(): Int = 0

    override fun createMobs() {
        // lix
        putMobAt(CatLix::class.java, 15, 29)

        // old alchemist
        putMobAt(Alchemist::class.java, 18, 6)
        Alchemist.Quest.reset()

        // jessica
        putMobAt(Jessica::class.java, 14, 3)
        Jessica.Quest.reset()

        // sodan
        putMobAt(DisheartenedBuddy::class.java, 17, 11)

        // scholar
        putMobAt(Scholar::class.java, 27, 11)

        // minstrel
        putMobAt(Minstrel::class.java, 14, 19)

        // battle mage
        putMobAt(SPDBattleMage::class.java, 6, 13)
    }

    override fun respawner(): Actor? = null

    override fun createItems() {}

    private fun putMobAt(cls: Class<out Mob>, x: Int, y: Int): Mob {
        val mob = cls.newInstance().apply { pos = xy2cell(x, y) }
        mobs.add(mob)
        return mob
    }
}