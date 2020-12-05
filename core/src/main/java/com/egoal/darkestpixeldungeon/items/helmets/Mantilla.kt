package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class Mantilla : Helmet() {
    init {
        image = ItemSpriteSheet.MANTILLA
    }

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