package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import java.util.ArrayList

abstract class Special : Item() {
    init {
        unique = true
        bones = false
        stackable = false

        defaultAction = AC_USE
    }

    override val isIdentified: Boolean
        get() = true
    override val isUpgradable: Boolean
        get() = false

    override fun actions(hero: Hero): ArrayList<String> = arrayListOf(AC_USE)

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_USE) use(hero)
    }

    protected open fun use(hero: Hero) {}

    open fun tick() {}

    companion object {
        protected const val AC_USE = "use"
    }
}