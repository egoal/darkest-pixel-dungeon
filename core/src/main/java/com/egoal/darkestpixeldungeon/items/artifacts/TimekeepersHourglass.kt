package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.sqrt

class TimekeepersHourglass : Artifact() {
    init {
        image = ItemSpriteSheet.ARTIFACT_HOURGLASS

        levelCap = 5
        cooldown = 0

        chargeCap = chargeCapacity(level())
        charge = chargeCap
        partialCharge = 0f

        defaultAction = AC_ACTIVATE
    }

    var sandBags = 0
    var activated = false

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero) && charge > 0 && !cursed)
            actions.add(AC_ACTIVATE)

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_ACTIVATE) {
            if (!isEquipped(hero))
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
            else if (charge < 1) GLog.i(Messages.get(this, "no_charge"))
            else if (cursed) GLog.i(Messages.get(this, "cursed"))
            else if (cooldown > 0) GLog.i(Messages.get(this, "not-ready"))
            else if (activated) {
                hero.buff(TimeFreeze::class.java)?.detach()
                activated = false
                cooldown = 5 // 5 turn 
                GLog.i(Messages.get(this, "deactivate"))
            } else {
                GLog.i(Messages.get(this, "onfreeze"))
                Sample.INSTANCE.play(Assets.SND_TELEPORT)

                GameScene.flash(0x444444)
                activated = TimeFreeze().attachTo(hero)
            }
        }
    }

    override fun passiveBuff(): ArtifactBuff = Recharge()

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        if (super.doUnequip(hero, collect, single)) {
            hero.buff(TimeFreeze::class.java)?.detach()
            return true
        }
        return false
    }

    // 1, 3, 6, 10-> 1, 2, 3, 4: like MeleeWeapon::STRReq
    private fun chargeCapacity(lvl: Int): Int = (2 + (sqrt(8f * (2 * lvl) + 1f) - 1f) / 2f).toInt()

    override fun upgrade(): Item {
        // artifact transmutation
        while (level() + 1 > sandBags)
            sandBags++

        val self = super.upgrade()
        chargeCap = chargeCapacity(level())

        return self
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero)) {
            if (!cursed) {
                if (level() < levelCap)
                    desc += "\n\n" + Messages.get(this, "desc_hint")
            } else desc += "\n\n" + Messages.get(this, "desc_cursed")
        }

        return desc
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(SAND_BAGS, sandBags)
        bundle.put(ACTIVATED, activated)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        sandBags = bundle.getInt(SAND_BAGS)
        activated = bundle.getBoolean(ACTIVATED)
    }

    inner class Recharge : ArtifactBuff() {
        override fun act(): Boolean {
            val lock = target.buff(LockedFloor::class.java)
            if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
                partialCharge += 1f / (40f - (chargeCap - charge) * 2f)

                if (partialCharge >= 1f) {
                    partialCharge--
                    charge++
                    if (charge == chargeCap)
                        partialCharge = 0f
                }
            } else if (cursed && Random.Int(10) == 0)
                (target as Hero).spend(TICK) // steal time 

            if (cooldown > 0) --cooldown

            updateQuickslot()
            spend(Actor.TICK)

            return true
        }
    }

    // the world!
    inner class TimeFreeze : ArtifactBuff() {
        var partialTime = 0f
        private val pressed = arrayListOf<Int>() // cells delay to be pressed
        private val damages = arrayListOf<Damage>() // damages delay to give

        fun processTime(time: Float): Boolean {
            partialTime += time

            charge -= partialTime.toInt()
            partialTime -= partialTime.toInt()

            updateQuickslot()

            if (charge <= 0) {
                detach()
                return false
            }
            return true
        }

        fun addDelayedPress(cell: Int) {
            if (!pressed.contains(cell)) pressed.add(cell) // the order matters, 
        }

        fun addDelayedDamage(dmg: Damage) {
            damages.add(dmg)
        }

        private fun triggerPresses() {
            pressed.forEach { Dungeon.level.press(it, null) }
            pressed.clear()
        }

        private fun triggerDamages() {
            for (dmg in damages) {
                (dmg.to as Char?)?.let { ch: Char ->
                    if (ch.isAlive) {
                        ch.takeDamage(dmg)
                        if (Dungeon.visible[ch.pos]) {
                            // effects
                            if (dmg.from is Char) {
                                (dmg.from as Char?)?.let {
                                    ch.sprite.bloodBurstA(it.sprite.center(), dmg.value)
                                    Sample.INSTANCE.play(Assets.SND_HIT, 1f, 1f, Random.Float(.8f, 1.25f))
                                }
                            }
                        }
                    }
                }
            }
            damages.clear()
        }

        override fun attachTo(target: Char): Boolean {
            Dungeon.level?.let {
                it.mobs.forEach { m -> m.sprite.add(CharSprite.State.PARALYSED) }
            }
            // GameScene.freezeEmitters = true
            GameScene.setColorLayer(0x441885c4)

            return super.attachTo(target)
        }

        override fun detach() {
            Dungeon.level.mobs.forEach {
                it.sprite.remove(CharSprite.State.PARALYSED)
            }
            // GameScene.freezeEmitters = false
            GameScene.resetColorLayer()

            updateQuickslot()
            super.detach()
            activated = false

            triggerPresses()
            triggerDamages()
        }

        override fun storeInBundle(bundle: Bundle) {
            super.storeInBundle(bundle)
            bundle.put(TF_PRESSES, pressed.toIntArray())
            bundle.put(TF_PARTIAL, partialTime)
        }

        override fun restoreFromBundle(bundle: Bundle) {
            super.restoreFromBundle(bundle)
            for (i in bundle.getIntArray(TF_PRESSES))
                pressed.add(i)
            partialTime = bundle.getFloat(TF_PARTIAL)
        }
    }

    companion object {
        private const val AC_ACTIVATE = "activate"
        private const val SAND_BAGS = "sand-bags"
        private const val ACTIVATED = "activated"

        private const val TF_PRESSES = "tf-presses"
        private const val TF_PARTIAL = "tf-partial"

        class SandBag : Item() {
            init {
                image = ItemSpriteSheet.SANDBAG
            }

            override fun doPickUp(hero: Hero): Boolean {
                val tw = hero.belongings.getItem(TimekeepersHourglass::class.java)
                if (tw == null || tw.cursed) {
                    GLog.w(Messages.get(this, "no_hourglass"))
                    return false
                }

                tw.upgrade()
                Sample.INSTANCE.play(Assets.SND_DEWDROP)
                if (tw.level() == tw.levelCap)
                    GLog.p(Messages.get(this, "maxlevel"))
                else GLog.p(Messages.get(this, "levelup"))
                hero.spendAndNext(TIME_TO_PICK_UP)
                return true
            }

            override fun price(): Int = 10
        }

        fun IsTimeStopped(): Boolean = Dungeon.hero.buff(TimeFreeze::class.java) != null
    }

}