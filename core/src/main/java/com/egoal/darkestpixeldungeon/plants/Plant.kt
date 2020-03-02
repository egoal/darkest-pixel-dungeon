package com.egoal.darkestpixeldungeon.plants

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Barkskin
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.LeafParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.artifacts.SandalsOfNature
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.PlantSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList

abstract class Plant(val image: Int) : Bundlable {
    var pos: Int = 0

    lateinit var sprite: PlantSprite

    val plantName = Messages.get(this, "name")

    fun trigger() {
        Actor.findChar(pos)?.let {
            if (it is Hero && it.subClass == HeroSubClass.WARDEN)
                Buff.affect(it, Barkskin::class.java).level(it.HT / 3)
        }

        wither()
        activate()
    }

    fun wither() {
        Dungeon.level.uproot(pos)

        sprite.kill()
        if (Dungeon.visible[pos]) CellEmitter.get(pos)!!.burst(LeafParticle.GENERAL, 6)

        if (Dungeon.hero.subClass == HeroSubClass.WARDEN) {
            val naturalismLevel = 1 +
                    (Dungeon.hero.buff(SandalsOfNature.Naturalism::class.java)?.itemLevel() ?: -1)

            if (Random.Int(5 - (naturalismLevel / 2)) == 0) {
                // extra seed
                val seed = Generator.SEED.generate()

                if (seed is BlandfruitBush.Seed) {
                    if (Random.Int(15) - Dungeon.limitedDrops.blandfruitSeed.count >= 0) {
                        Dungeon.level.drop(seed, pos).sprite.drop()
                        Dungeon.limitedDrops.blandfruitSeed.count++
                    }
                } else Dungeon.level.drop(seed, pos).sprite.drop()
            }
            // extra dewdrop
            if (Random.Int(5 - naturalismLevel) == 0)
                Dungeon.level.drop(Dewdrop(), pos).sprite.drop()
        }
    }

    abstract fun activate()

    override fun restoreFromBundle(bundle: Bundle) {
        pos = bundle.getInt(POS)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(POS, pos)
    }

    fun desc(): String = Messages.get(this, "desc")

    open class Seed(private val plantClass: Class<out Plant>, 
                    val alchemyClass: Class<out Potion>) : Item() {
        init {
            stackable = true
            defaultAction = AC_PLANT

            cursedKnown = true
            cursed = false
        }

        override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_PLANT) }

        override fun onThrow(cell: Int) {
            if (Dungeon.level.map[cell] == Terrain.ALCHEMY ||
                    Level.pit[cell] ||
                    Dungeon.level.traps.get(cell) != null) {
                super.onThrow(cell)
            } else {
                Dungeon.level.plant(this, cell)
            }
        }

        override fun execute(hero: Hero, action: String) {
            super.execute(hero, action)

            if (action == AC_PLANT) {
                hero.spend(TIME_TO_PLANT)
                hero.busy()

                (detach(hero.belongings.backpack) as Seed).onThrow(hero.pos)

                hero.sprite.operate(hero.pos)
            }
        }

        fun couch(positon: Int): Plant {
            if (Dungeon.visible[positon]) Sample.INSTANCE.play(Assets.SND_PLANT)

            return plantClass.newInstance().apply { pos = positon }
        }

        override fun isUpgradable(): Boolean = false

        override fun isIdentified(): Boolean = true

        override fun price(): Int = 10 * quantity

        override fun desc(): String = Messages.get(plantClass, "desc")

        override fun info(): String = Messages.get(Seed::class.java, "info", desc())
    }


    companion object {
        private const val POS = "pos"

        private const val AC_PLANT = "PLANT"

        private const val TIME_TO_PLANT = 1f

    }
}