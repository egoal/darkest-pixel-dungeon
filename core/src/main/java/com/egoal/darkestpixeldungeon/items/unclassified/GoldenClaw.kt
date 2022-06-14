package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.GreedyMidas
import com.egoal.darkestpixeldungeon.actors.hero.perks.LevelPerception
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.NPC
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.HealthIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.Game
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.max

open class GoldenClaw : Item() {
    init {
        image = ItemSpriteSheet.GOLDEN_CLAW

        levelKnown = true
        cursedKnown = levelKnown
        unique = true
        bones = false

        defaultAction = AC_USE
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    override fun actions(hero: Hero): ArrayList<String> = arrayListOf(AC_USE)

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_USE)
            GameScene.selectItem(sellableSelector, WndBag.Mode.FOR_SALE, M.L(this, "select_to_convert"))
    }

    override fun doPickUp(hero: Hero): Boolean {
        if (hero.belongings.getItem(GoldenClaw.Evil::class.java) != null) return true

        return super.doPickUp(hero)
    }

    protected open fun gainGold(hero: Hero, q: Int) {
        val g = Gold(q)
        hero.heroPerk.get(GreedyMidas::class.java)?.procGold(g)
        if (g.quantity() > q) {
            GameScene.effect(Flare(5, 32f).color(0xffdd00, true).show(
                    hero.sprite.parent, DungeonTilemap.tileCenterToWorld(hero.pos), 1.5f))
        }

        hero.sprite.operate(hero.pos)
        hero.spend(1f)
        hero.busy()

        g.doPickUp(hero)
    }

    private val sellableSelector = WndBag.Listener {
        if (it != null) {
            val options = if (it.quantity() == 1) {
                arrayOf(M.L(GoldenClaw::class.java, "convert", it.price()),
                        M.L(GoldenClaw::class.java, "cancel"))
            } else {
                val price = it.price()
                arrayOf(M.L(GoldenClaw::class.java, "convert_one", price / it.quantity()),
                        M.L(GoldenClaw::class.java, "convert_all", price),
                        M.L(GoldenClaw::class.java, "cancel"))
            }

            val wnd = object : WndOptions(ItemSprite(it), it.name(), it.info(), *options) {
                override fun onSelect(index: Int) {
                    if (index == options.size - 1) return
                    if (options.size == 3 && index == 0) sellOne(it)
                    else sell(it)
                }

                private fun sell(item: Item) {
                    val hero = Dungeon.hero
                    if (item.isEquipped(hero) && !(item as EquipableItem).doUnequip(hero, false)) return

                    item.detachAll(hero.belongings.backpack)

                    this@GoldenClaw.gainGold(hero, item.price())
                }

                private fun sellOne(item: Item) {
                    assert(item.quantity() > 1)

                    val hero = Dungeon.hero
                    val detached = item.detach(hero.belongings.backpack)!!
                    this@GoldenClaw.gainGold(hero, detached.price())
                }
            }

            Game.scene().addToFront(wnd)
        }
    }

    companion object {
        private const val AC_USE = "use"
        private const val AC_EXEC = "exec"
    }

    class Evil : GoldenClaw() {
        init {
            image = ItemSpriteSheet.EVIL_GOLDEN_CLAW
            defaultAction = AC_EXEC
        }

        private var cooldown = 0

        override fun actions(hero: Hero): ArrayList<String> = arrayListOf(AC_EXEC)

        override fun execute(hero: Hero, action: String) {
            super.execute(hero, action)
            if (action === AC_EXEC) {
                WndOptions.Show(ItemSprite(this), name, "", M.L(this, "opt_mob"), M.L(this, "opt_item")) {
                    if (it == 0) {
                        if (cooldown > 0) GLog.w(M.L(Evil::class.java, "cooldown"))
                        else GameScene.selectCell(caster)
                    } else execute(hero, AC_USE)
                }
            }
        }

        override fun doPickUp(hero: Hero): Boolean {
            hero.belongings.getItem(GoldenClaw::class.java)?.detachAll(hero.belongings.backpack)

            val picked = super.doPickUp(hero)
            if (picked) Buff.affect(hero, Cooldown::class.java)

            return picked
        }

        override fun desc(): String = super.desc() + "\n" + M.L(this, "desc_hint")

        override fun status(): String? {
            return if (cooldown > 0) "$cooldown" else super.status()
        }

        private fun pointAt(mob: Mob) {
            if (mob is NPC || mob.properties().contains(Char.Property.PHANTOM)) {
                GLog.w(M.L(this, "invalid"))
                return
            }
            if (mob.properties().contains(Char.Property.BOSS) || mob.properties().contains(Char.Property.MINIBOSS)) {
                GLog.w(M.L(this, "too_strong"))
                return
            }

            // do it
            mob.destroy()
            mob.sprite.killAndErase()
            // Dungeon.level.mobs.remove(mob)
            HealthIndicator.instance.target(null)
            CellEmitter.get(mob.pos).burst(Speck.factory(Speck.COIN), Random.IntRange(10, 15))

            val gold = 20 + Random.Int(mob.exp() * 12, mob.exp() * 20)
            Dungeon.gold += gold
            Statistics.GoldCollected += gold

            cooldown += gold * 4 / 5
            updateQuickslot()

            val exp = max(1, mob.Config.EXP / 2) // extra exp
            Dungeon.hero.earnExp(exp)

            Dungeon.hero.sprite.showStatus(CharSprite.NEUTRAL, "+$gold")
            Dungeon.hero.spendAndNext(1f)

            Sample.INSTANCE.play(Assets.SND_GOLD, 1f, 1f, Random.Float(0.9f, 1.1f)) // todo: change this
        }

        override fun gainGold(hero: Hero, q: Int) {
            val g = Gold(q)
            hero.heroPerk.get(GreedyMidas::class.java)?.procGold(g)
            if (g.quantity() > q) {
                GameScene.effect(Flare(5, 32f).color(0xffdd00, true).show(
                        hero.sprite.parent, DungeonTilemap.tileCenterToWorld(hero.pos), 1.5f))
            }

            // g.doPickUp(hero)
            Dungeon.gold += g.quantity()
            Statistics.GoldCollected += g.quantity()

            hero.sprite.operate(hero.pos)
            hero.spend(1f)
            hero.busy()

            CellEmitter.get(hero.pos).burst(Speck.factory(Speck.COIN), Random.IntRange(6, 10))
            hero.sprite.showStatus(CharSprite.NEUTRAL, "+${g.quantity()}")

            Sample.INSTANCE.play(Assets.SND_GOLD, 1f, 1f, Random.Float(0.9f, 1.1f))
        }

        private val caster = object : CellSelector.Listener {
            override fun onSelect(cell: Int?) {
                if (cell != null && Level.fieldOfView[cell]) {
                    Dungeon.level.findMobAt(cell)?.let {
                        pointAt(it)
                    }
                }
            }

            override fun prompt(): String = M.L(Evil::class.java, "prompt")
        }

        class Cooldown : Buff() {
            private lateinit var evil: Evil

            override fun act(): Boolean {
                if (!::evil.isInitialized) {
                    evil = (target as Hero).belongings.getItem(Evil::class.java)!!
                }
                if (evil.cooldown > 0) {
                    --evil.cooldown
                    evil.updateQuickslot()
                }
                spend(Actor.TICK)
                return true
            }
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put("cooldown", cooldown)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            cooldown = bundle.getInt("cooldown")
        }
    }
}