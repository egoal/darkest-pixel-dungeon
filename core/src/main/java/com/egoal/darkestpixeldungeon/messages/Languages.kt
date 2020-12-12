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
package com.egoal.darkestpixeldungeon.messages

import java.util.Locale

enum class Languages(val nativeName: String,
                     val locale: Locale,
                     val status: Status,
                     val reviewers: Array<String>,
                     val translators: Array<String>) {
    ENGLISH("english", Locale.ENGLISH, Status.REVIEWED,
            arrayOf<String>("Egoal", "endlesssolitude", " 路人NPC"),
            arrayOf<String>(" 1834515403a", "Fevre", "Fishbone", "MrKukurykpl", "Omicronrg9", "Piedro0", "SeaMonser", "riwansia", "shenlingfeiniao")),

    CHINESE_TR("繁体中文", Locale.TRADITIONAL_CHINESE, Status.INCOMPLETE, arrayOf<String>("Egoal"), arrayOf<String>("那些回忆")),

    CHINESE("中文", Locale.CHINESE, Status.REVIEWED, arrayOf<String>("Jinkeloid(zdx00793)"),
            arrayOf<String>("931451545", "HoofBumpBlurryface", "Lery", "Lyn-0401", "ShatteredFlameBlast", "Hmdzl001", "Tempest102"));

    enum class Status {
        //below 60% complete languages are not added.
        INCOMPLETE, //60-99% complete
        UNREVIEWED, //100% complete
        REVIEWED    //100% reviewed
    }

    val code: String get() = locale.toString()

    companion object {
        private val DEFAULT = CHINESE

        fun matchLocale(locale: Locale): Languages = values().find { it.locale == locale } ?: DEFAULT

        fun matchCode(code: String): Languages = values().find { it.code == code } ?: DEFAULT
    }

}