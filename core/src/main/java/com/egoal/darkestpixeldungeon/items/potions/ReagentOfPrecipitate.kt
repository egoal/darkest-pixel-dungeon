package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import kotlin.math.max

class ReagentOfPrecipitate : Reagent(false) {
    init {
        image = ItemSpriteSheet.REAGENT_PRECIPITATE

        defaultAction = AC_SMEAR
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_SMEAR)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_SMEAR) {
            GameScene.selectItem({
                detach(hero.belongings.backpack)
                val weapon = it as Weapon
                weapon.enchantment!!.left += max(weapon.enchantment!!.left, 10f)
                GLog.p(M.L(this, "smeared", weapon.name()))
            }, M.L(this, "select_item"), {
                it is Weapon && it.enchantment != null && it.enchantment!!.left > 0f
            })
        }
    }

    companion object {
        private const val AC_SMEAR = "SMEAR"
    }
}