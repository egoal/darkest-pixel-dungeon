package com.egoal.darkestpixeldungeon.levels

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Ghost
import com.egoal.darkestpixeldungeon.items.unclassified.DewVial
import com.egoal.darkestpixeldungeon.levels.diggers.Digger
import com.egoal.darkestpixeldungeon.levels.diggers.specials.MerchantDigger
import com.egoal.darkestpixeldungeon.levels.traps.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.Game
import com.watabou.noosa.Group
import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.particles.PixelParticle
import com.watabou.utils.ColorMath
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

open class SewerLevel : RegularLevel() {
    init {
        color1 = 0x48763c
        color2 = 0x59994a
    }

    override fun trackMusic(): String = Assets.TRACK_CHAPTER_1
    
    override fun tilesTex(): String = Assets.TILES_SEWERS

    override fun waterTex(): String = Assets.WATER_SEWERS

    override fun water(): BooleanArray = Patch.Generate(this, if (feeling == Level.Feeling.WATER) 0.60f else 0.45f, 5)

    override fun grass(): BooleanArray = Patch.Generate(this, if (feeling == Level.Feeling.GRASS) 0.60f else 0.40f, 4)

    override fun trapClasses(): Array<Class<out Trap>> = if (Dungeon.depth == 1) arrayOf(WornTrap::class.java) else
        arrayOf(ChillingTrap::class.java, ToxicTrap::class.java, WornTrap::class.java,
                AlarmTrap::class.java, OozeTrap::class.java, FlockTrap::class.java,
                SummoningTrap::class.java, TeleportationTrap::class.java)

    override fun trapChances() = if (Dungeon.depth == 1) floatArrayOf(1f) else
        floatArrayOf(4f, 4f, 4f, 2f, 2f, 1f, 1f, 1f)

    override fun decorate() {
        for (i in 0 until width())
            if (map[i] == Terrain.WALL && map[i + width()] == Terrain.WATER && Random.Int(4) == 0)
                map[i] = Terrain.WALL_DECO

        for (i in width() until length() - width())
            if (map[i] == Terrain.WALL && map[i - width()] == Terrain.WALL && map[i + width()] == Terrain.WATER &&
                    Random.Int(2) == 0)
                map[i] = Terrain.WALL_DECO

        for (i in width() + 1 until length() - width() - 1)
            if (map[i] == Terrain.EMPTY) {
                val count = PathFinder.NEIGHBOURS4.count { map[i + it] == Terrain.WALL }
                if (Random.Int(16) < count * count) map[i] = Terrain.EMPTY_DECO
            }
    }

    override fun chooseDiggers(): ArrayList<Digger> {
        // less diggers
        val diggers = selectDiggers(Random.NormalIntRange(1, 3), Random.IntRange(11, 13))
        if (Dungeon.shopOnLevel())
            diggers.add(MerchantDigger())

        return diggers
    }

    override fun createItems() {
        // drop vial
        if (Dungeon.depth == 1 && !Dungeon.limitedDrops.dewVial.dropped()) {
            addItemToSpawn(DewVial())
            Dungeon.limitedDrops.dewVial.drop()
        }

        // ghost
        Ghost.Quest.Spawn(this)

        super.createItems()
    }

    override fun tileName(tile: Int) = when (tile) {
        Terrain.WATER -> Messages.get(SewerLevel::class.java, "water_name")
        else -> super.tileName(tile)
    }

    override fun tileDesc(tile: Int) = when (tile) {
        Terrain.EMPTY_DECO -> Messages.get(SewerLevel::class.java, "empty_deco_desc")
        Terrain.BOOKSHELF -> Messages.get(SewerLevel::class.java, "bookshelf_desc")
        else -> super.tileDesc(tile)
    }

    override fun addVisuals(): Group {
        super.addVisuals()
        AddSewerVisuals(this, visuals)
        return visuals
    }

    companion object {
        fun AddSewerVisuals(level: Level, group: Group) {
            for (i in 0 until level.length())
                if (level.map[i] == Terrain.WALL_DECO)
                    group.add(Sink(i))
        }

        // effects
        private class Sink(val pos: Int) : Emitter() {
            var rippleDelay = 0f

            init {
                val p = DungeonTilemap.tileCenterToWorld(pos)
                pos(p.x - 2, p.y + 1, 4f, 0f)

                pour(Sink.factory, 0.1f)
            }

            override fun update() {
                visible = Dungeon.visible[pos]
                if (visible) {
                    super.update()
                    rippleDelay -= Game.elapsed

                    if (rippleDelay <= 0) {
                        val ripple = GameScene.ripple(pos + Dungeon.level.width())
                        if (ripple != null) {
                            ripple.y -= (DungeonTilemap.SIZE / 2).toFloat()
                            rippleDelay = Random.Float(0.4f, 0.6f)
                        }
                    }
                }
            }

            companion object {
                private val factory = object : Emitter.Factory() {
                    override fun emit(emitter: Emitter, index: Int, x: Float, y: Float) {
                        val p = emitter.recycle(WaterParticle::class.java) as WaterParticle
                        p.reset(x, y)
                    }
                }
            }

        }

        // for out new instance, shall be public 
        class WaterParticle : PixelParticle() {
            init {
                acc.y = 50f
                am = .5f

                color(ColorMath.random(0xb6ccc2, 0x3b6653))
                size(2f)
            }

            fun reset(x: Float, y: Float) {
                revive()

                this.x = x
                this.y = y

                speed.set(Random.Float(-2f, +2f), 0f)

                lifespan = 0.5f
                left = lifespan
            }
        }
    }
}