package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import android.util.Log
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ShopkeeperSprite
import com.egoal.darkestpixeldungeon.windows.WndBadge
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.utils.Bundle
import kotlin.collections.ArrayList

open class Merchant : NPC() {
    init {
        spriteClass = ShopkeeperSprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    private val items = ArrayList<Item>()

    override fun act(): Boolean {
        throwItem()
        sprite.turnTo(pos, Dungeon.hero.pos)
        spend(Actor.TICK)
        return true
    }

    override fun takeDamage(dmg: Damage): Int {
        flee()
        return 0
    }

    override fun add(buff: Buff) {
        flee()
    }

    override fun reset(): Boolean = true

    override fun interact(): Boolean {
        Journal.add(M.T(name))
        val actions = actions()
        val options = actions.map { M.L(this, it) }.toTypedArray()
        GameScene.show(object : WndOptions(sprite(), name, greeting(), *options) {
            override fun onSelect(index: Int) {
                execute(actions[index])
            }
        })

        return false
    }

    ///
    protected fun actions(): ArrayList<String> = arrayListOf(AC_BUY, AC_SELL)

    protected fun execute(action: String) {
        if (action == AC_BUY) {
        } else if (action == AC_SELL) {
//            val wnd = GameScene.selectItem({ item: Item ->
//
//            }, WndBag.Mode.FOR_SALE, M.L(DPDShopKeeper::class.java, "select_to_sell"))
        }
    }

    fun initSellItems() {
        //todo: may move painter things here
    }

    protected fun addItemToSell(item: Item, checkSimilar: Boolean = false): Boolean {
        if (items.contains(item)) return true

        if (checkSimilar) {
            val it = items.find { it.isSimilar(item) }
            if (it != null) {
                it.quantity(it.quantity() + item.quantity())
                return true
            }
        }

        return if (items.size < CAPACITY) {
            items.add(item)
            true
        } else {
            Log.e("dpd", "cannot add to merchant")
            false
        }
    }

    private fun removeItemFromSell(item: Item) = items.remove(item)

    protected fun flee() {
        Journal.remove(M.T(name))
        destroy()
        sprite.killAndErase()
        CellEmitter.get(pos).burst(ElmoParticle.FACTORY, 6)
    }

    private fun greeting(): String = M.L(this, "greeting")

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ITEM_STR, items)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        bundle.getCollection(ITEM_STR).filterNotNull().forEach { addItemToSell(it as Item) }
    }

    companion object {
        private const val CAPACITY = 25

        private const val ITEM_STR = "item"

        private const val AC_BUY = "buy"
        private const val AC_SELL = "sell"
    }
}