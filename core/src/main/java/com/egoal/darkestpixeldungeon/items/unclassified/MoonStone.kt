package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.MoonNight
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import java.util.ArrayList

class MoonStone : Item() {
    init {
        image = ItemSpriteSheet.MOON_STONE

        stackable = true
        defaultAction = AC_USE
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_USE) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_USE) {
            hero.spend(TIME_TO_USE)
            hero.busy()
            hero.sprite.operate(hero.pos)

            detach(hero.belongings.backpack)

            Use()
        }
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true
    override fun price(): Int = 40 * quantity

    companion object {
        private const val AC_USE = "use"
        private const val TIME_TO_USE = .5f

        fun Use(duration: Float = -1f) {
            Buff.prolong(Dungeon.hero, MoonNight::class.java, if (duration < 0) MoonNight.DURATION else duration)
            GLog.w(Messages.get(MoonStone::class.java, "used"))

            Dungeon.observe()
            GameScene.updateFog()
        }
    }

}