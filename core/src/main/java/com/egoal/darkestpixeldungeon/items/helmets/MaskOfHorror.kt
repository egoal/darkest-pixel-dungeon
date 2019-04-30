package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class MaskOfHorror : Helmet() {
    init {
        image = ItemSpriteSheet.HELMET_HORROR
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isIdentified) {
            desc += "\n\n" + Messages.get(this, "effect-desc")
            if (cursed)
                desc += "\n\n" + Messages.get(this, "cursed-desc")
        }

        return desc
    }

    override fun procGivenDamage(dmg: Damage) {
        if (!cursed && Random.Float() < 0.1f)
            dmg.addFeature(Damage.Feature.ACCURATE)
    }
}