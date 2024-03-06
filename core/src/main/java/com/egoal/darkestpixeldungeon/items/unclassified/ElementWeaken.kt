package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.ElementBroken
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample

open class ElementWeaken(icon: Int, private val element: Damage.Element, private val ratio: Float) : Item() {
    init {
        image = icon
        bones = false
        stackable = true

        defaultAction = AC_THROW
        usesTargeting = true
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    override fun onThrow(cell: Int) {
        Actor.findChar(cell)?.let {
            Buff.prolong(it, ElementBroken::class.java, 10f).add(element, ratio)
        }
        CellEmitter.get(cell).burst(Speck.factory(Speck.STEAM, color()), 5)
        Sample.INSTANCE.play(Assets.SND_PUFF)
    }

    override fun price(): Int = 5 * quantity

    protected open fun color() = 0x000000
}

class FireButterfly : ElementWeaken(ItemSpriteSheet.FIRE_BUTTERFLY, Damage.Element.Fire, 1.25f) {
    override fun color(): Int = 0xff7f00
}

class PoisonPowder : ElementWeaken(ItemSpriteSheet.POISON_POWDER, Damage.Element.Poison, 1.25f) {
    override fun color(): Int = 0x1d7a3b
}