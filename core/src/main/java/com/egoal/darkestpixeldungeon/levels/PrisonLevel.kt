package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Challenge
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Jessica
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Wandmaker
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.features.Luminary
import com.egoal.darkestpixeldungeon.levels.traps.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.utils.Random

class PrisonLevel : RegularLevel() {
    init {
        color1 = 0x6a723d
        color2 = 0x88924c
    }

    override fun trackMusic(): String = Assets.TRACK_CHAPTER_2

    override fun tilesTex() = Assets.TILES_PRISON

    override fun waterTex() = Assets.WATER_PRISON

    override fun water(): BooleanArray = Patch.Generate(this, if (feeling == Level.Feeling.WATER) 0.65f else 0.45f, 4)

    override fun grass(): BooleanArray = Patch.Generate(this, if (feeling == Level.Feeling.GRASS) 0.60f else 0.40f, 3)


    override fun trapClasses(): Array<Class<out Trap>> = arrayOf(
            ChillingTrap::class.java, FireTrap::class.java, PoisonTrap::class.java,
            SpearTrap::class.java, ToxicTrap::class.java, AlarmTrap::class.java,
            FlashingTrap::class.java, GrippingTrap::class.java, ParalyticTrap::class.java,
            LightningTrap::class.java, OozeTrap::class.java, ConfusionTrap::class.java,
            FlockTrap::class.java, SummoningTrap::class.java, TeleportationTrap::class.java, FreakingTrap::class.java)

    override fun trapChances(): FloatArray = floatArrayOf(4f, 4f, 4f, 4f, 2f, 2f, 2f, 2f, 2f, 2f, 1f, 1f, 1f, 1f, 1f)

    override fun createItems() {
        Jessica.Quest.spawnBook(this)
        super.createItems()
    }

    private var spawnWandmaker = false

    override fun chooseDiggers(): ArrayList<Digger> {
        val diggers = super.chooseDiggers()

        val digger = Wandmaker.Quest.GiveDigger()
        spawnWandmaker = digger != null
        if (digger != null) {
            diggers.add(digger)
        }

        return diggers
    }

    override fun createMobs() {
        if (spawnWandmaker) {
            spawnWandmaker = false // when reset, create Mobs would called again
            Wandmaker.Quest.Spawn(this, spaces.find { it.type == DigResult.Type.Entrance }!!.rect)
        }

        super.createMobs()
    }

    override fun decorate() {
        // empty
        for (i in width() + 1 until length() - width() - 1)
            if (map[i] == Terrain.EMPTY) {
                var c = 0.05f
                if (map[i + 1] == Terrain.WALL && map[i + width()] == Terrain.WALL)
                    c += 0.2f
                if (map[i - 1] == Terrain.WALL && map[i + width()] == Terrain.WALL)
                    c += 0.2f
                if (map[i + 1] == Terrain.WALL && map[i - width()] == Terrain.WALL)
                    c += 0.2f
                if (map[i - 1] == Terrain.WALL && map[i - width()] == Terrain.WALL)
                    c += 0.2f

                if (Random.Float() < c) {
                    map[i] = Terrain.EMPTY_DECO
                }
            }

        // wall
        for (i in 0 until width())
            if (map[i] == Terrain.WALL &&
                    (map[i + width()] == Terrain.EMPTY || map[i + width()] == Terrain.EMPTY_SP) &&
                    Random.Int(6) == 0)
                map[i] = Terrain.WALL_DECO

        for (i in width() until length() - width())
            if (map[i] == Terrain.WALL &&
                    map[i - width()] == Terrain.WALL &&
                    (map[i + width()] == Terrain.EMPTY || map[i + width()] == Terrain.EMPTY_SP) &&
                    Random.Int(3) == 0)
                map[i] = Terrain.WALL_DECO

    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(PrisonLevel::class.java, "water_name")
        else -> super.tileName(tile)
    }

    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.EMPTY_DECO -> Messages.get(PrisonLevel::class.java, "empty_deco_desc")
        Terrain.BOOKSHELF -> Messages.get(PrisonLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }

    override fun createSceneLuminaryAt(pos: Int): Luminary = Torch(pos)

    class Torch(pos: Int) : Luminary(pos) {

        override fun createVisual(): LightVisual = Visual(pos)

        class Visual(cell: Int) : Luminary.TorchLight(cell) {
            init {
                pour(FlameParticle.FACTORY, 0.15f)
            }
        }
    }

//    override fun updateFieldOfView(c: Char, fieldOfView: BooleanArray) {
//        super.updateFieldOfView(c, fieldOfView)
//        Log.d("dpd", "luminaries: ${luminaries.size}, lighted: ${Level.lighted.count { it }}")
//    }

//    companion objectid {
//        fun AddPrisonVisuals(level: Level, group: Group) {
//            for (i in 0 until level.length())
//                if (level.map[i] == Terrain.WALL_DECO)
//                    group.add(Torch(i))
//        }
//    }

}