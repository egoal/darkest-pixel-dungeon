package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.unclassified.DewVial
import com.egoal.darkestpixeldungeon.items.unclassified.Gold
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.unclassified.PotionTestPaper
import com.egoal.darkestpixeldungeon.items.artifacts.AlchemistsToolkit
import com.egoal.darkestpixeldungeon.items.potions.*
import com.egoal.darkestpixeldungeon.items.weapon.curses.Fragile
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.AlchemistSprite
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.IconTitle
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.noosa.Game
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import java.util.ArrayList
import kotlin.math.min

class Alchemist : NPC.Unbreakable() {
    init {
        spriteClass = AlchemistSprite::class.java
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (!Quest.hasGiven_) {
            // give quest
            GameScene.show(object : WndQuest(this, Messages.get(this, "hello")) {
                override fun onBackPressed() {
                    super.onBackPressed()

                    Quest.hasGiven_ = true
                    Quest.hasCompleted_ = false

                    // drop dew vial
                    val dv = DewVial()
                    if (dv.doPickUp(Dungeon.hero)) {
                        GLog.i(Messages.get(Dungeon.hero, "you_now_have", dv.name()))
                    } else
                        Dungeon.level.drop(dv, Dungeon.hero.pos).sprite.drop()

                    Dungeon.limitedDrops.dewVial.drop()
                }
            })

            // todo: add journal

        } else {
            if (!Quest.hasCompleted_) {
                WndDialogue.Show(this, M.L(this, "back"), M.L(this, "yes"), M.L(this, "no")) {
                    if (it == 0) {
                        val dv = Dungeon.hero.belongings.getItem(DewVial::class.java)
                        if (dv == null) tell(M.L(Alchemist::class.java, "bottle_miss"))
                        else {
                            val volume = dv.Volume
                            if (volume == 0) tell(M.L(Alchemist::class.java, "empty"))
                            else {
                                val responds = when {
                                    volume < 5 -> M.L(Alchemist::class.java, "little")
                                    dv.full -> M.L(Alchemist::class.java, "full")
                                    else -> M.L(Alchemist::class.java, "enough")
                                }
                                GameScene.show(object : WndQuest(this, responds) {
                                    override fun onBackPressed() {
                                        super.onBackPressed()
                                        drink()
                                    }
                                })
                            }
                        }

                    } else {
                        tell(M.L(Alchemist::class.java, "wait"))
                    }
                }

            } else {
                tell(Messages.get(this, "farewell"))
            }
        }


        return false
    }

    // drink and give reward
    fun drink() {
        val dv = Dungeon.hero.belongings.getItem(DewVial::class.java)!!
        val vol = dv.Volume
        Flare(6, 32f).show(sprite, 2f)
        sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)
        GLog.i(M.L(this, "drink"))

        Dungeon.hero.spend(1f)
        Dungeon.hero.busy()
        Sample.INSTANCE.play(Assets.SND_DRINK)
        // empty dew vial
        dv.empty()

        // give reward
        Gold(Random.Int(5, 15) * vol + 20).doPickUp(Dungeon.hero)

        if (vol >= 5) {
            // give test papers
            val cnt = min(vol / 5, 2)
            for (i in 1..cnt) {
                val ptp = PotionTestPaper()
                if (!ptp.doPickUp(Dungeon.hero))
                    Dungeon.level.drop(ptp, Dungeon.hero.pos).sprite.drop()
            }
            GLog.i(Messages.get(this, "reward_given"))
        }
        Quest.hasCompleted_ = true
    }

    override fun reset(): Boolean = true

    object Quest {
        var hasGiven_ = false
        var hasCompleted_ = false

        // serialization
        private const val NODE = "alchemist"
        private const val GIVEN = "given"
        private const val COMPLETED = "completed"

        fun reset() {
            hasCompleted_ = false
            hasGiven_ = false
        }

        fun storeInBundle(bundle: Bundle) {
            val node = Bundle()
            node.put(GIVEN, hasGiven_)
            node.put(COMPLETED, hasCompleted_)

            bundle.put(NODE, node)
        }

        fun restoreFromBundle(bundle: Bundle) {
            val node = bundle.getBundle(NODE)
            if (!node.isNull) {
                hasGiven_ = node.getBoolean(GIVEN)
                hasCompleted_ = node.getBoolean(COMPLETED)
            } else
                reset()
        }

    }
}
