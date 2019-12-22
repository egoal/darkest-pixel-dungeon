package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Imp
import com.egoal.darkestpixeldungeon.levels.traps.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.Group
import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.particles.PixelParticle
import com.watabou.utils.Random

class CityLevel : RegularLevel() {
    init {
        color1 = 0x4b6636
        color2 = 0xf2f2f2
    }

    override fun trackMusic(): String = Assets.TRACK_CHAPTER_4
    
    override fun tilesTex(): String = Assets.TILES_CITY

    override fun waterTex(): String = Assets.WATER_CITY

    override fun water(): BooleanArray = Patch.Generate(this, if (feeling == Level.Feeling.WATER) 0.65f else 0.45f, 4)

    override fun grass(): BooleanArray = Patch.Generate(this, if (feeling == Level.Feeling.GRASS) 0.60f else 0.40f, 3)

    override fun trapClasses(): Array<Class<out Trap>> = arrayOf(
            BlazingTrap::class.java, FrostTrap::class.java, SpearTrap::class.java,
            VenomTrap::class.java, ExplosiveTrap::class.java, GrippingTrap::class.java,
            LightningTrap::class.java, RockfallTrap::class.java, OozeTrap::class.java,
            WeakeningTrap::class.java, CursingTrap::class.java, FlockTrap::class.java,
            GuardianTrap::class.java, PitfallTrap::class.java, SummoningTrap::class.java,
            TeleportationTrap::class.java, DisarmingTrap::class.java, WarpingTrap::class.java, FreakingTrap::class.java)

    override fun trapChances(): FloatArray = floatArrayOf(8f, 8f, 8f, 8f, 4f, 4f, 4f, 4f, 4f, 4f, 2f, 2f, 2f, 2f, 2f, 2f, 1f, 1f, 2f)

    override fun decorate() {
        for (i in 0 until length()) {
            if (map[i] == Terrain.EMPTY && Random.Int(10) == 0) {
                map[i] = Terrain.EMPTY_DECO
            } else if (map[i] == Terrain.WALL && Random.Int(8) == 0) {
                map[i] = Terrain.WALL_DECO
            }
        }
    }

    override fun createItems() {
        Imp.Quest.Spawn(this)

        super.createItems()
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
        AddCityVisuals(this, visuals)
        return visuals
    }

    companion object {
        fun AddCityVisuals(level: Level, group: Group) {
            for (i in 0 until level.length())
                if (level.map[i] == Terrain.WALL_DECO)
                    group.add(Smoke(i))
        }

        class Smoke(val pos: Int) : Emitter() {
            init {
                val p = DungeonTilemap.tileCenterToWorld(pos)
                pos(p.x - 4, p.y - 2, 4f, 0f)

                pour(Smoke.factory, 0.2f)
            }

            override fun update() {
                visible = Dungeon.visible[pos]
                if (visible)
                    super.update()
            }

            companion object {
                val factory = object : Factory() {
                    override fun emit(emitter: Emitter, index: Int, x: Float, y: Float) {
                        val sp = emitter.recycle(SmokeParticle::class.java) as SmokeParticle
                        sp.reset(x, y)
                    }
                }
            }
        }

        class SmokeParticle : PixelParticle() {
            init {
                color(0x000000)
                speed.set(Random.Float(8f), -Random.Float(8f))
            }

            fun reset(x: Float, y: Float) {
                revive()

                this.x = x
                this.y = y

                lifespan = 2f
                left = lifespan
            }

            override fun update() {
                super.update()
                val p = left / lifespan
                am = if (p > 0.8f) 1 - p else p * 0.25f
                size(8 - p * 4)
            }
        }
    }
}