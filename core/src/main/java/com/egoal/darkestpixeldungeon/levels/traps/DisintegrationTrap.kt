/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.levels.traps

import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Beam
import com.egoal.darkestpixeldungeon.items.bags.Bag
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.TrapSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class DisintegrationTrap : Trap() {

    init {
        color = TrapSprite.VIOLET
        shape = TrapSprite.LARGE_DOT
    }

    override fun activate() {

        if (Dungeon.visible[pos]) {
            sprite.parent.add(Beam.DeathRay(DungeonTilemap.tileCenterToWorld(pos - 1),
                    DungeonTilemap.tileCenterToWorld(pos + 1)))
            sprite.parent.add(Beam.DeathRay(DungeonTilemap.tileCenterToWorld(pos - Dungeon.level.width()),
                    DungeonTilemap.tileCenterToWorld(pos + Dungeon.level.width())))
            Sample.INSTANCE.play(Assets.SND_RAY)
        }

        val heap = Dungeon.level.heaps.get(pos)
        heap?.explode()

        val ch = Actor.findChar(pos)
        if (ch != null) {
            ch.takeDamage(Damage(Math.max(ch.HT / 5, Random.Int(ch.HP / 2, 2 * ch.HP / 3)),
                    this, ch).addElement(Damage.Element.SHADOW))
            if (ch === Dungeon.hero) {
                val hero = ch as Hero?
                if (!hero!!.isAlive) {
                    Dungeon.fail(javaClass)
                    GLog.n(Messages.get(this, "ondeath"))
                } else {
                    var item = hero.belongings.randomUnequipped()
                    var bag = hero.belongings.backpack
                    //bags do not protect against this trap
                    if (item is Bag) {
                        bag = item
                        item = Random.element(bag.items)
                    }
                    if (item == null || item.level() > 0 || item.unique) return
                    if (!item.stackable) {
                        item.detachAll(bag)
                        GLog.w(Messages.get(this, "one", item.name()))
                    } else {
                        val n = Random.NormalIntRange(1, (item.quantity() + 1) / 2)
                        for (i in 1..n)
                            item.detach(bag)
                        GLog.w(Messages.get(this, "some", item.name()))
                    }
                }
            }
        }

    }
}
