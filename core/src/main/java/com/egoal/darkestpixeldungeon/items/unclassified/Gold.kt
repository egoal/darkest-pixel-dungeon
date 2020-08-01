package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.GoldPlatedStatue
import com.egoal.darkestpixeldungeon.items.artifacts.MasterThievesArmband
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

class Gold(value: Int = 1) : Item() {
    init {
        image = ItemSpriteSheet.GOLD
        stackable = true

        quantity = value
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = ArrayList<String>()
        if (hero.challenge == Challenge.CastingMaster) actions.add(AC_CAST)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_CAST)
            GameScene.selectItem(itemSelector, WndBag.Mode.UPGRADEABLE, M.L(Challenge::class.java, "select_upgrade"))
    }

    override fun doPickUp(hero: Hero): Boolean {
        if (hero.challenge == Challenge.GreedIsGood) {
            GLog.n(M.L(Challenge::class.java, "gone", name()))
            hero.next()
            return true
        }

        val greedyCollect = hero.buff(GoldPlatedStatue.Greedy::class.java)?.extraCollect(quantity)
                ?: 0

        val get = quantity + greedyCollect

        Dungeon.gold += get
        Statistics.GoldCollected += get
        Badges.validateGoldCollected()

        hero.buff(MasterThievesArmband.Thievery::class.java)?.let { thievery ->
            thievery.collect(get)
        }
        GameScene.pickUp(this)

        hero.sprite.showStatus(CharSprite.NEUTRAL, if (greedyCollect == 0) "+$get" else "+$quantity(+$greedyCollect)")
        hero.spendAndNext(TIME_TO_PICK_UP)

        Sample.INSTANCE.play(Assets.SND_GOLD, 1f, 1f, Random.Float(0.9f, 1.1f))

        return true
    }

    override fun isUpgradable(): Boolean = false
    override fun isIdentified(): Boolean = true

    override fun random(): Item = this.apply { quantity = Random.Int(20 + Dungeon.depth * 8, 40 + Dungeon.depth * 16) }

    private val itemSelector = WndBag.Listener { item ->
        if (item != null)
            upgradeItem(curUser, item)
    }

    private fun upgradeItem(hero: Hero, item: Item) {
        val lvl = item.level()
        val goldreq = 100 + lvl * 120 + (lvl / 3) * 66 * lvl
        if (goldreq > Dungeon.gold) {
            hero.sayShort(HeroLines.NO_GOLD)
            return
        }

        Dungeon.gold -= goldreq
        ScrollOfUpgrade().onItemSelected(item)
        with(hero) {
            sprite.centerEmitter().start(Speck.factory(Speck.KIT), 0.05f, min(10, 2 + item.level() * 2))
            spend(3f)
            busy()
            sprite.operate(pos)

            buff(Hunger::class.java)?.let {
                if (!it.isStarving) {
                    it.reduceHunger(-Hunger.STARVING / 15)
                    BuffIndicator.refreshHero()
                }
            }
        }
        Sample.INSTANCE.play(Assets.SND_EVOKE)
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(VALUE, quantity)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        quantity = bundle.getInt(VALUE)
    }

    companion object {
        private const val VALUE = "value"
        private const val AC_CAST = "cast"
    }
}