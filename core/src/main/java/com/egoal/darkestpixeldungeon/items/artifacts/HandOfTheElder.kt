package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.rings.*
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import com.watabou.utils.Random
import java.util.ArrayList

class HandOfTheElder : Artifact() {
    init {
        image = ItemSpriteSheet.BONE_HAND
        levelCap = 10

        charge = 2
        chargeCap = 2

        defaultAction = AC_POINT
        usesTargeting = true
    }

    private val rings = mutableListOf<Class<out Ring>>()

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (isEquipped(hero)) {
            if (level() < levelCap && rings.size < MAX_RINGS_TO_WEAR) actions.add(AC_WEAR)
            if (charge > 0) actions.add(AC_POINT)
        }
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_POINT) {
            Item.curUser = hero
            if (!isEquipped(hero)) {
                GLog.i(Messages.get(Artifact::class.java, "need_to_equip"))
                QuickSlotButton.cancel()
            } else if (charge < 1) {
                GLog.i(Messages.get(this, "no_charge"))
                QuickSlotButton.cancel()
            } else
                GameScene.selectCell(charSelector)
        } else if (action == AC_WEAR) {
            if (cursed)
                GLog.w(Messages.get(this, "cannot_wear"))
            else if (level() < levelCap)
                GameScene.selectItem(ringSelector, WndBag.Mode.RING, Messages.get(this, "wear_prompt"))
        }
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero)) {
            desc += "\n\n" + Messages.get(this, "desc_equipped")
            if (rings.size > 0) {
                desc += "\n\n" + Messages.get(this, "desc_rings")
                for (r in rings)
                    desc += "\n" + Messages.get(r, "name")
            }
        }
        return desc
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(RINGS, rings.toTypedArray())
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        rings.clear()
        rings.addAll(bundle.getClassArray(RINGS).map { it as Class<out Ring> })
    }

    // buff
    override fun passiveBuff(): Artifact.ArtifactBuff? {
        return Recharge()
    }

    inner class Recharge : Artifact.ArtifactBuff() {

        override fun act(): Boolean {
            if (charge < chargeCap) {
                partialCharge += (0.025 * Math.pow(1.05, level().toDouble())).toFloat()

                if (partialCharge >= 1f) {
                    charge++
                    partialCharge -= 1f
                    if (charge == chargeCap)
                        partialCharge = 0f

                    updateQuickslot()
                }
            } else
                partialCharge = 0f

            spend(Actor.TICK)
            return true
        }
    }

    // wear
    private fun wearRing(ring: Ring) {
        upgrade(GameMath.clamp(ring.level() + 1, 1, levelCap - level()))

        if (ring.cursed) {
            cursed = true
            Sample.INSTANCE.play(Assets.SND_CURSED)
            GLog.p(Messages.get(this, "cursed_levelup", ring.name()))
        } else {
            Sample.INSTANCE.play(Assets.SND_ASTROLABE)
            GLog.p(Messages.get(this, "levelup", ring.name()))
        }

        rings.add(ring.javaClass)
    }

    private val ringSelector = WndBag.Listener { item: Item? ->
        if (item != null && item is Ring) {
            if (!item.isIdentified) {
                GLog.w(Messages.get(HandOfTheElder::class.java, "unknown_ring"))
            } else if (rings.contains(item::class.java))
                GLog.w(Messages.get(HandOfTheElder::class.java, "duplicate_ring"))
            else {
                // okay to wear
                wearRing(item)

                with(Dungeon.hero) {
                    sprite.operate(pos)
                    busy()
                    spend(2f)
                    sprite.emitter().burst(ElmoParticle.FACTORY, 12)

                    if (item.isEquipped(this))
                        item.doUnequip(this, false)
                    item.detachAll(belongings.backpack)
                }
            }
        }
    }

    // point
    private fun pointAt(c: Char) {
        // cost
        --charge

        Item.curUser.sprite.zap(c.pos)
        Item.curUser.spend(1f)
        Item.curUser.busy()

        MagicMissile.slowness(Item.curUser.sprite.parent, Item.curUser.pos, c.pos) {
            // affect 
            var bonus = if (cursed) 1.25f else 1f
            if (c.properties().contains(Char.Property.MINIBOSS) || c.properties().contains(Char.Property.BOSS))
                bonus *= 0.5f
            val duration = (level() / 2 + 2) * bonus

            // root& damage
            c.takeDamage(Damage(Random.Int(c.HT / 10, c.HT / 5), Item.curUser, c).type(Damage.Type.MAGICAL).addElement(Damage.Element.SHADOW))

            // buffs
            if (c.isAlive) {
                Buff.prolong(c, Roots::class.java, duration)

                for (ringcls in rings) {
                    val buffcls = RingsToBuffs[ringcls] ?: Cripple::class.java

                    when (buffcls) {
                        Vulnerable::class.java -> Buff.prolong(c, Vulnerable::class.java, duration).ratio = 1.5f
                        Charm.Attacher::class.java -> Charm.Attacher(Item.curUser.id(), duration.toInt()).attachTo(c)
                        else -> Buff.prolong(c, buffcls, duration)
                    }
                }
            }

            Item.curUser.next()
        }

        Sample.INSTANCE.play(Assets.SND_ASTROLABE, 1f, 1f, .8f)
        updateQuickslot()
    }

    private val charSelector = object : CellSelector.Listener {
        override fun onSelect(cell: Int?) {
            if (cell != null) {
                val c = Actor.findChar(cell)
                if (c != null && c != Item.curUser)
                    pointAt(c)
            }
        }

        override fun prompt(): String = Messages.get(HandOfTheElder::class.java, "point_prompt")
    }

    companion object {
        private const val AC_WEAR = "wear"
        private const val AC_POINT = "point"
        private const val RINGS = "rings"

        private const val MAX_RINGS_TO_WEAR = 5

        private val RingsToBuffs = mapOf<Class<out Ring>, Class<out FlavourBuff>>(
                RingOfAccuracy::class.java to Shock::class.java,
                RingOfCritical::class.java to Weakness::class.java,
                RingOfElements::class.java to Chill::class.java,
                RingOfEvasion::class.java to Shock::class.java,
                RingOfForce::class.java to Vertigo::class.java,
                RingOfFuror::class.java to Slow::class.java,
                RingOfHaste::class.java to Slow::class.java,
                // RingOfMight.class, 
                RingOfSharpshooting::class.java to Blindness::class.java,
                RingOfTenacity::class.java to Vertigo::class.java,
                RingOfWealth::class.java to Charm.Attacher::class.java
        )
    }
}