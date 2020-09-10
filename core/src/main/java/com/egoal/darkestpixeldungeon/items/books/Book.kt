package com.egoal.darkestpixeldungeon.items.books

import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog

import java.util.ArrayList

/**
 * Created by 93942 on 5/10/2018.
 */

/*
* articles, notes, magic, so on
* this is like to be scroll, 
* but, not random, not consumable, 
* so, make a new class
* 
*/

abstract class Book : Item() {
    init {
        stackable = true
        defaultAction = AC_READ
        image = ItemSpriteSheet.DPD_BOOKS
    }

    override val isUpgradable: Boolean
        get() = false

    fun bookName(): String = Messages.get(this, "bookname")

    override fun name(): String = if (isIdentified) bookName() else super.name()

    override fun desc(): String = Messages.get(this, if (isIdentified) "bookdesc" else "desc")

    override fun isSimilar(item: Item): Boolean = this.isIdentified && item.isIdentified && super.isSimilar(item)

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_READ)

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_READ) {
            val pr = hero.canRead()

            if (pr.first) doRead(hero)
            else GLog.n(pr.second)
        }
    }

    protected abstract fun doRead(hero: Hero)

    override fun price(): Int = 30 * quantity

    companion object {
        private val AC_READ = "READ"
    }
}
