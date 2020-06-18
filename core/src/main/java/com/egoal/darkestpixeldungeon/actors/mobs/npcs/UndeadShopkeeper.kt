package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.perks.Discount
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.books.TomeOfPerk
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.SimpleMobSprite
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.watabou.utils.Random
import kotlin.math.round

class UndeadShopkeeper : Merchant() {
    init {
        spriteClass = Sprite::class.java
    }

    private var itemChecked = false

    override fun actions(): ArrayList<Pair<String, String>> {
        val acts = super.actions()
        acts.add(AC_WHY_HERE to M.L(this, "ac_$AC_WHY_HERE"))
        return acts
    }

    override fun execute(action: String) {
        if (action == AC_WHY_HERE)
            WndDialogue.Show(this, M.L(this, "customer_filter")) {}
        else if (action == "buy" && !itemChecked) {
            WndDialogue.Show(this, M.L(this, "pay_to_see", infoPrice()), M.L(this, "unhappy"), M.L(this, "fine")) {
                if (it == 0) WndDialogue.Show(this, M.L(this, "info_costs")) {}
                else if (it == 1) {
                    val price = infoPrice()
                    if (Dungeon.gold < price) {
                        say(M.L(this, "no_money"))
                    } else {
                        Dungeon.gold -= price
                        itemChecked = true
                        spawnItems()
                        execute("buy")
                    }
                }
            }
        } else
            super.execute(action)
    }

    override fun initSellItems() {
        addItemToSell(Generator.SEED.generate()) // avoid empty initials
    }

    private fun infoPrice() = round((100 + 10 * Dungeon.depth) * (Dungeon.hero.heroPerk.get(Discount::class.java)?.ratio()
            ?: 1f)).toInt()

    private fun spawnItems() {
        var arts = 0
        for (i in 0 until 3) {
            val item = if (Random.Float() < 0.4f) {
                ++arts
                Generator.ARTIFACT.generate()
            } else Generator.RING.generate()
            addItemToSell(item)
        }
        if (arts < 1) addItemToSell(Generator.ARTIFACT.generate())

        if (Random.Float() < 0.05) addItemToSell(TomeOfPerk())
    }

    class Sprite : SimpleMobSprite(Assets.UNDEAD_SHOPKEEPER)

    companion object {
        private const val AC_WHY_HERE = "why_here"
    }
}