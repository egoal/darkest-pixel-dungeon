package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Bones
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary
import com.egoal.darkestpixeldungeon.actors.mobs.King
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.KingStatuary
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.Group
import com.watabou.noosa.audio.Music
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.tweeners.AlphaTweener
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class CityBossLevel : Level() {
    init {
        color1 = 0x4b6636
        color2 = 0xf2f2f2
    }

    private var arenaDoor = 0
    var enteredArena = false
    var keyDropped = false

    override fun tilesTex(): String = Assets.TILES_CITY

    override fun waterTex(): String = Assets.WATER_CITY

    override fun trackMusic(): String = if (enteredArena && !keyDropped)
        Assets.TRACK_BOSS_LOOP else Assets.TRACK_CHAPTER_4

    override fun build(iteration: Int): Boolean {
        loadMapDataFromFile(MAP_FILE)
        arenaDoor = xy2cell(17, 29)

        return true
    }

    override fun decorate() {}

    override fun createMobs() {
        mobs.add(KingStatuary().apply { pos = xy2cell(10, 3) })
    }

    override fun respawner(): Actor? = null

    override fun createItems() {
        // hero remains 
        Bones.get()?.let {
            var pos: Int
            do {
                pos = xy2cell(Random.Int(14, 21), Random.Int(30, 34))
            } while (pos == entrance || map[pos] == Terrain.SIGN)
            drop(it, pos).type = Heap.Type.REMAINS
        }
    }

    override fun randomRespawnCell(): Int =
            PathFinder.NEIGHBOURS8.map { it + entrance }.filter { passable[it] }.random()

    override fun press(cell: Int, ch: Char?) {
        super.press(cell, ch)

        // active statuary
//        if (ch == Dungeon.hero && remainStatuaries > 0)
//            activeNearbyStatuaries(cell)

        // create the king
        if (!enteredArena && isNearToHallCenter(cell) && ch == Dungeon.hero) {
            enteredArena = true
            seal()

            val boss = Bestiary.mob(Dungeon.depth).apply {
                state = WANDERING
                pos = xy2cell(17, 10)
            }
            GameScene.add(boss)

            if (Dungeon.visible[boss.pos]) {
                boss.notice()
                boss.sprite.alpha(0f)
                boss.sprite.parent.add(AlphaTweener(boss.sprite, 1f, 1f))
            }

            set(arenaDoor, Terrain.LOCKED_DOOR)
            GameScene.updateMap(arenaDoor)
            Dungeon.observe()

            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }
    }

    override fun drop(item: Item, cell: Int): Heap {
        if (!keyDropped && item is SkeletonKey) {
            keyDropped = true
            unseal()

            set(arenaDoor, Terrain.DOOR)
            GameScene.updateMap(arenaDoor)
            Dungeon.observe()

            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }

        return super.drop(item, cell)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(DOOR, arenaDoor)
        bundle.put(ENTERED, enteredArena)
        bundle.put(DROPPED, keyDropped)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        arenaDoor = bundle.getInt(DOOR)
        enteredArena = bundle.getBoolean(ENTERED)
        keyDropped = bundle.getBoolean(DROPPED)
    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(CityLevel::class.java, "water_name")
        Terrain.HIGH_GRASS -> Messages.get(CityLevel::class.java, "high_grass_name")
        else -> super.tileName(tile)
    }

    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.ENTRANCE -> Messages.get(CityLevel::class.java, "entrance_desc")
        Terrain.EXIT -> Messages.get(CityLevel::class.java, "exit_desc")
        Terrain.WALL_DECO, Terrain.EMPTY_DECO -> Messages.get(CityLevel::class.java, "deco_desc")
        Terrain.EMPTY_SP -> Messages.get(CityLevel::class.java, "sp_desc")
        Terrain.STATUE, Terrain.STATUE_SP -> Messages.get(CityLevel::class.java, "statue_desc")
        Terrain.BOOKSHELF -> Messages.get(CityLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }

    override fun addVisuals(): Group {
        super.addVisuals()
        CityLevel.AddCityVisuals(this, visuals)
        return visuals
    }

    fun activateAllStatuaries(): Boolean {
        var activated = false
        for (i in 0 until length()) {
            if (map[i] == Terrain.STATUE_SP) {
                activated = true
                map[i] = Terrain.EMPTY_SP
                GameScene.updateMap(i)

                val ku = King.Undead().apply {
                    state = HUNTING
                    pos = i
                }
                GameScene.add(ku, 1f)
                if (Dungeon.visible[i]) {
                    ku.sprite.emitter().start(ShadowParticle.CURSE, 0.05f, 10)
                    Sample.INSTANCE.play(Assets.SND_BONES)
                    ku.say(M.L(ku, "awaken"))
                }
            }
        }

        if (activated) {
            buildFlagMaps()
            Dungeon.observe() // actually, no need
        }
        
        return activated
    }

    private fun isNearToHallCenter(cell: Int): Boolean = cell / width() < 14

    companion object {
        private const val MAP_FILE: String = "data/CityBossLevel.map"

        private const val DOOR = "door"
        private const val ENTERED = "entered"
        private const val DROPPED = "dropped"
        private const val STATUARIES = "statuaries"
    }

}