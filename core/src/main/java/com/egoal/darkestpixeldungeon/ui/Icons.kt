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
package com.egoal.darkestpixeldungeon.ui

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.watabou.noosa.Image

enum class Icons {
    SKULL,
    BUSY,
    COMPASS,
    INFO,
    PREFS,
    WARNING,
    TARGET,
    MASTERY,

    WATA,
    SHPX,

    WARRIOR,
    MAGE,
    ROGUE,
    HUNTRESS,
    SORCERESS,
    EXILE,

    CLOSE,
    DEPTH,
    DEPTH_LG,
    SLEEP,
    ALERT,
    BACKPACK,
    SEED_POUCH,
    SCROLL_HOLDER,
    POTION_BANDOLIER,
    WAND_HOLSTER,
    SKILL_TREE,
    CHECKED,
    UNCHECKED,
    EXIT,
    NOTES,
    CHALLENGE_OFF,
    CHALLENGE_ON,
    RESUME,

    DOT_OFF,
    DOT_ON,
    PERK,

    ARROW_RIGHT,
    TORCH,

    LIX;

    fun get(): Image {
        return get(this)
    }

    companion object {

        operator fun get(type: Icons): Image {
            val icon = Image(Assets.DPD_ICONS)
            when (type) {
                SKULL -> icon.frame(icon.texture.uvRect(0, 0, 8, 8))
                BUSY -> icon.frame(icon.texture.uvRect(8, 0, 16, 8))
                COMPASS -> icon.frame(icon.texture.uvRect(0, 8, 7, 13))
                INFO -> icon.frame(icon.texture.uvRect(16, 0, 30, 14))
                PREFS -> icon.frame(icon.texture.uvRect(30, 0, 46, 16))
                WARNING -> icon.frame(icon.texture.uvRect(46, 0, 58, 12))
                TARGET -> icon.frame(icon.texture.uvRect(0, 13, 16, 29))
                MASTERY -> icon.frame(icon.texture.uvRect(16, 14, 30, 28))

                WATA -> icon.frame(icon.texture.uvRect(30, 16, 45, 26))
                SHPX -> icon.frame(icon.texture.uvRect(64, 48, 80, 64))
                LIX -> icon.frame(icon.texture.uvRect(80, 48, 96, 64))

                WARRIOR -> icon.frame(icon.texture.uvRect(0, 29, 16, 45))
                MAGE -> icon.frame(icon.texture.uvRect(16, 29, 32, 45))
                ROGUE -> icon.frame(icon.texture.uvRect(32, 29, 48, 45))
                HUNTRESS -> icon.frame(icon.texture.uvRect(48, 29, 64, 45))
                SORCERESS -> icon.frame(icon.texture.uvRect(64, 29, 80, 45))
                EXILE -> icon.frame(icon.texture.uvRect(0, 66, 16, 82))

                CLOSE -> icon.frame(icon.texture.uvRect(0, 45, 13, 58))
                DEPTH -> icon.frame(icon.texture.uvRect(46, 12, 54, 20))
                DEPTH_LG -> icon.frame(icon.texture.uvRect(34, 46, 50, 62))
                SLEEP -> icon.frame(icon.texture.uvRect(13, 45, 22, 53))
                ALERT -> icon.frame(icon.texture.uvRect(22, 45, 30, 53))
                BACKPACK -> icon.frame(icon.texture.uvRect(58, 0, 68, 10))
                SCROLL_HOLDER -> icon.frame(icon.texture.uvRect(68, 0, 78, 10))
                SEED_POUCH -> icon.frame(icon.texture.uvRect(78, 0, 88, 10))
                WAND_HOLSTER -> icon.frame(icon.texture.uvRect(88, 0, 98, 10))
                POTION_BANDOLIER -> icon.frame(icon.texture.uvRect(98, 0, 108, 10))
                SKILL_TREE-> icon.frame(icon.texture.uvRect(32, 64, 42, 74))

                CHECKED -> icon.frame(icon.texture.uvRect(54, 12, 66, 24))
                UNCHECKED -> icon.frame(icon.texture.uvRect(66, 12, 78, 24))
                EXIT -> icon.frame(icon.texture.uvRect(108, 0, 124, 16))
                NOTES -> icon.frame(icon.texture.uvRect(95, 42, 107, 55))
                CHALLENGE_OFF -> icon.frame(icon.texture.uvRect(80, 16, 104, 40))
                CHALLENGE_ON -> icon.frame(icon.texture.uvRect(104, 16, 128, 40))
                RESUME -> icon.frame(icon.texture.uvRect(13, 53, 24, 64))
                DOT_OFF -> icon.frame(icon.texture.uvRect(109, 42, 114, 47))
                DOT_ON -> icon.frame(icon.texture.uvRect(114, 42, 119, 47))
                ARROW_RIGHT -> icon.frame(icon.texture.uvRect(109, 47, 116, 59))

                PERK -> icon.frame(icon.texture.uvRect(0, 57, 8, 65))
                TORCH -> icon.frame(icon.texture.uvRect(16, 64, 32, 80))
            }

            return icon
        }

        operator fun get(cl: HeroClass): Image = when (cl) {
            HeroClass.WARRIOR -> get(WARRIOR)
            HeroClass.MAGE -> get(MAGE)
            HeroClass.ROGUE -> get(ROGUE)
            HeroClass.HUNTRESS -> get(HUNTRESS)
            HeroClass.SORCERESS -> get(SORCERESS)
            HeroClass.EXILE -> get(EXILE)
        }
    }
}
