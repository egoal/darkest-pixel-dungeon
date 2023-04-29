package com.egoal.darkestpixeldungeon.plants

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Barkskin
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.egoal.darkestpixeldungeon.effects.particles.LeafParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.artifacts.SandalsOfNature
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.PlantSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBadge
import com.egoal.darkestpixeldungeon.windows.WndBag
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
        //todo: refactor this
        if (this !is Rotberry && this !is BlandfruitBush)
            Dungeon.level.heaps.get(pos)?.let { heap ->
                val item = heap.peek()
                if (item is MeleeWeapon) {
                    item.enchant(Enchantment.ForPotion(
                            PlantTable.row { it.plant == this@Plant.javaClass }.potion), 10f)
                    // GLog.w(M.L(Potion::class.java, "enchanted", item.name(), item.enchantment!!.name()))
                }
            }

        wither()
        activate()
    }

    fun wither() {
        Dungeon.level.uproot(pos)

        sprite.kill()
        if (Dungeon.visible[pos]) CellEmitter.get(pos)!!.burst(LeafParticle.GENERAL, 6)

        // warden perk
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

    fun desc(): String = M.L(this, "desc")

    open class Seed : Item() {
        val plantClass: Class<out Plant> get() = PlantTable.row { it.seed == javaClass }.plant
        val alchemyClass: Class<out Potion> get() = PlantTable.row { it.seed == javaClass }.potion

        init {
            stackable = true
            defaultAction = AC_PLANT

            cursedKnown = true
            cursed = false
        }

        override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply {
            add(AC_PLANT)
            add(AC_SMEAR)
        }

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
            } else if (action == AC_SMEAR) {
                GameScene.selectItem({
                    if (it != null) {
                        detach(hero.belongings.backpack)
                        Enchantment.DoEnchant(hero, it as Weapon, alchemyClass, 6f)
                    }
                }, WndBag.Mode.SMEARABLE, M.L(Potion::class.java, "select_weapon"))
            }
        }

        fun couch(positon: Int): Plant {
            if (Dungeon.visible[positon]) Sample.INSTANCE.play(Assets.SND_PLANT)

            return plantClass.newInstance().apply { pos = positon }
        }

        override val isUpgradable: Boolean
            get() = false
        override val isIdentified: Boolean
            get() = true

        override fun price(): Int = 10 * quantity

        override fun desc(): String =
                Messages.get(plantClass, "desc")

        override fun info(): String = Messages.get(Seed::class.java, "info", desc())
    }

    companion object {
        private const val POS = "pos"
        private const val AC_PLANT = "PLANT"
        private const val AC_SMEAR = "SMEAR"
        private const val TIME_TO_PLANT = 1f
    }
}