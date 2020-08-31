package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Drunk
import com.egoal.darkestpixeldungeon.actors.buffs.Tipsy
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndConfirm
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.min

class Gourd : Item(), GreatBlueprint.Enchantable {
    init {
        image = ItemSpriteSheet.GOURD

        defaultAction = AC_DRINK
        unique = true
    }

    private var volume = 4
    private var enchanted = false

    override fun isIdentified(): Boolean = true
    override fun isUpgradable(): Boolean = false

    override fun status(): String = "$volume/$CAPACITY"

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply {
        add(AC_DRINK)
        add(AC_IRRIGATE)
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_DRINK) {
            if (volume == 0) hero.say(M.L(this, "empty"))
            else {
                drink(hero)
            }
        } else if (action == AC_IRRIGATE) {
            if (volume >= CAPACITY) GLog.i(M.L(this, "full"))
            else GameScene.selectItem(wineSelector, M.L(this, "select_wine"), WndBag.Filter { it is Wine })
        }
    }

    private fun irrigate(wine: Wine) {
        if (wine.gourdValue + volume > CAPACITY) {
            GameScene.show(WndConfirm(ItemSprite(this), name(), M.L(this, "no_enough_space")) {
                volume = CAPACITY - wine.gourdValue
                irrigate(wine)
            })
        }

        wine.detach(curUser.belongings.backpack)
        volume += wine.gourdValue

        curUser.sprite.operate(curUser.pos)
        curUser.spendAndNext(1f)
        GLog.i(M.L(this, "irrigated", wine.name()))

        updateQuickslot()
    }

    private fun drink(hero: Hero) {
        val dv = min(2, volume)
        volume -= dv

        var value = min(Random.Float(2f * dv, hero.pressure.pressure * 0.2f * dv), 12f)

        // exile cannot get Drunkard perk.
        val tipsy = hero.buff(Tipsy::class.java)
        if (tipsy == null) {

            Buff.prolong(hero, Tipsy::class.java, 15f * dv)
            if (enchanted) value += value / 3f
        } else {
            tipsy.detach()
            Buff.prolong(hero, Drunk::class.java, Drunk.duration(hero) + tipsy.cooldown() / 2f)
        }
        hero.recoverSanity(value)

        hero.sprite.operate(hero.pos)
        Sample.INSTANCE.play(Assets.SND_DRINK)
        hero.spendAndNext(2f)

        updateQuickslot()
    }

    private val wineSelector = WndBag.Listener {
        if (it is Wine) irrigate(it)
    }

    override fun enchantByBlueprint() {
        enchanted = true
    }

    override fun desc(): String {
        var desc = super.desc()
        if (enchanted) desc += "\n\n" + M.L(this, "enchant_desc")
        return desc
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(VOLUME, volume)
        bundle.put(ENCHANTED, enchanted)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        volume = bundle.getInt(VOLUME)
        enchanted = bundle.getBoolean(ENCHANTED)
    }

    companion object {
        private const val AC_DRINK = "drink"
        private const val AC_IRRIGATE = "irrigate"

        private const val VOLUME = "volume"
        private const val ENCHANTED = "enchanted"

        private const val CAPACITY = 20
    }
}

