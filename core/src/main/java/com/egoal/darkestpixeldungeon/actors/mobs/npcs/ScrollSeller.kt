package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.perks.Discount
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfIdentify
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse
import com.egoal.darkestpixeldungeon.items.unclassified.Stylus
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ScrollSellerSprite
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.utils.Bundle
import com.watabou.utils.Random

class ScrollSeller : Merchant() {
    var avaliableCleanTimes = 1

    init {
        spriteClass = ScrollSellerSprite::class.java
    }

    override fun initSellItems() {
        addItemToSell(ScrollOfIdentify())
        addItemToSell(ScrollOfRemoveCurse())
        val cntItems = Random.Int(1, 4)
        for (i in 1..cntItems) addItemToSell(Generator.SCROLL.generate())

        val wand = Generator.WAND.generate()
        wand.cursed = false
        addItemToSell(wand)

        if (Random.Float() < .25f) {
            val wand2 = Generator.WAND.generate()
            wand2.cursed = false
            addItemToSell(wand2)
        }

        addItemToSell(Stylus().identify())

        shuffleItems()
    }

    override fun actions(): ArrayList<String> {
        val actions = super.actions()
        if (avaliableCleanTimes > 0) actions.add(AC_CLEAN)
        return actions
    }

    override fun execute(action: String) {
        super.execute(action)
        if (action == AC_CLEAN) {
            if (Dungeon.gold < feeClean()) {
                tell(M.L(this, "no_enough_gold"))
            } else {
                GameScene.selectItem(selectorClean, WndBag.Mode.UNIDED_OR_CURSED, M.L(this, "select_to_clean"))
            }
        }
    }

    private val selectorClean = WndBag.Listener { item ->
        if (item != null) {
            --avaliableCleanTimes
            Dungeon.gold -= feeClean()

            Flare(6, 32f).show(Dungeon.hero.sprite, 2f)
            val procced = ScrollOfRemoveCurse.uncurse(Dungeon.hero, item)
            if (procced) {
                Dungeon.hero.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)
                yell(M.L(ScrollSeller::class.java, "cleansed"))
            } else
                yell(M.L(ScrollSeller::class.java, "not_cleansed"))
        }
    }

    private fun feeClean(): Int {
        val fee = 40 * (Dungeon.depth + 4)
        val r = Dungeon.hero.heroPerk.get(Discount::class.java)?.ratio()
        return if (r != null) (r * fee).toInt() else fee
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(CLEAN_TIMES, avaliableCleanTimes)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        avaliableCleanTimes = bundle.getInt(CLEAN_TIMES)
    }

    companion object {
        private const val AC_CLEAN = "clean"
        private const val CLEAN_TIMES = "clean-times"
    }
}