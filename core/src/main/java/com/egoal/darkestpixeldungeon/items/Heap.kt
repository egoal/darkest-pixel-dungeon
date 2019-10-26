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
package com.egoal.darkestpixeldungeon.items

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Frost
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mimic
import com.egoal.darkestpixeldungeon.actors.mobs.Wraith
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle
import com.egoal.darkestpixeldungeon.items.artifacts.Artifact
import com.egoal.darkestpixeldungeon.items.food.ChargrilledMeat
import com.egoal.darkestpixeldungeon.items.food.FrozenCarpaccio
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll
import com.egoal.darkestpixeldungeon.items.unclassified.Bomb
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundlable
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.LinkedList

class Heap : Bundlable {
    var type = Type.HEAP
    var pos = 0

//    var sprite: ItemSprite? = null
    lateinit var sprite: ItemSprite
    var seen = false

    var items: LinkedList<Item> = LinkedList()

    fun size(): Int = items.size
    fun empty(): Boolean = size() == 0

    enum class Type {
        HEAP,
        CHEST,
        LOCKED_CHEST,
        CRYSTAL_CHEST,
        TOMB,
        SKELETON,
        REMAINS,
        MIMIC
    }

    fun image(): Int = when (type) {
        Type.HEAP -> if (size() > 0) items.peek().image() else 0
        Type.CHEST, Type.MIMIC -> ItemSpriteSheet.CHEST
        Type.LOCKED_CHEST -> ItemSpriteSheet.LOCKED_CHEST
        Type.CRYSTAL_CHEST -> ItemSpriteSheet.CRYSTAL_CHEST
        Type.TOMB -> ItemSpriteSheet.TOMB
        Type.SKELETON -> ItemSpriteSheet.BONES
        Type.REMAINS -> ItemSpriteSheet.REMAINS
    }

    fun glowing(): ItemSprite.Glowing? = if (type == Type.HEAP && !empty()) items.peek().glowing() else null

    fun open(hero: Hero) {
        when (type) {
            Type.MIMIC -> if (Mimic.SpawnAt(pos, items) != null) {
                destroy()
            } else {
                type = Type.CHEST
            }
            Type.TOMB -> if (Random.Int(3) != 0) {
                Wraith.spawnAround(hero.pos)
                hero.takeDamage(Damage(Random.Int(3, 6), this, hero).type(Damage.Type.MENTAL))
            }
            Type.REMAINS, Type.SKELETON -> {
                CellEmitter.center(pos).start(Speck.factory(Speck.RATTLE), 0.1f, 3)
                for (item in items) {
                    if (item.cursed) {
                        item.cursedKnown = true
                        if (Wraith.spawnAt(pos) == null) {
                            //^ spawn failed, hurt hero directly
                            hero.sprite.emitter().burst(ShadowParticle.CURSE, 6)
                            hero.takeDamage(Damage(hero.HP / 2, this, hero))
                            hero.takeDamage(Damage(Random.Int(4, 12), this, hero).type(Damage.Type.MENTAL))
                        }
                        Sample.INSTANCE.play(Assets.SND_CURSED)
                        break
                    }
                }
            }
        }

        if (type != Type.MIMIC) {
            type = Type.HEAP
            sprite!!.link()
            sprite!!.drop()
        }
    }


    fun pickUp(): Item {

        val item = items.removeFirst()
        if (empty()) destroy()
        else if (sprite != null) {
            sprite!!.view(image(), glowing())
        }

        return item
    }

    fun peek(): Item? = items.peek()

    fun drop(item: Item) {
        var theItem = item
        if(theItem.stackable){
            items.find { it.isSimilar(theItem) }?.let {
                it.quantity += theItem.quantity
                theItem = it
            }
            items.remove(theItem)
        }

        if(theItem is Dewdrop) items.add(theItem)
        else items.addFirst(theItem)

        // update sprite
        if(::sprite.isInitialized){
            if(type== Type.HEAP) sprite.view(items.peek())
            else sprite.view(image(), glowing())
        }
    }

    fun replace(a: Item, b: Item) {
        val index = items.indexOf(a)
        if (index != -1) {
            items.removeAt(index)
            items.add(index, b)
        }
    }

    fun burn() {

        if (type == Type.MIMIC) {
            val m = Mimic.SpawnAt(pos, items)
            if (m != null) {
                Buff.affect(m, Burning::class.java).reignite(m)
                m.sprite.emitter().burst(FlameParticle.FACTORY, 5)
                destroy()
            }
        }

        if (type != Type.HEAP) {
            return
        }

        var burnt = false
        var evaporated = false

        for (item in items.toTypedArray()) {
            if (item is Scroll && item !is ScrollOfUpgrade) {
                items.remove(item)
                burnt = true
            } else if (item is Dewdrop) {
                items.remove(item)
                evaporated = true
            } else if (item is MysteryMeat) {
                replace(item, ChargrilledMeat.cook(item))
                burnt = true
            } else if (item is Bomb) {
                items.remove(item)
                item.explode(pos)
                //stop processing the burning, it will be replaced by the explosion.
                return
            }
        }

        if (burnt || evaporated) {

            if (Dungeon.visible[pos]) {
                if (burnt) {
                    burnFX(pos)
                } else {
                    evaporateFX(pos)
                }
            }

            if (empty()) destroy()
            else if (sprite != null) {
                sprite!!.view(items.peek())
            }

        }
    }

    //Note: should not be called to initiate an explosion, but rather by an
    // explosion that is happening.
    fun explode() {

        //breaks open most standard containers, mimics die.
        if (type == Type.MIMIC || type == Type.CHEST || type == Type.SKELETON) {
            type = Type.HEAP
            sprite!!.link()
            sprite!!.drop()
            return
        }

        if (type != Type.HEAP) {

            return

        } else {

            for (item in items.toTypedArray()) {

                if (item is Potion) {
                    items.remove(item)
                    item.shatter(pos)

                } else if (item is Bomb) {
                    items.remove(item)
                    item.explode(pos)
                    //stop processing current explosion, it will be replaced by the new
                    // one.
                    return

                    //unique and upgraded items can endure the blast
                } else if (!(item.level() > 0 || item.unique))
                    items.remove(item)

            }

            if (empty()) {
                destroy()
            } else if (sprite != null) {
                sprite!!.view(items.peek())
            }
        }
    }

    fun freeze() {

        if (type == Type.MIMIC) {
            val m = Mimic.SpawnAt(pos, items)
            if (m != null) {
                Buff.prolong(m, Frost::class.java, Frost.duration(m) * Random.Float(1.0f,
                        1.5f))
                destroy()
            }
        }

        if (type != Type.HEAP) {
            return
        }

        var frozen = false
        for (item in items.toTypedArray()) {
            if (item is MysteryMeat) {
                replace(item, FrozenCarpaccio.cook(item))
                frozen = true
            } else if (item is Potion && !(item is PotionOfStrength || item is PotionOfMight)) {
                items.remove(item)
                item.shatter(pos)
                frozen = true
            } else if (item is Bomb) {
                item.fuse = null
                frozen = true
            }
        }

        if (frozen) {
            if (empty()) {
                destroy()
            } else if (sprite != null) {
                sprite!!.view(items.peek())
            }
        }
    }

    fun destroy() {
        Dungeon.level.heaps.remove(this.pos)
        if (sprite != null) {
            sprite!!.kill()
        }
        items.clear()
    }

    override fun toString(): String = when (type) {
        Type.CHEST, Type.MIMIC -> M.L(this, "chest")
        Type.LOCKED_CHEST -> M.L(this, "locked_chest")
        Type.CRYSTAL_CHEST -> M.L(this, "crystal_chest")
        Type.TOMB -> M.L(this, "tomb")
        Type.SKELETON -> M.L(this, "skeleton")
        Type.REMAINS -> M.L(this, "remains")
        else -> peek()!!.toString()
    }

    fun info(): String = when (type) {
        Type.CHEST, Type.MIMIC -> M.L(this, "chest_desc")
        Type.LOCKED_CHEST -> M.L(this, "locked_chest_desc")
        Type.CRYSTAL_CHEST -> when (peek()) {
            is Artifact -> M.L(this, "crystal_chest_desc", M.L(this, "artifact"))
            is Wand -> M.L(this, "crystal_chest_desc", M.L(this, "wand"))
            else -> Messages.get(this, "crystal_chest_desc", M.L(this, "ring"))
        }
        Type.TOMB -> M.L(this, "tomb_desc")
        Type.SKELETON -> M.L(this, "skeleton_desc")
        Type.REMAINS -> M.L(this, "remains_desc")
        else -> peek()!!.info()
    }

    override fun restoreFromBundle(bundle: Bundle) {
        pos = bundle.getInt(POS)
        seen = bundle.getBoolean(SEEN)
        type = Type.valueOf(bundle.getString(TYPE))
        items.addAll(bundle.getCollection(ITEMS) as Collection<Item>)
    }

    override fun storeInBundle(bundle: Bundle) {
        bundle.put(POS, pos)
        bundle.put(SEEN, seen)
        bundle.put(TYPE, type.toString())
        bundle.put(ITEMS, items)
    }

    companion object {
        fun burnFX(pos: Int) {
            CellEmitter.get(pos).burst(ElmoParticle.FACTORY, 6)
            Sample.INSTANCE.play(Assets.SND_BURNING)
        }

        fun evaporateFX(pos: Int) {
            CellEmitter.get(pos).burst(Speck.factory(Speck.STEAM), 5)
        }

        private const val POS = "pos"
        private const val SEEN = "seen"
        private const val TYPE = "type"
        private const val ITEMS = "items"
    }

}
