package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.weapon.Enchantment
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.abs

class PairSwords(var left: Sword = Sword(), var right: Sword = Sword()) : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.PAIR_SWORDS

        tier = 5

        identify()
    }

    // properties
    override fun min(lvl: Int): Int = (left.min() + right.min()) / 2 + left.level() + right.level()
    // ^ similar to standard 

    override fun max(lvl: Int): Int {
        val value = Math.max(left.max(), right.max())
        // correct by diff
        return when (abs(left.level() - right.level())) {
            0 -> value * 7 / 4 // more powerful
            1 -> value * 6 / 4 // similar to a normal tier-5 weapon
            2 -> value * 5 / 4
            else -> value * 4 / 5
        }
    }

    override fun STRReq(lvl: Int): Int = Math.max(left.STRReq(), right.STRReq()) + 2

    override fun accuracyFactor(hero: Hero, target: Char): Float {
        val f = super.accuracyFactor(hero, target)
        val diff = abs(left.level() - right.level())

        return Math.pow(1.1, (2 - diff).toDouble()).toFloat() * f
    }

    // enchanting, prefer to enchant the one without enchantment
    // or you could just split them, enchant, then dual again.
    override fun inscribe(): Weapon = swordToInscribe().inscribe()

    override fun inscribe(insc: Inscription?): Weapon = swordToInscribe().inscribe(insc)

    override fun isInscribed(type: Class<out Inscription>): Boolean = left.isInscribed(type) || right.isInscribed(type)

    override fun enchant(type: Class<out Enchantment>, duration: Float): Weapon {
        swordToEnchant().enchant(type, duration)
        return this
    }

    override fun hasEnchant(type: Class<out Enchantment>): Boolean = left.hasEnchant(type) || right.hasEnchant(type)

    override fun glowing(): ItemSprite.Glowing? = left.glowing() ?: right.glowing()

    private fun swordToInscribe(): Weapon = if (left.inscription == null || right.inscription != null) left else right

    private fun swordToEnchant(): Weapon = if (left.enchantment == null || right.enchantment != null) left else right

    override fun proc(dmg: Damage): Damage = left.proc(right.proc(dmg))

    // split-> upgrade -> merge
    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    override fun desc(): String {
        val swordName = { sword: Sword ->
            var name = "${sword.name()}+${sword.level()}"
            if (sword.enchantment != null) name += " (${sword.enchantment!!.name()})"
            name
        }

        var desc = super.desc()
        desc += "\n\n" + swordName(left) + "\n" + swordName(right)

        return desc
    }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        if (!cursed)
            actions.add(AC_SPLIT)

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_SPLIT) {
            if (!left.doPickUp(hero))
                Dungeon.level.drop(left, hero.pos).sprite.drop()
            if (!right.doPickUp(hero))
                Dungeon.level.drop(right, hero.pos).sprite.drop()

            if (isEquipped(hero))
                doUnequip(hero, false)
            detach(hero.belongings.backpack)

            GLog.i(Messages.get(this, "splited"))
        }
    }

    override fun price(): Int = left.price() + right.price()

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(LEFT_SWORDS, left)
        bundle.put(RIGHT_SWORDS, right)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)

        left = bundle.get(LEFT_SWORDS) as Sword
        right = bundle.get(RIGHT_SWORDS) as Sword
    }

    companion object {
        private const val AC_SPLIT = "SPLIT"
        private const val LEFT_SWORDS = "left_one"
        private const val RIGHT_SWORDS = "right_one"
    }
}