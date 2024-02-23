package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import android.util.Log
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Journal
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.perks.Discount
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.MasterThievesArmband
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ShopkeeperSprite
import com.egoal.darkestpixeldungeon.ui.ItemSlot
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.windows.*
import com.watabou.noosa.BitmapText
import com.watabou.noosa.ColorBlock
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import kotlin.math.min

open class Merchant : NPC() {
    init {
        spriteClass = ShopkeeperSprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    protected val items = ArrayList<Item>()

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
        WndDialogue.Show(this, greeting(), *actions.map { it.second }.toTypedArray()) {
            execute(actions[it].first)
        }

        return false
    }

    /// merchant
    protected open fun actions(): ArrayList<Pair<String, String>> = arrayListOf(
            AC_BUY to M.L(this, "ac_$AC_BUY"), AC_SWAP to M.L(this, "ac_$AC_SWAP")
    )

    protected open fun execute(action: String) {
        if (action == AC_BUY) {
            if (items.isEmpty()) tell(M.L(this, "nothing_more"))
            else GameScene.show(WndShop())
        } else if (action == AC_SWAP) {
            swapPosition(Dungeon.hero)
            Dungeon.hero.spendAndNext(1f / Dungeon.hero.speed());
        }
    }

    open fun initSellItems() {
        //todo: may move painter things here
    }

    protected open fun onStealFailed(hero: Hero) {
        yell(M.L(this, "thief"))
        flee()
    }

    fun addItemToSell(item: Item, checkSimilar: Boolean = false): Boolean {
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

    protected fun removeItemFromSell(item: Item) = items.remove(item)

    fun shuffleItems() {
        items.shuffle()
    }

    protected open fun flee() {
        Journal.remove(M.T(name))
        destroy()
        sprite.killAndErase()
        CellEmitter.get(pos).burst(ElmoParticle.FACTORY, 6)
    }

    private fun greeting(): String = M.L(this, "greeting")

    private fun buyPrice(item: Item) = Dungeon.hero.heroPerk.get(Discount::class.java)?.buyPrice(item)
            ?: item.sellPrice()

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
        private const val AC_SWAP = "swap"

        //
        private const val SLOT_COLS = 5
        private const val SLOT_MARGIN = 1
        private const val GAP = 2f
        private const val MARGIN = 2f

        private const val GOODS_BTN_BG = -0x66aca9b3
        private const val GOODS_BTN_SIZE = 20f

        private const val SHOP_WIDTH = (GOODS_BTN_SIZE + SLOT_MARGIN) * SLOT_COLS + SLOT_MARGIN
    }

    // windows
    open inner class WndShop : Window() {
        protected val goodsButtons = ArrayList<GoodsButton>()

        init {
            val it = IconTitle(sprite(), name)
            it.setRect(MARGIN, MARGIN, SHOP_WIDTH - MARGIN * 2, 0f)
            add(it)

            val line = ColorBlock(SHOP_WIDTH - MARGIN * 2, 1f, 0xff222222.toInt())
            line.x = MARGIN
            line.y = it.bottom() + GAP
            add(line)

            val btm = placeItems(line.y + line.height() + GAP)
            updateButtons()

            resize(SHOP_WIDTH.toInt(), btm.toInt())
        }

        private fun placeItems(top: Float): Float {
            var btnHeight = 0f
            for (pr in items.withIndex()) {
                val r = pr.index / SLOT_COLS
                val c = pr.index % SLOT_COLS

                val btn = object : GoodsButton(pr.value) {
                    override fun onClick() {
                        onWantBuy(pr.value, pr.index)
                    }
                }
                btnHeight = btn.actualHeight()
                btn.setPos(c * (btn.width() + SLOT_MARGIN), top + r * (btn.actualHeight() + SLOT_MARGIN))
                add(btn)
                goodsButtons.add(btn)
            }

            val rows = (items.size - 1) / SLOT_COLS + 1
            return top + rows * (btnHeight + SLOT_MARGIN)
        }

        protected fun updateButtons() {
            for (btn in goodsButtons) {
                btn.redPrice(buyPrice(btn.item()) > Dungeon.gold)
            }
        }

        protected open fun onWantBuy(item: Item, itemIndex: Int) {
            val price = buyPrice(item)
            val actions = ArrayList<String>()
            if (price <= Dungeon.gold) actions.add(M.L(Merchant::class.java, "buy"))
            val thief = Dungeon.hero.buff(MasterThievesArmband.Thievery::class.java)
            if (thief != null)
                actions.add(M.L(Merchant::class.java, "steal", min(100, (thief.stealChance(price) * 100).toInt())))

            val wnd = object : WndOptions(ItemSprite(item), item.name(), item.info(), *actions.toTypedArray()) {
                override fun onSelect(index: Int) {
                    // todo: use action name instead
                    if (price <= Dungeon.gold && index == 0) {
                        // buy
                        val hero = Dungeon.hero
                        Dungeon.gold -= price
                        removeItemFromSell(item)
                        if (!item.doPickUp(hero)) Dungeon.level.drop(item, hero.pos).sprite.drop()
                        goodsButtons[itemIndex].enable(false)

                        updateButtons()
                    } else {
                        //steal
                        val hero = Dungeon.hero
                        if (thief!!.steal(price)) {
                            removeItemFromSell(item)
                            if (!item.doPickUp(hero)) Dungeon.level.drop(item, hero.pos).sprite.drop()
                            goodsButtons[itemIndex].enable(false)
                        } else {
                            this@WndShop.hide()
                            onStealFailed(hero)
                        }
                    }
                }
            }
            add(wnd)
        }
    }

    open inner class GoodsButton(item: Item) : ItemSlot(item) {
        private lateinit var bg: ColorBlock
        private lateinit var price: BitmapText
        private var price_hidden = false

        init {
            price.text(buyPrice(item).toString())
            price.measure()
            price.hardlight(0xffff00)

            width = GOODS_BTN_SIZE
            height = GOODS_BTN_SIZE // + price.height() + GAP
        }

        fun actualHeight() = GOODS_BTN_SIZE + price.height() + GAP

        fun item(): Item = item!!

        override fun createChildren() {
            bg = ColorBlock(GOODS_BTN_SIZE, GOODS_BTN_SIZE, GOODS_BTN_BG)
            add(bg)

            price = BitmapText(PixelScene.pixelFont)
            add(price)

            super.createChildren()
        }

        override fun layout() {
            super.layout()

            bg.x = x
            bg.y = y

            price.x = bg.x + bg.width() / 2f - price.width() / 2f
            price.y = bg.y + bg.height() + GAP
        }

        override fun onTouchDown() {
            bg.brightness(1.5f)
            Sample.INSTANCE.play(Assets.SND_CLICK, 0.7f, 0.7f, 1.2f)
        }

        override fun onTouchUp() {
            bg.resetColor()
        }

        fun redPrice(on: Boolean = true) {
            if (price_hidden) {
                price.hardlight(0x808080)
            } else {
                if (on) price.hardlight(0xff0000)
                else price.hardlight(0xffff00)
            }
        }

        fun hidePrice() {
            price_hidden = true
            price.text("---")
            redPrice()
        }
    }
}