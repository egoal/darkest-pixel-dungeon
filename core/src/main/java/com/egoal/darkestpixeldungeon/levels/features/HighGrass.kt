package com.egoal.darkestpixeldungeon.levels.features

import com.egoal.darkestpixeldungeon.Challenges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Barkskin
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.LeafParticle
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.KGenerator
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
        if (level.map[pos] != Terrain.HIGH_GRASS) return

        Level.set(pos, Terrain.HIGH_GRASS_COLLECTED)

        if (!Dungeon.isChallenged(Challenges.NO_HERBALISM)) {
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
                    Level.set(pos, Terrain.GRASS)

                    val seed = KGenerator.SEED.generate()
                    if (seed is BlandfruitBush.Seed) {
                        if (Random.Int(15) - Dungeon.limitedDrops.blandfruitSeed.count >= 0) {
                            level.drop(seed, pos).sprite.drop()
                            Dungeon.limitedDrops.blandfruitSeed.count++
                        }
                    } else level.drop(seed, pos).sprite.drop()
                }

                // dew, 1/6 ~ 1/4
                if (Random.Int(12 - naturalismLevel) < 2) level.drop(Dewdrop(), pos)
            }
        }

        GameScene.updateMap(pos)

        var leaves = 4
        if (ch is Hero) {
            if (ch.subClass == HeroSubClass.WARDEN) {
                Buff.affect(ch, Barkskin::class.java).level(ch.HT / 10)
                leaves += 4
            }

            if (ch.belongings.armor?.hasGlyph(Camouflage::class.java) == true) {
                Buff.affect(ch, Camouflage.Camo::class.java).set(2 + ch.belongings.armor.level())
                leaves += 4
            }
        }

        CellEmitter.get(pos).burst(LeafParticle.LEVEL_SPECIFIC, leaves)
        if (ch != Dungeon.hero) Dungeon.observe()
    }
    
}