package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class LittlePail : Helmet() {
    init {
        image = ItemSpriteSheet.LITTLE_PAIL
    }

    override fun procTakenDamage(dmg: Damage) {
        dmg.value -= Random.NormalIntRange(0, 2)
    }

    override fun random(): Item = this.apply { cursed = Random.Float() < 0.1f }
}