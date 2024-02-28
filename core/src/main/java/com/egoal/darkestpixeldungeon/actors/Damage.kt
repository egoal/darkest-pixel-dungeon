package com.egoal.darkestpixeldungeon.actors

import com.egoal.darkestpixeldungeon.messages.M
import kotlin.math.max

/**
 * Created by 93942 on 4/30/2018.
 */
class Damage(var value: Int, var from: Any, var to: Any) {
    enum class Type {
        NORMAL, MAGICAL, MENTAL;

        override fun toString(): String = M.L(this, super.toString().lowercase())
    }

    object Element {
        const val NONE = 0x0000
        const val FIRE = 0x0001
        const val POISON = 0x0002
        const val ICE = 0x0004
        const val LIGHT = 0x0008 // this should be lightning...
        const val SHADOW = 0x0010
        const val HOLY = 0x0020 // this should use name: light
        const val ELEMENT_COUNT = 6
        fun all(): Int {
            var a = 0
            for (i in 0 until ELEMENT_COUNT) a = a or (0x01 shl i)
            return a
        }

        val names = (0 until ELEMENT_COUNT).map { M.L(Damage::class.java, "ele$it") }.toTypedArray()
    }

    object Feature {
        const val NONE = 0x0000
        const val CRITICAL = 0x0001
        const val ACCURATE = 0x0002
        const val PURE = 0x0004
        const val DEATH = 0x0008
        const val RANGED = 0x0010
        const val FEATURE_COUNT = 5
        fun all(): Int {
            var a = 0
            for (i in 0 until FEATURE_COUNT) a = a or (0x01 shl i)
            return a
        }
    }

    // attributes
    var add_value = 0 // additional elemental damage
    var type = Type.NORMAL

    var element = Element.NONE
        private set

    var feature = Feature.NONE

    fun type(t: Type): Damage {
        type = t
        return this
    }

    fun setAdditionalDamage(e: Int, value: Int): Damage {
        element = e
        add_value = max(add_value, value)
        return this
    }

    fun convertToElement(e: Int): Damage {
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

    fun hasElement(e: Int): Boolean {
        return element and e != 0
    }
}