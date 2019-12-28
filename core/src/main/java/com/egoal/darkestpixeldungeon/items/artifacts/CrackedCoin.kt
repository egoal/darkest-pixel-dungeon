package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Lightning
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ElectronParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

class CrackedCoin : Artifact() {
    init {
        image = ItemSpriteSheet.CRACKED_COIN

        levelCap = 10
        exp = 0

        chargeCap = 100
        charge = chargeCap

        defaultAction = AC_SHELL
        usesTargeting = true
    }

    private var shieldActived = false

    override fun random(): Item {
        return super.random().apply { level(5) } // so, its level 5!
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n"
            desc += if (!cursed)
                M.L(this, "desc_hint") + "\n\n" + M.L(this, "desc_shield") + "\n" + M.L(this, "desc_shell")
            else M.L(this, "desc_cursed")
        }

        return desc
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero)) {
            actions.add(AC_SHIELD)
        }

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_SHIELD) {
            if (!shieldActived) {
                if (!isEquipped(hero))
                    GLog.i(M.L(Artifact::class.java, "need_to_equip"))
                else if (cursed) {
                    GLog.w(M.L(this, "cursed"))
                } else {
                    shieldActived = true
                    hero.spend(1f)
                    hero.busy()
                    Sample.INSTANCE.play(Assets.SND_MELD)
                    activeBuff = activeBuff()
                    activeBuff.attachTo(hero)
                    hero.sprite.operate(hero.pos)
                }
            } else {
                shieldActived = false
                activeBuff!!.detach()
                activeBuff = null
                hero.spend(1f)
                hero.sprite.operate(hero.pos)
            }
        } else if (action == AC_SHELL) {
            if (!isEquipped(hero))
                GLog.i(M.L(Artifact::class.java, "need_to_equip"))
            else if (cursed) {
                GLog.w(M.L(this, "cursed"))
            } else if (charge < chargeCap) {
                GLog.w(M.L(this, "not_charged"))
                QuickSlotButton.cancel()
            } else {
                GameScene.selectCell(dirSelector)
            }
        }
    }

    override fun activate(ch: Char) {
        super.activate(ch)
        if (shieldActived) {
            activeBuff = activeBuff()
            activeBuff.attachTo(ch)
        }
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        if (super.doUnequip(hero, collect, single)) {
            shieldActived = false
            return true
        } else
            return false
    }

    override fun passiveBuff(): ArtifactBuff = EatGold()

    override fun activeBuff(): ArtifactBuff = Shield()

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ACTIVED, shieldActived)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        shieldActived = bundle.getBoolean(ACTIVED)
    }

    inner class EatGold : ArtifactBuff() {
        override fun act(): Boolean {
            if (cursed) Dungeon.gold -= level()

            spend(10f)
            return true
        }
    }

    inner class Shield : Artifact.ArtifactBuff() {
        override fun icon(): Int = BuffIndicator.GOLD_SHIELD

        fun procTakenDamage(dmg: Damage) {
            if (dmg.type == Damage.Type.MENTAL || charge >= chargeCap) return

            val gold = min(Dungeon.gold, round(dmg.value / 2 / dpg()).toInt())
            Dungeon.gold -= gold

            dmg.value -= round(gold * dpg()).toInt()
            if (charge < chargeCap) {
                charge += round(max(1, gold) * 4f * 0.85f.pow(level())).toInt()
                if (charge >= chargeCap) {
                    charge = chargeCap
                    GLog.p(M.L(CrackedCoin::class.java, "charged"))
                }
                updateQuickslot()
            }
        }

        // damage per gold
        private fun dpg(): Float = max(0.35f, 0.2f * level() - 0.4f) // usually, it starts with level 5

        override fun toString(): String = M.L(this, "name")
        override fun desc(): String = M.L(this, "desc", dpg())
    }

    private fun shellCost(): Int = 10 + 5 * level()

    val arcs = ArrayList<Lightning.Arc>()

    private fun shellAt(hero: Hero, pos: Int) {
        val shot = Ballistica(hero.pos, pos, Ballistica.STOP_TERRAIN)
        if (pos == hero.pos || shot.collisionPos == hero.pos) {
            GLog.w(M.L(this, "self_target"))
            return
        }

        // cost
        Dungeon.gold -= min(Dungeon.gold, shellCost())
        charge = 0
        updateQuickslot()

        hero.sprite.zap(pos)
        arcs.clear()
        arcs.add(Lightning.Arc(shot.sourcePos, shot.collisionPos))
        hero.sprite.parent.add(Lightning(arcs) {
            onZap(hero, shot)

            exp += 1
            val requiredExp = level() * level() / 2 + 1
            if (exp >= requiredExp) {
                exp -= requiredExp
                upgrade()
                GLog.p(M.L(CrackedCoin::class.java, "levelup"))
            }

            hero.spendAndNext(1f)
        })
        for (c in shot.subPath(1, shot.dist))
            if (Dungeon.visible[c])
                CellEmitter.center(c).burst(ElectronParticle.FACTORY, Random.Int(4, 10))
        // MagicMissile.electronics(hero.sprite.parent, shot.sourcePos, shot.collisionPos, null)

        Sample.INSTANCE.play(Assets.SND_LIGHTNING)
    }

    private fun onZap(hero: Hero, beam: Ballistica) {
        var terrainAffected = false
        for (c in beam.subPath(1, beam.dist)) {
            Actor.findChar(c)?.let {
                val dmg = hero.giveDamage(it).type(Damage.Type.MAGICAL).addElement(Damage.Element.LIGHT)
                dmg.value = max(dmg.value, 8 + 5 * level()) // wand of lightning
                it.takeDamage(dmg)
                if (it.isAlive) {
                    Buff.prolong(it, Paralysis::class.java, Random.Float(1f, 1.5f) + level() / 5f)
                    it.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12)
                }
            }
            if (Level.flamable[c]) {
                Dungeon.level.destroy(c)
                GameScene.updateMap(c)
                terrainAffected = true
            }
        }

        if (terrainAffected) Dungeon.observe()
    }

    private val dirSelector = object : CellSelector.Listener {
        override fun prompt(): String = M.L(CrackedCoin::class.java, "prompt")

        override fun onSelect(cell: Int?) {
            if (cell != null) shellAt(Item.curUser, cell)
        }
    }

    companion object {
        private const val AC_SHIELD = "shield"
        private const val AC_SHELL = "shell"

        private const val ACTIVED = "actived"
    }
}