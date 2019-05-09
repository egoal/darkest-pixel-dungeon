package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Blacksmith
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Yvette
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.BlackSmithDigger
import com.egoal.darkestpixeldungeon.levels.diggers.unordinary.TrappedRangerDigger
import com.egoal.darkestpixeldungeon.levels.traps.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.Game
import com.watabou.noosa.Group
import com.watabou.noosa.particles.PixelParticle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random


class CavesLevel : RegularLevel() {
    init {
        color1 = 0x534f3e
        color2 = 0xb9d661
    }

    override fun trackMusic(): String = Assets.TRACK_CHAPTER_3

    override fun tilesTex() = Assets.TILES_CAVES

    override fun waterTex() = Assets.WATER_CAVES

    override fun water(): BooleanArray = Patch.Generate(this, if (feeling == Level.Feeling.WATER) 0.60f else 0.45f, 6)

    override fun grass(): BooleanArray = Patch.Generate(this, if (feeling == Level.Feeling.GRASS) 0.55f else 0.35f, 3)

    override fun trapClasses(): Array<Class<out Trap>> = arrayOf(
            FireTrap::class.java, FrostTrap::class.java, PoisonTrap::class.java,
            SpearTrap::class.java, VenomTrap::class.java, ExplosiveTrap::class.java,
            FlashingTrap::class.java, GrippingTrap::class.java, ParalyticTrap::class.java,
            LightningTrap::class.java, RockfallTrap::class.java, OozeTrap::class.java,
            ConfusionTrap::class.java, FlockTrap::class.java, GuardianTrap::class.java,
            PitfallTrap::class.java, SummoningTrap::class.java, TeleportationTrap::class.java,
            WarpingTrap::class.java)

    override fun trapChances(): FloatArray = floatArrayOf(8f, 8f, 8f, 8f, 8f, 4f, 4f, 4f, 4f, 4f, 4f, 4f, 2f, 2f, 2f, 2f, 2f, 2f, 1f)

    //todo: overhaul quest system...
    override fun chooseDiggers(): ArrayList<Digger> {
        val diggers = super.chooseDiggers()

        // todo: rework this...
        if (!Yvette.Quest.Spawned && !Blacksmith.Quest.spawned) {
            if (Random.Int(15 - Dungeon.depth) == 0) {
                if (Random.Int(2) == 0) {
                    diggers.add(BlackSmithDigger())
                    Blacksmith.Quest.Spawn()
                } else {
                    diggers.add(TrappedRangerDigger())
                    Yvette.Quest.Spawned = true
                }
            }
        }

        return diggers
    }

    override fun decorate() {
        // corners
        for (space in spaces) {
            if (space.type != DigResult.Type.Normal || space.rect.width <= 3 || space.rect.height <= 3) continue

            val area = space.rect.area
            if (Random.Int(area) > 8) {
                val c = xy2cell(space.rect.x1, space.rect.y1)
                if (map[c - 1] == Terrain.WALL && map[c - width] == Terrain.WALL) {
                    map[c] = Terrain.WALL
                    traps.remove(c)
                    plants.remove(c)
                }
            }

            if (Random.Int(area) > 8) {
                val c = xy2cell(space.rect.x2, space.rect.y1)
                if (map[c + 1] == Terrain.WALL && map[c - width] == Terrain.WALL) {
                    map[c] = Terrain.WALL
                    traps.remove(c)
                    plants.remove(c)
                }
            }

            if (Random.Int(area) > 8) {
                val c = xy2cell(space.rect.x2, space.rect.y1)
                if (map[c - 1] == Terrain.WALL && map[c + width] == Terrain.WALL) {
                    map[c] = Terrain.WALL
                    traps.remove(c)
                    plants.remove(c)
                }
            }

            if (Random.Int(area) > 8) {
                val c = xy2cell(space.rect.x2, space.rect.y2)
                if (map[c + 1] == Terrain.WALL && map[c + width] == Terrain.WALL) {
                    map[c] = Terrain.WALL
                    traps.remove(c)
                    plants.remove(c)
                }
            }
        }

        // floor
        for (i in width + 1 until length - width) {
            if (map[i] == Terrain.EMPTY) {
                val n = PathFinder.NEIGHBOURS4.count { map[it + i] == Terrain.WALL }
                if (Random.Int(6) <= n)
                    map[i] = Terrain.EMPTY_DECO
            }
        }

        // mine
        for (i in 0 until length)
            if (map[i] == Terrain.WALL && Random.Int(12) == 0)
                map[i] = Terrain.WALL_DECO
    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.GRASS -> Messages.get(CavesLevel::class.java, "grass_name")
        Terrain.HIGH_GRASS -> Messages.get(CavesLevel::class.java, "high_grass_name")
        Terrain.WATER -> Messages.get(CavesLevel::class.java, "water_name")
        else -> super.tileName(tile)
    }


    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.ENTRANCE -> Messages.get(CavesLevel::class.java, "entrance_desc")
        Terrain.EXIT -> Messages.get(CavesLevel::class.java, "exit_desc")
        Terrain.HIGH_GRASS -> Messages.get(CavesLevel::class.java, "high_grass_desc")
        Terrain.WALL_DECO -> Messages.get(CavesLevel::class.java, "wall_deco_desc")
        Terrain.BOOKSHELF -> Messages.get(CavesLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }

    override fun addVisuals(): Group {
        super.addVisuals()
        AddCavesVisuals(this, visuals)
        return visuals
    }

    companion object {
        fun AddCavesVisuals(level: Level, group: Group) {
            for (i in 0 until level.length())
                if (level.map[i] == Terrain.WALL_DECO)
                    group.add(Vein(i))
        }

        class Vein(val pos: Int) : Group() {
            var delay = Random.Float(2f)

            override fun update() {
                visible = Dungeon.visible[pos]
                if (visible) {

                    super.update()
                    delay -= Game.elapsed
                    if (delay <= 0) {

                        //pickaxe can remove the ore, should remove the sparkling too.
                        if (Dungeon.level.map[pos] != Terrain.WALL_DECO) {
                            kill()
                            return
                        }

                        delay = Random.Float()

                        val p = DungeonTilemap.tileToWorld(pos)
                        (recycle(Sparkle::class.java) as Sparkle).reset(
                                p.x + Random.Float(DungeonTilemap.SIZE.toFloat()),
                                p.y + Random.Float(DungeonTilemap.SIZE.toFloat()))
                    }
                }
            }
        }

        class Sparkle : PixelParticle() {
            fun reset(x: Float, y: Float) {
                revive()

                this.x = x
                this.y = y

                lifespan = 0.5f
                left = lifespan
            }

            override fun update() {
                super.update()

                val p = left / lifespan
                am = if (p < 0.5f) p * 2 else (1 - p) * 2
                size(am * 2)
            }
        }

    }
}
