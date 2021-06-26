/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.items.books.Book
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.windows.WndOptions

import java.util.ArrayList

class TomeOfMastery : Item() {
    init {
        stackable = false
        image = ItemSpriteSheet.MASTERY

        unique = true
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_READ) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_READ) {
            if (hero.heroClass.subClasses.isEmpty()) {
                GLog.w(M.L(Book::class.java, "cannot_understand"))
                return
            }

            curUser = hero

            val heroClass = hero.heroClass
            var message = heroClass.subClasses[0].desc()
            for (i in 1 until heroClass.subClasses.size)
                message += "\n\n" + heroClass.subClasses[i].desc()

            WndOptions.Show(ItemSprite(this), name(), message, *heroClass.subClasses.map { it.title() }.toTypedArray()) {
                choose(heroClass.subClasses[it])
            }
        }
    }

    override fun doPickUp(hero: Hero): Boolean {
        Badges.validateMastery()
        return super.doPickUp(hero)
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    fun choose(way: HeroSubClass) {
        detach(curUser.belongings.backpack)

        with(curUser) {
            spend(TIME_TO_READ)
            busy()

            subClass = way
            sprite.operate(pos)
        }

        HeroSubClass.Choose(curUser, way)
    }

    companion object {

        private const val TIME_TO_READ = 10f

        private const val AC_READ = "READ"
    }
}
