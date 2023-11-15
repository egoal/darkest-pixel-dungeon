package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mimic
import com.egoal.darkestpixeldungeon.effects.Splash
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class EmptyBottle : MissileWeapon(1) {
    init {
        image = ItemSpriteSheet.POTION_EMPTY_BOTTLE
        DLY = 1f
    }

    override fun unitPrice(): Int = 3

    override fun breakChance(): Float = 1f

    override fun min(lvl: Int): Int = 4
    override fun max(lvl: Int): Int = 8

    override fun miss(cell: Int) {
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
        Splash.at(cell, 0xffffff, 5)
    }

    override fun proc(dmg: Damage): Damage {
        val ch = dmg.to as Char
        if (Random.Int(2) == 0) Buff.affect(ch, Bleeding::class.java).set(curUser.STR)
        else Buff.prolong(ch, Vertigo::class.java, 3f)

        Splash.at(ch.pos, 0xffffff, 5)

        return super.proc(dmg)
    }

    override fun accuracyFactor(hero: Hero, target: Char): Float = super.accuracyFactor(hero, target) * 1.5f

    companion object {
        fun produce(quantity: Int = 1) {
            val eb = EmptyBottle()
            eb.quantity = quantity
            if (!eb.collect(Dungeon.hero.belongings.backpack))
                Dungeon.level.drop(eb, Dungeon.hero.pos)
        }
    }
}