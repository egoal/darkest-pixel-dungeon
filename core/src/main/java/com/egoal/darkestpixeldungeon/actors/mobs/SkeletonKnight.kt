package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.mobs.abilities.CounterDefend
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.HandOfTheElder
import com.egoal.darkestpixeldungeon.items.food.BrownAle
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.SkeletonKnightSprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.*

/**
 * Created by 93942 on 5/13/2018.
 */

class SkeletonKnight : Mob() {
    private var combocd = COMBO_COOLDOWN

    init {
        spriteClass = SkeletonKnightSprite::class.java

        abilities.add(CounterDefend())
    }

    override fun act(): Boolean {
        combocd -= 1
        return super.act()
    }

    override fun attack(enemy: Char): Boolean {
        if (combocd <= 0 && Random.Float() < COMBO) {
            combocd = COMBO_COOLDOWN

            spend(-cooldown() * .99f)
            sprite.showStatus(CharSprite.WARNING, Messages.get(this, "combo"))
        }

        return super.attack(enemy)
    }

    override fun createLoot(): Item? {
        if (!Dungeon.limitedDrops.handOfElder.dropped() && Random.Float() < 0.15f) {
            Dungeon.limitedDrops.handOfElder.drop()
            return HandOfTheElder().random()
        }
        return super.createLoot()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(COOLDOWN_COMBO, combocd)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        combocd = bundle.getInt(COOLDOWN_COMBO)
    }

    override fun immunizedBuffs(): HashSet<Class<*>> = IMMUS

    companion object {
        private const val COMBO = .175f

        private const val COMBO_COOLDOWN = 2

        private const val COOLDOWN_COMBO = "cooldown_combo"

        private val IMMUS = hashSetOf<Class<*>>(Bleeding::class.java)
    }
}
