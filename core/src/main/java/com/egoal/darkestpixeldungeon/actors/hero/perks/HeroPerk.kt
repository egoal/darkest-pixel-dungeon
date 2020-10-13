package com.egoal.darkestpixeldungeon.actors.hero.perks

import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle

//todo: may use events to handle this...
class HeroPerk : Bundlable {
    val perks = ArrayList<Perk>()

    fun <T> get(cls: Class<T>): T? where T : Perk = perks.find { cls.isInstance(it) } as T?
    fun has(cls: Class<out Perk>): Boolean = get(cls) != null

    fun add(perk: Perk): Boolean {
        val similar = perks.find { perk.javaClass == it.javaClass }

        return if (similar != null) {
            if (similar.level == -1) {
                perks.remove(similar)
                similar.onLose()
            } else similar.upgrade()
            true
        } else {
            if (perks.add(perk)) {
                perk.onGain()
                true
            } else
                false
        }
    }

    // todo:
    fun downgrade(perk: Perk) {
        val it = perks.find { perk.javaClass == it.javaClass }!!

        if (it.level > 0) it.downgrade()
        else if (it.level < 0) it.upgrade()

        if (it.level == 0) {
            perks.remove(it)
            it.onLose()
        }
    }

    fun remove(perk: Perk) {
        val it = perks.find { perk.javaClass == it.javaClass }!!
        perks.remove(it)
        it.onLose()
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


