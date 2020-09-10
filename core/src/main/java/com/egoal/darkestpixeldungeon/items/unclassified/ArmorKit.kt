package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.armor.ClassArmor
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.noosa.audio.Sample
import java.util.ArrayList

class ArmorKit : Item() {
    init {
        image = ItemSpriteSheet.KIT
        unique = true
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_APPLY) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_APPLY) {
            curUser = hero
            GameScene.selectItem(armorSelector, WndBag.Mode.ARMOR, Messages.get(this, "prompt"))
        }
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    private fun upgrade(armor: Armor) {
        detach(curUser.belongings.backpack)

        val classArmor = ClassArmor.upgrade(curUser, armor)
        if (curUser.belongings.armor == armor) {
            curUser.belongings.armor = classArmor
            (curUser.sprite as HeroSprite).updateArmor()
        } else {
            armor.detach(curUser.belongings.backpack)
            classArmor.collect(curUser.belongings.backpack)
        }

        GLog.w(Messages.get(this, "upgraded", armor.name()))
        with(curUser) {
            sprite.centerEmitter().start(Speck.factory(Speck.KIT), 0.05f, 10)
            spend(10f)
            busy()
            sprite.operate(pos)
        }

        Sample.INSTANCE.play(Assets.SND_EVOKE)
    }

    private val armorSelector = WndBag.Listener {
        if (it != null)
            upgrade(it as Armor)
    }

    companion object {
        private const val TIME_TO_UPGRADE = 2f
        private const val AC_APPLY = "APPLY"
    }

}
