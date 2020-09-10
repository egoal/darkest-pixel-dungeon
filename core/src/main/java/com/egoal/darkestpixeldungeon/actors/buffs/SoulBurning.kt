package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.rings.RingOfResistance
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random

import javax.microedition.khronos.opengles.GL

/**
 * Created by 93942 on 5/5/2018.
 */

class SoulBurning : Buff(), Hero.Doom {
    private var left_: Float = 0.toFloat()

    init {
        type = Buff.buffType.NEGATIVE
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEFT, left_)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        left_ = bundle.getFloat(LEFT)
    }

    override fun act(): Boolean {
        if (target.isAlive) {
            val maxDmg = Dungeon.depth
            val dmgHP = Random.Int(2, maxDmg)

            if (target is Hero) {
                //todo: affect hero
            } else {
                target.takeDamage(Damage(dmgHP, this, target).type(Damage.Type.MAGICAL).addElement(Damage.Element.SHADOW))
            }
        } else {
            detach()
        }

        spend(Actor.TICK)
        left_ -= Actor.TICK
        if (left_ <= 0)
            detach()

        return true
    }

    fun reignite(ch: Char) {
        left_ = duration(ch)
    }

    override fun icon(): Int {
        return BuffIndicator.SOUL_FIRE
    }

    override fun fx(on: Boolean) {
        if (on)
            target.sprite.add(CharSprite.State.SOUL_BURNING)
        else
            target.sprite.remove(CharSprite.State.SOUL_BURNING)
    }

    override fun heroMessage(): String? {
        return Messages.get(this, "heromsg")
    }

    override fun toString(): String {
        return Messages.get(this, "name")
    }

    override fun desc(): String {
        return Messages.get(this, "desc", dispTurns(left_))
    }

    override fun onDeath() {
        //todo: add badges
        Dungeon.fail(javaClass)
        GLog.n(Messages.get(this, "ondeath"))
    }

    companion object {

        private val DURATION = 8f

        private val LEFT = "left"

        fun duration(ch: Char): Float {
            val r = ch.buff(RingOfResistance.Resistance::class.java)
            return if (r != null) r.durationFactor() * DURATION else DURATION
        }
    }
}
