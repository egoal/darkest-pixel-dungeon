package com.egoal.darkestpixeldungeon.actors

import com.egoal.darkestpixeldungeon.messages.M
import kotlin.math.max

/**
 * Created by 93942 on 4/30/2018.
 */
class Damage(var value: Int, var from: Any, var to: Any) {
    constructor(from: Any, to: Any, type: Type = Type.NORMAL) : this(0, from, to) {
        this.type = type
    }

    enum class Type {
        NORMAL, MAGICAL, MENTAL;

        override fun toString(): String = M.L(this, super.toString().lowercase())
    }

    enum class Element {
        Fire, Poison, Ice, Light, Shadow, Holy;

        val textName: String
            get() = M.L(Damage::class.java, "ele$ordinal")
        val color: Int
            get() = when (this) {
                Fire -> 0xee7722
                Poison -> 0x8844ff
                Ice -> 0x88ccff
                Light -> 0xffffff
                Shadow -> 0x2a1a33
                Holy -> 0xffff00
            }
    }

    object Feature {
        const val NONE = 0x0000
        const val CRITICAL = 0x0001
        const val ACCURATE = 0x0002
        const val PURE = 0x0004
        const val DEATH = 0x0008
        const val RANGED = 0x0010
    }

    // attributes
    var type = Type.NORMAL

    var add_value = 0 // additional elemental damage
    var element = Element.Fire
        private set

    var feature = Feature.NONE

    fun type(t: Type): Damage {
        type = t
        return this
    }

    fun setAdditionalDamage(e: Element, value: Int): Damage {
        element = e
        add_value = max(add_value, value)
        return this
    }

    fun convertToElement(e: Element): Damage {
        setAdditionalDamage(e, value)
        value = 0
        return this
    }

    fun addFeature(f: Int): Damage {
        feature = feature or f
        return this
    }

    fun isFeatured(f: Int): Boolean {
        return feature and f != 0
    }
}