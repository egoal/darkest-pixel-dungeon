package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class CollarOfSlave : Helmet() {
    init {
        image = ItemSpriteSheet.SLAVE_COLLAR;
    }
    fun stealth() = if (cursed) -2 else 2

    override fun desc(): String {
        var desc = super.desc()
        if (isIdentified) {
            desc += "\n\n" + M.L(this, "effect-desc")
            if (cursed)
                desc += "\n\n" + M.L(this, "cursed-desc")
        }

        return desc
    }
}