package com.egoal.darkestpixeldungeon.levels.features

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Barkskin
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.LeafParticle

import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Camouflage
import com.egoal.darkestpixeldungeon.items.artifacts.SandalsOfNature
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.plants.BlandfruitBush
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.utils.Random

object HighGrass {
    fun Trample(level: Level, pos: Int, ch: Char?) {
        // called only if HIGH_GRASS, HIGH_GRASS_COLLECTED, see Level::press
        if (level.map[pos] == Terrain.HIGH_GRASS) {
            Level.set(pos, Terrain.HIGH_GRASS_COLLECTED)
            val prize = Harvest(level, pos, ch)
            if (prize != null) level.drop(prize, pos).sprite.drop()

            if (prize is Plant.Seed || Dungeon.depth == 0) Level.set(pos, Terrain.GRASS)

            GameScene.updateMap(pos)
        }

        var leaves = 4
        if (ch is Hero) {
            if (ch.subClass == HeroSubClass.WARDEN) {
                Buff.affect(ch, Barkskin::class.java).level(ch.HT / 10)
                leaves += 4
            }

            if (ch.belongings.armor?.hasGlyph(Camouflage::class.java) == true) {
                Buff.affect(ch, Camouflage.Camo::class.java).set(2 + ch.belongings.armor!!.level())
                leaves += 4
            }
        }

        CellEmitter.get(pos).burst(LeafParticle.LEVEL_SPECIFIC, leaves)
        if (ch != Dungeon.hero) Dungeon.observe()
    }

    fun Harvest(level: Level, pos: Int, ch: Char?): Item? {
        var naturalismLevel = 0
        ch?.buff(SandalsOfNature.Naturalism::class.java)?.let { naturalism ->
            naturalismLevel = if (naturalism.isCursed) -1 else {
                naturalism.charge()
                naturalism.itemLevel() + 1
            }
        }

        if (naturalismLevel >= 0) {
            // less seed in the village
            val chance = if (Dungeon.depth == 0) 30 else (18 - naturalismLevel * 3)
            if (Random.Int(chance) == 0) {
                //^ drop seed
                val seed = Generator.SEED.generate()
                if (seed is BlandfruitBush.Seed) {
                    if (Random.Int(15) - Dungeon.limitedDrops.blandfruitSeed.count >= 0) {
                        Dungeon.limitedDrops.blandfruitSeed.count++
                    } else return null
                }

                return seed
            }

            // dew, 1/6 ~ 1/4
            if (Random.Int(12 - naturalismLevel) < 2) return Dewdrop()
        }

        return null
    }

}