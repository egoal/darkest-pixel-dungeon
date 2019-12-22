package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.GreedyMidas
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.Game
import java.util.ArrayList

class GoldenClaw : Item() {
    init {
        image = ItemSpriteSheet.GOLDEN_CLAW

        levelKnown = true
        cursedKnown = levelKnown
        unique = true
        bones = false

        defaultAction = AC_USE
    }

    override fun isUpgradable(): Boolean = false

    override fun actions(hero: Hero): ArrayList<String> = arrayListOf(AC_USE)

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_USE)
            GameScene.selectItem(sellableSelector, WndBag.Mode.FOR_SALE, M.L(this, "select_to_convert"))
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

                    addGold(hero, item.price())
                }

                private fun sellOne(item: Item) {
                    assert(item.quantity() > 1)

                    val hero = Dungeon.hero
                    val detached = item.detach(hero.belongings.backpack)
                    addGold(hero, detached.price())
                }

                private fun addGold(hero: Hero, q: Int) {
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
            }

            Game.scene().addToFront(wnd)
        }
    }

    companion object {
        private const val AC_USE = "use"
    }
}