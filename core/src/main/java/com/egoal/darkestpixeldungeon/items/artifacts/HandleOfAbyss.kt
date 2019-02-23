package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.AbyssHero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.ArrayList

class HandleOfAbyss : Artifact() {
    init {
        image = ItemSpriteSheet.HANDLE_OF_ABYSS

        levelCap = 10

        charge = 100
        chargeCap = 100

        defaultAction = AC_SUMMON
        defeated = false
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)

        if (isEquipped(hero) && charge == chargeCap)
            actions.add(AC_SUMMON)

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_SUMMON) {
            if (!isEquipped(hero))
                GLog.w(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (charge != chargeCap) GLog.w(Messages.get(this, "no-charge"))
            else if (AbyssHero.Instance() != null)
                GLog.w(Messages.get(this, "already-summoned"))
            else {
                // check pos 
                val avpos = PathFinder.NEIGHBOURS8.map { hero.pos + it }.filter {
                    Actor.findChar(it) == null && (Level.passable[it] || Level.avoid[it])
                }

                if (avpos.isEmpty()) {
                    GLog.w(Messages.get(this, "cannot-summon-here"))
                } else {
                    val ah = AbyssHero(level(), defeated && !cursed).apply {
                        pos = Random.element(avpos)
                    }

                    GameScene.add(ah, 1f)
                    CellEmitter.get(ah.pos).burst(ShadowParticle.CURSE, 5)
                    ah.onSpawned()
                    Sample.INSTANCE.play(Assets.SND_BURNING)

                    hero.spend(1f)
                    hero.busy()
                    hero.sprite.operate(hero.pos)

                    charge = 0
                    updateQuickslot()
                }
            }
        }
    }

    override fun desc(): String = if (isIdentified && defeated) {
        var desc = Messages.get(this, "desc-real") + "\n\n" + Messages.get(this, "desc-tip")
        if (cursed) desc += Messages.get(this, "desc-cursed")
        desc
    } else
        Messages.get(this, "desc")

    override fun passiveBuff(): ArtifactBuff = Recharge()

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("defeated", defeated)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        defeated = bundle.getBoolean("defeated")
    }

    inner class Recharge : ArtifactBuff() {
        override fun act(): Boolean {
            val lock = target.buff(LockedFloor::class.java)
            if (AbyssHero.Instance() == null && (lock == null || lock.regenOn()) && charge < chargeCap && !cursed) {
                partialCharge += 100f / (200f + level() * 10)
                if (partialCharge > 1f) {
                    charge++
                    partialCharge--
                    if (charge == chargeCap) {
                        partialCharge = 0f
                        GLog.p(Messages.get(HandleOfAbyss::class.java, "charged"))
                    }
                }
            } else if (cursed && Random.Float() < 0.01) {
                val avpos = PathFinder.NEIGHBOURS8.map { Dungeon.hero.pos + it }.filter {
                    Actor.findChar(it) == null && (Level.passable[it] || Level.avoid[it])
                }
                if (avpos.isNotEmpty()) {
                    val ah = AbyssHero(level(), false).apply {
                        pos = Random.element(avpos)
                    }

                    GameScene.add(ah, 1f)
                    CellEmitter.get(ah.pos).burst(ShadowParticle.CURSE, 5)
                    ah.onSpawned()
                    Sample.INSTANCE.play(Assets.SND_BURNING)
                }
            }

            updateQuickslot()
            spend(Actor.TICK)

            return true
        }

        fun gainExp(porton: Float) {
            if (defeated && level() < levelCap) {
                exp += Math.round(porton * 100)

                if (exp >= 100) {
                    exp -= 100
                    GLog.p(Messages.get(HandleOfAbyss::class.java, "levelup"))
                    upgrade()
                }
            }
        }
    }

    companion object {
        private const val AC_SUMMON = "summon"

        private var defeated = false
        fun SetDefeated() {
            if (!defeated) {
                defeated = true
                GLog.w(Messages.get(HandleOfAbyss::class.java, "now-you-know"))
            }
        }
    }
}