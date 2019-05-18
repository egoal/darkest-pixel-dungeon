package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.unclassified.PotionTestPaper
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.PotionSellerSprite
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

/**
 * Created by 93942 on 8/26/2018.
 */

open class PotionSeller : DPDShopKeeper() {

    init {
        spriteClass = PotionSellerSprite::class.java
    }

    override fun initSellItems(): DPDShopKeeper {
        // potions
        val cntItems = Random.NormalIntRange(3, 8)
        repeat(cntItems) {
            val item = if (Random.Float() < 0.6f) KGenerator.POTION.generate()
            else KGenerator.SEED.generate()

            // may be reinforced
            if (item is Potion && item.canBeReinforced() && Random.Float() < 0.3f)
                item.reinforce()
            addItemToSell(item)
        }
        addItemToSell(PotionOfHealing())
        addItemToSell(PotionTestPaper().quantity(Random.Int(1, 3)))
        
        shuffleItems()
        return this
    }

    override fun onPlayerStealFailed(hero: Hero) {
        Sample.INSTANCE.play(Assets.SND_SHATTER)

        if (Random.Float() < .7f)
            GameScene.add(Blob.seed(hero.pos, 1000, ToxicGas::class.java))
        else
            GameScene.add(Blob.seed(hero.pos, 2, Fire::class.java))

        super.onPlayerStealFailed(hero)
    }

    companion object {
        fun Random(): PotionSeller {
            val p = Random.Float()
            return if (p < 0.3f) PlagueDoctor() else PotionSeller()
        }
    }
}
