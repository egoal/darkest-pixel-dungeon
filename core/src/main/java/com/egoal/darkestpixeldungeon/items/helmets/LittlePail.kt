package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class LittlePail : Helmet() {
    init {
        image = ItemSpriteSheet.LITTLE_PAIL
    }

    override fun procTakenDamage(dmg: Damage) {
        if (dmg.type == Damage.Type.NORMAL)
            dmg.value -= Random.NormalIntRange(0, 2)
    }

    override fun random(): Item = this.apply { cursed = Random.Float() < 0.1f }

    override fun desc(): String {
        var desc = super.desc()
        if (isIdentified) {
            desc += "\n\n" + Messages.get(this, "effect-desc")
            if (cursed)
                desc += "\n\n" + Messages.get(Helmet::class.java, "cursed_desc")
        }

        return desc
    }
}