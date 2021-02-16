package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.SimpleMobSprite
import com.watabou.utils.Random

class BarterMan : Merchant() {
    init {
        spriteClass = Sprite::class.java
    }

    override fun actions(): ArrayList<Pair<String, String>> = arrayListOf(
            AC_TRADE to M.L(this, "ac_$AC_TRADE"), AC_SWAP to M.L(this, "ac_$AC_SWAP")
    )

    override fun execute(action: String) {
        super.execute(action)

        if (action == AC_TRADE) {
            if (items.isEmpty()) tell(M.L(this, "nothing_more"))
            else GameScene.show(WndTrade())
        }
    }

    override fun initSellItems() {
        val np = Random.IntRange(1, 3)
        val ns = Random.IntRange(1, 3)
        val nw = Random.NormalIntRange(2, 6)
        val na = Random.IntRange(1, 2)

        repeat(np) { addItemToSell(Generator.POTION.generate()) }
        repeat(ns) { addItemToSell(Generator.SCROLL.generate()) }
        repeat(nw) { addItemToSell(Generator.WEAPON.generate()) }
        repeat(na) { addItemToSell(Generator.ARMOR.generate()) }

        for (item in items) item.level(0)
    }

    class Sprite : SimpleMobSprite(Assets.BARTER_MAN)

    companion object {
        private const val AC_TRADE = "trade"
        private const val AC_SWAP = "swap"
    }

    inner class WndTrade : WndShop() {
        override fun onWantBuy(item: Item, itemIndex: Int) {
            GameScene.selectItem({
                if (it != null) {
                    val hero = Dungeon.hero
                    if (it.isEquipped(Dungeon.hero)) (it as EquipableItem).doUnequip(Dungeon.hero, false)
                    else it.detach(hero.belongings.backpack)
                    removeItemFromSell(item)

                    if (!item.doPickUp(hero)) Dungeon.level.drop(item, hero.pos).sprite.drop()

                    goodsButtons[itemIndex].enable(false)
                    updateButtons()
                }
            }, M.L(BarterMan::class.java, "prompt_select"), {
                it.isIdentified && !it.cursed && when (item) {
                    is Potion -> it is Potion
                    is Scroll -> it is Scroll
                    is MeleeWeapon -> it is MeleeWeapon && item.tier == it.tier
                    is Armor -> it is Armor
                    else -> false
                }
            })
        }
    }
}