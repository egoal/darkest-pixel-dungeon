package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Lucky
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random

class LuckyCoin : Item() {
    init {
        image = ItemSpriteSheet.LUCKY_COIN
        stackable = true

        defaultAction = AC_USE
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    override fun price(): Int = 5 * quantity

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_USE) {
            Buff.prolong(hero, Lucky::class.java, 150f)
            GameScene.effect(Flare(7, 32f).color(0xffc203, true).show(
                    hero.sprite.parent, DungeonTilemap.tileCenterToWorld(hero.pos), 2f))
            hero.spendAndNext(Actor.TICK)
        }
    }

    override fun random(): Item = this.apply { quantity = Random.IntRange(1, 2) }

    companion object {
        private const val AC_USE = "use"
    }
}