package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo
import com.egoal.darkestpixeldungeon.actors.mobs.Mimic
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class EmptyBottle : Item() {
    init {
        image = ItemSpriteSheet.POTION_EMPTY_BOTTLE

        stackable = true
        usesTargeting = true

        defaultAction = AC_THROW
    }

    override val isUpgradable: Boolean get() = false
    override val isIdentified: Boolean get() = true

    override fun price(): Int = 3 * quantity

    override fun onThrow(cell: Int) {
        if (Level.pit[cell]) super.onThrow(cell)
        else {
            val ch = Actor.findChar(cell)
            val hit = Random.Int(10) != 0
            if (ch != null && hit) {
                ch.takeDamage(Damage(curUser.STR, curUser, ch))
                if (ch.isAlive) {
                    if (Random.Int(2) == 0) Buff.affect(ch, Bleeding::class.java).set(curUser.STR)
                    else Buff.prolong(ch, Vertigo::class.java, 3f)
                }

                Sample.INSTANCE.play(Assets.SND_HIT, 1f, 1f, Random.Float(.8f, 1.25f))
            } else {
                ch?.let {
                    it.sprite.showStatus(CharSprite.NEUTRAL, it.defenseVerb())
                }

                Dungeon.level.press(cell, null)

                Dungeon.level.mobs
                        .filter { Dungeon.level.distance(it.pos, cell) <= 8 }
                        .forEach { it.beckon(cell) }

                Dungeon.level.heaps.values()
                        .filter { it.type == Heap.Type.MIMIC && Dungeon.level.distance(it.pos, cell) <= 8 }
                        .forEach { heap ->
                            Mimic.SpawnAt(heap.pos, heap.items)?.let {
                                it.beckon(cell)
                                heap.destroy()
                            }
                        }

                Sample.INSTANCE.play(Assets.SND_SHATTER)
            }

            Splash.at(cell, 0xffffff, 5)
        }
    }

    companion object {
        fun produce(quantity: Int = 1) {
            val eb = EmptyBottle()
            eb.quantity = quantity
            if (!eb.collect(Dungeon.hero.belongings.backpack))
                Dungeon.level.drop(eb, Dungeon.hero.pos)
        }
    }
}