package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Bones
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.LevelDigger
import com.egoal.darkestpixeldungeon.levels.diggers.normal.*
import com.egoal.darkestpixeldungeon.levels.diggers.secret.SecretRatKingDigger
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.SewerExitDigger
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.audio.Music
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class SewerBossLevel : SewerLevel() {

    private var bossAppeared = false
    private var bossDefeated = false
    private var stairs = 0 // place holder for entrance

    override fun trackMusic(): String = if (bossAppeared && !bossDefeated) Assets.TRACK_BOSS_LOOP else super.trackMusic()

    override fun water(): BooleanArray {
        val array = Patch.Generate(this, 0.5f, 5)

        // water in the entrance space 
        val rect = spaces.find { it.type == DigResult.Type.Entrance }!!.rect
        for (x in rect.x1..rect.x2)
            array[xy2cell(x, rect.y1 + 1)] = true

        return array
    }

    override fun grass(): BooleanArray = Patch.Generate(this, 0.5f, 4)

    override fun respawner(): Actor? = null

    override fun chooseDiggers(): ArrayList<Digger> {
        val rectDiggers = hashMapOf(
                CellDigger::class.java to .1f,
                LatticeDigger::class.java to .05f,
                RectDigger::class.java to 1.25f,
                StripDigger::class.java to .1f,
                GraveyardDigger::class.java to .02f,
                SmallCornerDigger::class.java to 0.075f
        )

        val diggers = ArrayList<Digger>()
        diggers.add(SecretRatKingDigger())
        diggers.add(SewerExitDigger())

        val total = Random.IntRange(7, 9)
        while (diggers.size < total)
            diggers.add(Random.chances(rectDiggers).newInstance())

        return diggers
    }

    override fun createLevelDigger(): LevelDigger = LevelDigger(this, 1)
    
    override fun decorate() {
        super.decorate()

        // fill all chasm
        for (i in 0 until length)
            if (map[i] == Terrain.CHASM)
                map[i] = Terrain.EMPTY
    }

    override fun setStairs(): Boolean {
        map[entrance] = Terrain.ENTRANCE
        map[exit] = Terrain.LOCKED_EXIT

        return true
    }

    override fun randomRespawnCell(): Int = pointToCell(spaces.find { it.type == DigResult.Type.Entrance }!!.rect.random())

    override fun createMobs() {
        val mob = Bestiary.mob(Dungeon.depth).apply {
            pos = (1..10).map { pointToCell(randomSpace(DigResult.Type.Normal)!!.rect.random()) }
                    .maxBy { distance(entrance, it) }!!
        }

        mobs.add(mob)
    }

    override fun createItems() {
        Bones.get()?.let {
            var pos: Int
            do {
                pos = randomRespawnCell()
            } while (pos == entrance || map[pos] == Terrain.SIGN)
            drop(it, pos).type = Heap.Type.REMAINS
        }
    }

    override fun seal() {
        if (entrance != 0) {
            super.seal()

            set(entrance, Terrain.WATER_TILES)
            GameScene.updateMap(entrance)
            GameScene.ripple(entrance)

            stairs = entrance
            entrance = 0
            bossAppeared = true

            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }
    }

    override fun unseal() {
        if (stairs != 0) {
            super.unseal()

            entrance = stairs
            stairs = 0

            set(entrance, Terrain.ENTRANCE)
            GameScene.updateMap(entrance)

            bossDefeated = true
            Music.INSTANCE.play(trackMusic(), true)
            Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(STAIRS, stairs)
        bundle.put(BOSS_APPEARED, bossAppeared)
        bundle.put(BOSS_DEFEATED, bossDefeated)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        stairs = bundle.getInt(STAIRS)
        bossAppeared = bundle.getBoolean(BOSS_APPEARED)
        bossDefeated = bundle.getBoolean(BOSS_DEFEATED)
    }

    companion object {
        private const val STAIRS = "stairs"
        private const val BOSS_APPEARED = "boss-appeared"
        private const val BOSS_DEFEATED = "boss-defeated"
    }
}