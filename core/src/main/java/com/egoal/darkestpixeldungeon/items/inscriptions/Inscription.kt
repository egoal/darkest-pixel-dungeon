package com.egoal.darkestpixeldungeon.items.inscriptions

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.inscriptions.good.AntiMagic
import com.egoal.darkestpixeldungeon.items.inscriptions.good.Healthy
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.Random

abstract class Inscription : Bundlable {

    // affection
    open fun procTakenDamage(equipment: EquipableItem, dmg: Damage) {}
    open fun procGivenDamage(dmg: Damage) {}
    open fun mhpFix(mhp: Int): Int = mhp

    abstract fun glowing(): ItemSprite.Glowing

    fun name(equipmentName: String): String = Messages.get(this, "name", equipmentName)

    fun name(): String = name(
            if (curse()) Messages.get(Item::class.java, "curse")
            else Messages.get(this, "inscription"))

    fun desc(): String = Messages.get(this, "desc")

    fun curse() = false

    fun checkIfKilledOwner(owner: Char): Boolean {
        return if (!owner.isAlive && owner is Hero) {
            Dungeon.fail(javaClass)
            GLog.n(Messages.get(this, "killed", name()))

            // Badges.validateDeathFromGlyph()
            true
        } else
            false
    }

    // todo: random

    override fun storeInBundle(bundle: Bundle) {}
    override fun restoreFromBundle(bundle: Bundle) {}

    companion object {
        fun RandomInscription(): Inscription = Random.chances(Inscriptions).newInstance()

        fun RandomCurse(): Inscription = Random.chances(Curses).newInstance()

        private val Inscriptions: HashMap<Class<out Inscription>, Float> = hashMapOf(
                AntiMagic::class.java to 1f, 
                Healthy::class.java to 1f
        )

        private val Curses: HashMap<Class<out Inscription>, Float> = hashMapOf()
    }
}

