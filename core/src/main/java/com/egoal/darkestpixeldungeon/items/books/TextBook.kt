package com.egoal.darkestpixeldungeon.items.books

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.windows.WndTextBook

import java.util.ArrayList

/**
 * Created by 93942 on 10/14/2018.
 */

open class TextBook : Book() {
    private var pageSize = -1

    fun pageSize(): Int {
        if (pageSize < 0)
            pageSize = Integer.parseInt(Messages.get(this, "pagesize"))
        return pageSize
    }

    fun page(i: Int): String = Messages.get(this, "page$i")

    override fun doRead(hero: Hero) {
        identify()
        GameScene.show(WndTextBook(this))
    }

    override fun price(): Int = quantity() * pageSize() * 5
}
