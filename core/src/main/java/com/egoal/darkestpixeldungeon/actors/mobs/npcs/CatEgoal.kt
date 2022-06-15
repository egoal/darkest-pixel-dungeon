package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.food.Food
import com.egoal.darkestpixeldungeon.items.food.OrchidRoot
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey
import com.egoal.darkestpixeldungeon.items.unclassified.GoldenClaw
import com.egoal.darkestpixeldungeon.items.weapon.melee.ShortSticks
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CatLixSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import java.util.ArrayList

class CatEgoal : NPC.Unbreakable() {
    init {
        spriteClass = CatLixSprite::class.java

        state = Wandering()
    }

    var answered = false
    var praised = false

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (answered) {
            val str = if (praised) M.L(this, "happy")
            else M.L(this, "normal", Dungeon.hero.className())

            WndDialogue.Show(this, str, M.L(this, "you-moved")) {
                tell(M.L(CatEgoal::class.java, "didi"))
            }
        } else
            WndDialogue.Show(this, M.L(this, "greetings"), M.L(this, "agree"),
                    M.L(this, "disagree")) {
                onAnsweredHero(it)
            }

        return false
    }

    fun onAnsweredHero(index: Int) {
        answered = true
        praised = index == 0

        val g = Gift().apply {
            identify()
            setItems(
                    ShortSticks().identify(),
                    // OrchidRoot(),
                    // if (praised) Generator.SCROLL.generate() else Generator.POTION.generate(),
                    GoldenClaw(),
                    SkeletonKey(Dungeon.depth))
        }
        if (g.doPickUp(Dungeon.hero))
            GLog.i(M.L(Dungeon.hero, "you_now_have", g.name()))
        else
            Dungeon.level.drop(g, Dungeon.hero.pos).sprite.drop()

        val text = if (praised) M.L(this, "ans_happy", Dungeon.hero.className())
        else M.L(this, "ans_normal")
        yell(text)

//         GameScene.show(WndGainNewPerk.CreateWithRandomPositives(M.L(this, "from_egoal"), 3))
    }

    private val ANSWERED = "answered"
    private val PRAISED = "praised"

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)

        bundle.put(ANSWERED, answered)
        bundle.put(PRAISED, praised)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        answered = bundle.getBoolean(ANSWERED)
        praised = bundle.getBoolean(PRAISED)
    }

    override fun speed(): Float = 5f

    companion object {
        class Gift : Item() {
            private val AC_OPEN = "open"
            private val TIME_TO_OPEN = 1f

            init {
                stackable = false
                defaultAction = AC_OPEN

                image = ItemSpriteSheet.DPD_CAT_GIFT
            }

            lateinit var items: List<Item>

            fun setItems(vararg items: Item) {
                this.items = items.toList()
            }

            override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_OPEN) }

            override fun execute(hero: Hero, action: String) {
                super.execute(hero, action)
                if (action == AC_OPEN)
                    open(hero)
            }

            private fun open(hero: Hero) {
                detach(hero.belongings.backpack)
                hero.spend(TIME_TO_OPEN)
                hero.busy()

                GLog.i(M.L(this, "opened"))

                // give
                for (item in items)
                    if (item.doPickUp(hero))
                        GLog.w(M.L(Dungeon.hero, "you_now_have", item.name()))
                    else
                        Dungeon.level.drop(item, hero.pos).sprite.drop()

                Sample.INSTANCE.play(Assets.SND_OPEN)
                hero.sprite.operate(hero.pos)
            }

            private val ALL_ITEM = "all_items"
            override fun storeInBundle(bundle: Bundle) {
                super.storeInBundle(bundle)

                bundle.put(ALL_ITEM, items)
            }

            override fun restoreFromBundle(bundle: Bundle) {
                super.restoreFromBundle(bundle)
                items = bundle.getCollection(ALL_ITEM) as List<Item>
            }
        }
    }

    override fun reset() = true

    override fun move(step: Int) {
        if (!Dungeon.visible[pos] && !Dungeon.visible[step]) super.move(step)
    }

    inner class Wandering : AiState {
        override fun act(enemyInFOV: Boolean, justAlerted: Boolean): Boolean {
            enemySeen = false

            if (answered)
                if (target != -1 && getCloser(target)) {
                    sprite.place(pos)
                } else {
                    target = Dungeon.level.randomDestination()
                }
            spend(2f)
            return true
        }

        override fun status(): String = M.L(this, "status")
    }
}