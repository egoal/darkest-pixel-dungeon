package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.abs

const val AC_SPLIT = "SPLIT"
const val LEFT_SWORDS = "left_one"
const val RIGHT_SWORDS = "right_one"

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
            0 -> value * 2 // more powerful
            1 -> value * 7 / 4 // similar to a usually tier-5 weapon
            2 -> value * 5 / 4
            else -> value * 4 / 5
        }
    }

    override fun STRReq(lvl: Int): Int = Math.max(left.STRReq(), right.STRReq()) + 2

    override fun accuracyFactor(hero: Hero?): Float {
        val f = super.accuracyFactor(hero)
        val diff = abs(left.level() - right.level())

        return Math.pow(1.1, (2 - diff).toDouble()).toFloat() * f
    }

    // enchanting, prefer to enchant the one without enchantment
    // or you could just split them, enchant, then dual again.
    override fun enchant(): Weapon = selectEnchantSword().enchant()

    override fun enchant(ench: Enchantment): Weapon = selectEnchantSword().enchant(ench)

    private fun selectEnchantSword(): Weapon = when {
        left.enchantment == null -> left
        right.enchantment == null -> right
        else -> if (Random.Int(2) == 0) left else right
    }

    override fun proc(dmg: Damage): Damage = left.proc(right.proc(dmg))

    // split-> upgrade -> merge
    override fun isUpgradable(): Boolean = false

    override fun desc(): String {
        var desc = super.desc()
        desc += "\n\n${left.name()}+${left.level()} \n ${right.name()}+${right.level()}"

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
                Dungeon.level.drop(left, Dungeon.hero.pos).sprite.drop()
            if (!right.doPickUp(hero))
                Dungeon.level.drop(right, Dungeon.hero.pos).sprite.drop()

            if (isEquipped(hero))
                doUnequip(hero, false)
            detach(hero.belongings.backpack)

            GLog.i(Messages.get(this, "splited"));
        }
    }

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
}