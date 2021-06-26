package com.egoal.darkestpixeldungeon.items.weapon

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.curses.*
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.*
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Suppress
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.Random

abstract class Inscription(val icon: Int = -1, val curse: Boolean = false) : Bundlable {
    fun name(): String = if (!curse) name(M.L(Inscription::class.java, "inscription"))
    else name(M.L(Item::class.java, "curse"))

    fun name(weaponName: String): String = M.L(this, "name", weaponName)

    fun desc(): String = M.L(this, "desc")

    abstract fun proc(weapon: Weapon, damage: Damage): Damage

    override fun storeInBundle(bundle: Bundle) {}
    override fun restoreFromBundle(bundle: Bundle) {}

    abstract class Curse(icon: Int) : Inscription(ROW_OFFSET + icon, true)

    companion object {
        private const val ROW_OFFSET = 18 // image.

        // list
        private val inscriptions = arrayOf(
                arrayOf(Dazzling::class.java, Eldritch::class.java, Lucky::class.java, Projecting::class.java,
                        Storming::class.java, Heavy::class.java, Suppress::class.java, Vorpal::class.java),
                arrayOf(Grim::class.java, Stunning::class.java, Vampiric::class.java),
                arrayOf(Holy::class.java)
        )
        private val chances = floatArrayOf(70f, 30f, 0f)

        private val negatives = arrayOf<Class<out Inscription>>(
                Annoying::class.java, Arrogant::class.java, Bloodthirsty::class.java,
                Displacing::class.java, Exhausting::class.java, Fragile::class.java,
                Provocation::class.java, Sacrificial::class.java, Wayward::class.java
        )


        fun randomPositive(): Inscription = inscriptions[Random.chances(chances)].random().newInstance()

        fun randomNegative(): Inscription = Random.oneOf(*negatives).newInstance()
    }
}