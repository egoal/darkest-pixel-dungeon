package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.noosa.audio.Sample

open class Reagent(val drinkable: Boolean) : Item() {
    init {
        bones = true

        defaultAction = if (drinkable) AC_DRINK else AC_THROW
        stackable = true
        cursedKnown = true
        cursed = false
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    override fun price(): Int = 15 * quantity

    override fun desc(): String {
        val desc = super.desc()
        return if (!drinkable) desc + "\n" + M.L(this, "not_drinkable")
        else desc
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (drinkable) actions.add(AC_DRINK)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_DRINK) drink(hero)
    }

    protected open fun drink(hero: Hero) {
        detach(hero.belongings.backpack)

        hero.spend(1f)
        hero.busy()

        Sample.INSTANCE.play(Assets.SND_DRINK)
        hero.sprite.operate(hero.pos)

        // apply(hero)
    }

    override fun onThrow(cell: Int) {
        if (Dungeon.level.map[cell] == Terrain.WELL || Level.pit[cell])
            super.onThrow(cell)
        else {
            Dungeon.level.press(cell, null)
            shatter(cell)
        }
    }

    protected open fun shatter(cell: Int) {
        if (Dungeon.visible[cell]) {
            Sample.INSTANCE.play(Assets.SND_SHATTER)

            val color = ItemSprite.pick(image, 7, 8)
            Splash.at(cell, color, 5)
        }
    }

    companion object {
        const val AC_DRINK = "DRINK"
    }
}