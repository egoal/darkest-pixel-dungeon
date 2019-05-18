package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.items.potions.PotionOfToxicGas
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.watabou.noosa.TextureFilm

class PlagueDoctor : PotionSeller() {
    init {
        spriteClass = Sprite::class.java
    }

    override fun initSellItems(): DPDShopKeeper {
        addItemToSell(PotionOfToxicGas())
        return super.initSellItems()
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.PLAGUE_DOCTOR)

            // set animations
            val frames = TextureFilm(texture, 12, 15)
            idle = Animation(1, true)
            idle.frames(frames, 0, 1)

            die = Animation(20, false)
            die.frames(frames, 0)

            run = idle.clone()
            attack = idle.clone()

            play(idle)
        }
    }
}