package com.egoal.darkestpixeldungeon.levels

import android.opengl.GLES20
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.ShopDigger
import com.egoal.darkestpixeldungeon.levels.traps.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.noosa.Game
import com.watabou.noosa.Group
import com.watabou.noosa.particles.PixelParticle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import javax.microedition.khronos.opengles.GL10

class KHallsLevel : KRegularLevel() {
    init {
        color1 = 0x801500
        color2 = 0xa68521

        seeDistance = 8
        viewDistance = Math.max(25 - Dungeon.depth, 1)
    }

    override fun tilesTex(): String = Assets.TILES_HALLS

    override fun waterTex(): String = Assets.WATER_HALLS

    override fun water(): BooleanArray = Patch.generate(this, if (feeling == Level.Feeling.WATER) 0.55f else 0.40f, 6)

    override fun grass(): BooleanArray = Patch.generate(this, if (feeling == Level.Feeling.GRASS) 0.55f else 0.30f, 3)

    override fun trapClasses(): Array<Class<out Trap>> = arrayOf(
            BlazingTrap::class.java, DisintegrationTrap::class.java, FrostTrap::class.java,
            SpearTrap::class.java, VenomTrap::class.java, ExplosiveTrap::class.java,
            GrippingTrap::class.java, LightningTrap::class.java, OozeTrap::class.java,
            WeakeningTrap::class.java, CursingTrap::class.java, FlockTrap::class.java,
            GrimTrap::class.java, GuardianTrap::class.java, SummoningTrap::class.java,
            TeleportationTrap::class.java, DisarmingTrap::class.java, DistortionTrap::class.java,
            WarpingTrap::class.java)

    override fun trapChances(): FloatArray = floatArrayOf(8f, 8f, 8f, 8f, 8f, 4f, 4f, 4f, 4f, 4f, 2f, 2f, 2f, 2f, 2f, 2f, 1f, 1f, 1f)

    override fun chooseDiggers(): ArrayList<Digger> {
        // less diggers, but more specials
        val diggers = selectDiggers(Random.NormalIntRange(2, 4), 12)
        if (Dungeon.shopOnLevel())
            diggers.add(ShopDigger())

        return diggers
    }

    override fun decorate() {
        for (i in width + 1 until length - width - 1) {
            if (map[i] == Terrain.EMPTY) {
                val n = PathFinder.NEIGHBOURS8.count {
                    (Terrain.flags[map[i + it]] and Terrain.PASSABLE) != 0
                }

                if (Random.Int(80) < n)
                    map[i] = Terrain.EMPTY_DECO
            } else if (map[i] == Terrain.WALL &&
                    map[i - 1] != Terrain.WALL_DECO && map[i - width] != Terrain.WALL_DECO
                    && Random.Int(20) == 0)
                map[i] = Terrain.WALL_DECO
        }
    }

    override fun tileName(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(HallsLevel::class.java, "water_name")
        Terrain.GRASS -> Messages.get(HallsLevel::class.java, "grass_name")
        Terrain.HIGH_GRASS -> Messages.get(HallsLevel::class.java, "high_grass_name")
        Terrain.STATUE, Terrain.STATUE_SP -> Messages.get(HallsLevel::class.java, "statue_name")
        else -> super.tileName(tile)
    }

    override fun tileDesc(tile: Int): String = when (tile) {
        Terrain.WATER -> Messages.get(HallsLevel::class.java, "water_desc")
        Terrain.STATUE, Terrain.STATUE_SP -> Messages.get(HallsLevel::class.java, "statue_desc")
        Terrain.BOOKSHELF -> Messages.get(HallsLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }

    override fun addVisuals(): Group {
        super.addVisuals()
        AddHallsVisuals(this, visuals)
        return visuals
    }

    companion object {
        fun AddHallsVisuals(level: Level, group: Group) {
            for (i in 0 until level.length())
                if (level.map[i] == Terrain.WATER)
                    group.add(Stream(i))
        }

        class Stream(val pos: Int) : Group() {
            var delay = Random.Float(2f)

            override fun update() {
                visible = Dungeon.visible[pos]
                if (visible) {
                    super.update()

                    delay -= Game.elapsed
                    if (delay <= 0) {
                        delay = Random.Float(2f)

                        val p = DungeonTilemap.tileToWorld(pos)
                        (recycle(FireParticle::class.java) as FireParticle).reset(
                                p.x + Random.Float(DungeonTilemap.SIZE.toFloat()),
                                p.y + Random.Float(DungeonTilemap.SIZE.toFloat()))
                    }
                }
            }

            override fun draw() {
                GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE)
                super.draw()
                GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
            }
        }

        class FireParticle : PixelParticle.Shrinking() {
            init {
                color(0xee7722)
                lifespan = 1f

                acc.set(0f, 80f)
            }

            fun reset(x: Float, y: Float) {
                revive()

                this.x = x
                this.y = y

                left = lifespan

                speed.set(0f, -40f)
                size = 4f
            }

            override fun update() {
                super.update()
                val p = left / lifespan
                am = if (p > 0.8f) (1 - p) * 5f else 1f
            }
        }
    }

}