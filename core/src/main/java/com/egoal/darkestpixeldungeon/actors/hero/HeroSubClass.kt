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
package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.utils.Bundle

enum class HeroSubClass(private val title: String) {
    NONE(""),

    GLADIATOR("gladiator"),
    BERSERKER("berserker"),

    WARLOCK("warlock"),
    BATTLEMAGE("battlemage"),

    ASSASSIN("assassin"),
    FREERUNNER("freerunner"),

    SNIPER("sniper"),
    WARDEN("warden"),

    STARGAZER("stargazer"),
    WITCH("witch");

    fun title(): String = Messages.get(this, title)

    fun desc(): String = Messages.get(this, title + "_desc")

    fun storeInBundle(bundle: Bundle) {
        bundle.put(SUBCLASS, toString())
    }

    companion object {

        private const val SUBCLASS = "subClass"

        fun RestoreFromBundle(bundle: Bundle): HeroSubClass = valueOf(bundle.getString(SUBCLASS))
    }

}
