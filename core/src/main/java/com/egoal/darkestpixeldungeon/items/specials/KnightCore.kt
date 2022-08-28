package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Bundle

class KnightCore : Special() {
    private var honor: Int = 0

    init {
        image = ItemSpriteSheet.KNIGHT
    }

    override fun use(hero: Hero) {
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(HONOR, honor)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        honor = bundle.getInt(HONOR)
    }

    override fun status(): String? = if (honor > 0) "$honor" else null

    companion object {
        private val HONOR = "honor"
    }
}