package com.egoal.darkestpixeldungeon.sprites

import com.egoal.darkestpixeldungeon.Assets

object DollSpriteSheets {
    const val BODY_ASSETS = Assets.HERO_BODY
    const val HEAD_ASSETS = Assets.HERO_HEAD
    const val ARMOR_ASSETS = Assets.HERO_ARMOR

    object Body {
        const val BASE = 0
    }

    object Head {
        const val WARRIOR = 0
        const val MAGE = 1
        const val ROUGE = 2
        const val HUNTRESS = 3
        const val SORCERESS = 4
    }

    object Armor {
        const val UNDERWARE_MALE = 0
        const val UNDERWARE_FEMALE = 1
        const val CLOTH_ARMOR = 2
        const val LEATHER_ARMOR = 3
        const val MAIL_ARMOR = 4
        const val SCALE_ARMOR = 5
        const val PLATE_ARMOR = 6
    }
}