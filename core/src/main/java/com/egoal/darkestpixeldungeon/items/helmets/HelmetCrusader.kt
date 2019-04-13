package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.actors.Char
import com.watabou.utils.Random

class HelmetCrusader : Helmet() {
    init {
        image = ItemSpriteSheet.HELMET_CRUSADER
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isIdentified) {
            desc += "\n\n" + Messages.get(this, "effect_desc")
            if (cursed)
                desc += "\n\n" + Messages.get(Helmet::class.java, "cursed_desc")
        }

        return desc
    }

    override fun procTakenDamage(dmg: Damage): Damage {
        if (dmg.isFeatured(Damage.Feature.RANGED) && Random.Float() < 0.2f)
            dmg.value = 0
        return dmg
    }

    override fun viewAmend(): Int = -1
}