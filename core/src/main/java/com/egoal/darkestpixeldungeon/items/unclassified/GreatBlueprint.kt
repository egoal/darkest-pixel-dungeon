package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.CloakOfShadows
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Boomerang
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.noosa.audio.Sample
import java.util.ArrayList

class GreatBlueprint : Item() {
    init {
        image = ItemSpriteSheet.GREAT_BLUEPRINT

        unique = true
    }

    override fun isUpgradable(): Boolean = false

    override fun isIdentified(): Boolean = true

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_USE) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_USE) {
            curUser = hero

            GameScene.selectItem(itemSelector, M.L(this, "prompt"), filter)
        }
    }

    // todo...
    private val filter = WndBag.Filter { it is Enchantable }

    private val itemSelector = WndBag.Listener {
        if (it != null && filter.enable(it)) {
            enhance(it)
        }
    }

    private fun enhance(item: Item) {
        detach(Dungeon.hero.belongings.backpack)

        // enhance
        (item as Enchantable).enchantByBlueprint()

        GLog.w(M.L(this, "enhanced", item.name()))
        with(curUser) {
            sprite.centerEmitter().start(Speck.factory(Speck.KIT), 0.05f, 10)
            spend(10f)
            busy()
            sprite.operate(pos)
        }
        Sample.INSTANCE.play(Assets.SND_EVOKE)
        updateQuickslot()
    }

    companion object {
        private const val AC_USE = "USE"
    }
    
    interface Enchantable{
        fun enchantByBlueprint()
    }

}