package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import kotlin.math.min

//todo: may use events to handle this...
class HeroPerk : Bundlable {
    val perks = ArrayList<Perk>()

    fun <T> get(cls: Class<T>): T? where T : Perk = perks.find { cls.isInstance(it) } as T?
    fun has(cls: Class<out Perk>): Boolean = get(cls) != null

    //todo: merge to upgrade
    fun add(perk: Perk): Boolean {
        val similar = perks.find { perk.javaClass == it.javaClass }

        return if (similar != null) {
            similar.upgrade()
            true
        } else {
            if (perks.add(perk)) {
                perk.onGain()
                true
            } else
                false
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(STR_PERKS, perks)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        for (p in bundle.getCollection(STR_PERKS))
            if (p is Perk) perks.add(p)
    }

    companion object {
        private const val STR_PERKS = "perks"
    }
}


