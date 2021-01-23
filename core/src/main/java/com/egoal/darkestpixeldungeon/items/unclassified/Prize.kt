package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.food.Humanity
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat
import com.egoal.darkestpixeldungeon.items.food.OrchidRoot
import com.egoal.darkestpixeldungeon.items.food.OverpricedRation
import com.egoal.darkestpixeldungeon.items.potions.ReagentOfHealing
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.utils.Random

// born prizes, see HeroCreateScene

enum class Prize {
    REAGENT_OF_HEALING {
        private val item = ReagentOfHealing()
        override fun title(): String = item.name()
        override fun collect(hero: Hero) {
            item.collect()
        }
    },

    ORCHID_ROOT {
        private val item = OrchidRoot()
        override fun title(): String = item.name()
        override fun collect(hero: Hero) {
            item.collect()
        }
    },

    LITTLE_FOOD {
        override fun title(): String = M.L(Prize::class.java, "little_food")
        override fun collect(hero: Hero) {
            val food = if (Random.Int(3) != 0) OverpricedRation() else MysteryMeat().quantity(2)
            food.collect()
        }
    },

    SOME_SEED {
        override fun title(): String = M.L(Prize::class.java, "some_seed")
        override fun collect(hero: Hero) {
            repeat(3) { Generator.SEED.generate().collect() }
        }
    },

    SOME_GOLD {
        override fun title(): String = M.L(Prize::class.java, "some_gold")
        override fun collect(hero: Hero) {
            val num = Random.NormalIntRange(50, 120)
            Gold.Purse().apply { number = num }.collect()
        }
    },

    NOTHING {
        override fun title(): String = M.L(Prize::class.java, "nothing")
        override fun collect(hero: Hero) {}
    },

    WHATEVER {
        override fun title(): String = M.L(Prize::class.java, "whatever")
        override fun collect(hero: Hero) {
            val p = Random.Float()
            when {
                p < 0.04 -> {
                    Dungeon.torch += 1
                }
                p < 0.08 -> {
                    Humanity().collect()
                }
                p < 0.1 -> {
                    // got nothing
                }
                else -> {
                    var prize = Prize.values().random()
                    while (prize == NOTHING || prize == WHATEVER) prize = Prize.values().random()
                    prize.collect(hero)
                }
            }
        }
    }
    ;

    abstract fun title(): String
    abstract fun collect(hero: Hero)
}
