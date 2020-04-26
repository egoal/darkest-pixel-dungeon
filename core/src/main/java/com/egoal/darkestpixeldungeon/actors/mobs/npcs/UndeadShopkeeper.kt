package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.sprites.SimpleMobSprite

class UndeadShopkeeper : Merchant() {
    init {
        spriteClass = Sprite::class.java
    }
    
    
    
    class Sprite: SimpleMobSprite(Assets.UNDEAD_SHOPKEEPER)
}