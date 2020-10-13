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
package com.egoal.darkestpixeldungeon.actors.hero

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.Disarm
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.NPC
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.items.keys.Key
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade
import com.egoal.darkestpixeldungeon.items.unclassified.Amulet
import com.egoal.darkestpixeldungeon.items.unclassified.Dewdrop
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.features.AlchemyPot
import com.egoal.darkestpixeldungeon.levels.features.EnchantingStation
import com.egoal.darkestpixeldungeon.levels.features.MagicWell
import com.egoal.darkestpixeldungeon.levels.features.Sign
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene
import com.egoal.darkestpixeldungeon.scenes.SurfaceScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndMessage
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.audio.Sample

abstract class HeroAction(var dst: Int = 0) {

    abstract fun act(hero: Hero): Boolean

    protected fun goThere(hero: Hero): Boolean {
        if (hero.getCloser(dst)) return true
        hero.ready()
        return false
    }

    class Move(dst: Int) : HeroAction(dst) {
        override fun act(hero: Hero): Boolean {
            return if (hero.getCloser(dst)) true else {
                if (Dungeon.level.map[hero.pos] == Terrain.SIGN)
                    Sign.Read(hero.pos)
                hero.ready()

                return false
            }
        }
    }

    class PickUp(dst: Int) : HeroAction(dst) {
        override fun act(hero: Hero): Boolean {
            if (hero.pos == dst) {
                val heap = Dungeon.level.heaps.get(dst)
                if (heap != null) {
                    val item = heap.peek()

                    if (item!!.doPickUp(hero)) {
                        heap.pickUp()

                        if (item is Dewdrop || item is TimekeepersHourglass.Companion.SandBag ||
                                item is DriedRose.Companion.Petal || item is Key) {
                            // do nothing, things happened in doPickUp
                        } else {
                            val important = (item is ScrollOfUpgrade && item.isKnown) ||
                                    (item is PotionOfStrength && item.isKnown) ||
                                    (item is PotionOfMight && item.isKnown)

                            if (important)
                                GLog.p(Messages.get(hero, "you_now_have", item.name()))
                            else
                                GLog.i(Messages.get(hero, "you_now_have", item.name()))
                        }

                        if (!heap.empty()) GLog.i(Messages.get(hero, "something_else"))

                        hero.curAction = null
                    } else {
                        heap.sprite!!.drop()
                        hero.ready()
                    }
                } else
                    hero.ready()

                return false
            }

            return goThere(hero)
        }
    }

    class OpenChest(dst: Int) : HeroAction(dst) {
        override fun act(hero: Hero): Boolean {
            if (Dungeon.level.adjacent(hero.pos, dst) || hero.pos == dst) {
                val heap = Dungeon.level.heaps.get(dst)

                if (heap != null && (heap.type != Heap.Type.HEAP)) {
                    if ((heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST) &&
                            hero.belongings.specialKeys[Dungeon.depth] < 1) {
                        GLog.w(Messages.get(hero, "locked_chest"))
                        hero.ready()

                        return false
                    }

                    when (heap.type) {
                        Heap.Type.TOMB -> {
                            Sample.INSTANCE.play(Assets.SND_TOMB)
                            Camera.main.shake(1f, 0.5f)
                        }
                        Heap.Type.SKELETON, Heap.Type.REMAINS -> {
                        }
                        else -> {
                            Sample.INSTANCE.play(Assets.SND_UNLOCK)
                        }
                    }

                    hero.spend(Key.TIME_TO_UNLOCK)
                    hero.sprite.operate(dst)
                } else
                    hero.ready()
                return false
            }

            return goThere(hero)
        }
    }

    class Interact(var npc: NPC) : HeroAction() {
        override fun act(hero: Hero): Boolean {
            if (Dungeon.level.adjacent(hero.pos, npc.pos)) {
                hero.ready()
                hero.sprite.turnTo(hero.pos, npc.pos)
                return npc.interact()
            }

            if (Level.fieldOfView[npc.pos] && hero.getCloser(npc.pos))
                return true

            hero.ready()
            return false
        }
    }

    class InteractAlly(var mob: Mob) : HeroAction() {
        override fun act(hero: Hero): Boolean {
            hero.ready()

            if (mob.camp == Char.Camp.HERO) {
                //todo: refactor
                WndDialogue.Show(mob, mob.description() + "\n\n" + M.L(Mob::class.java, "ally", mob.name) + "\n" + mob.state.status(),
                        M.L(Mob::class.java, "swap"),
                        M.L(Mob::class.java, "follow"),
                        M.L(Mob::class.java, "wander")) {
                    when (it) {
                        0 -> {
                            mob.swapPosition(hero)
                            hero.spendAndNext(1f / hero.speed())
                        }
                        1 -> if (mob.state != mob.FOLLOW_HERO) mob.state = mob.FOLLOW_HERO
                        else -> if (mob.state == mob.FOLLOW_HERO) mob.state = mob.WANDERING
                    }
                }
            }
            return false
        }
    }

    class Unlock(door: Int) : HeroAction(door) {
        override fun act(hero: Hero): Boolean {
            if (Dungeon.level.adjacent(hero.pos, dst)) {
                val hasKey = when (Dungeon.level.map[dst]) {
                    Terrain.LOCKED_DOOR -> hero.belongings.ironKeys[Dungeon.depth] > 0
                    Terrain.LOCKED_EXIT -> hero.belongings.specialKeys[Dungeon.depth] > 0
                    else -> false
                }

                if (hasKey) {
                    hero.spend(Key.TIME_TO_UNLOCK)
                    hero.sprite.operate(dst)
                    Sample.INSTANCE.play(Assets.SND_UNLOCK)
                } else {
                    GLog.w(Messages.get(hero, "locked_door"))
                    hero.ready()
                }
                return false
            }

            return goThere(hero)
        }
    }

    class Descend(stairs: Int) : HeroAction(stairs) {

        override fun act(hero: Hero): Boolean {
            if (hero.pos == dst && hero.pos == Dungeon.level.exit) {
                if (Dungeon.depth == 0) {
                    // leave village 
                }

                hero.curAction = null
                hero.buff(TimekeepersHourglass.TimeFreeze::class.java)?.let { it.detach() }

                InterlevelScene.mode = InterlevelScene.Mode.DESCEND
                Game.switchScene(InterlevelScene::class.java)
                return false
            }

            return goThere(hero)
        }
    }

    class Ascend(stairs: Int) : HeroAction(stairs) {
        override fun act(hero: Hero): Boolean {
            if (hero.pos == dst && hero.pos == Dungeon.level.entrance) {
                if (Dungeon.depth == 0) {
                    if (hero.belongings.getItem(Amulet::class.java) == null) {
                        GameScene.show(WndMessage(Messages.get(hero, "leave_village")))
                        hero.ready()
                    } else {
                        // end game
                        Dungeon.win(Amulet::class.java)
                        Dungeon.deleteGame(hero.heroClass, true, true)
                        Game.switchScene(SurfaceScene::class.java)
                    }
                } else if (Dungeon.depth == 1 && hero.belongings.getItem(Amulet::class.java) == null) {
                    GameScene.show(WndMessage(Messages.get(hero, "leave")))
                    hero.ready()
                } else {
                    hero.curAction = null

                    hero.buff(Hunger::class.java)!!.let {
                        if (it.isStarving) it.reduceHunger(-Hunger.STARVING / 10)
                    }
                    hero.buff(TimekeepersHourglass.TimeFreeze::class.java)?.detach()

                    InterlevelScene.mode = InterlevelScene.Mode.ASCEND
                    Game.switchScene(InterlevelScene::class.java)
                    return false
                }
            }

            return goThere(hero)
        }
    }

    // old style cook
    class Cook(pot: Int) : HeroAction(pot) {
        override fun act(hero: Hero): Boolean {
            if (Dungeon.level.adjacent(hero.pos, dst)) {
                hero.ready()
                AlchemyPot.Operate(hero, dst)

                return false
            }

            return goThere(hero)
        }
    }

    class Enchant(pos: Int) : HeroAction(pos) {
        override fun act(hero: Hero): Boolean {
            if (Dungeon.level.adjacent(hero.pos, dst)) {
                hero.ready()
                EnchantingStation.Operate(hero)

                return false
            }

            return goThere(hero)
        }
    }

    class Attack(var target: Char) : HeroAction() {
        override fun act(hero: Hero): Boolean {
            hero.enemy = target
            if (target.isAlive && hero.canAttack(target) && !hero.isCharmedBy(target) && hero.buff(Disarm::class.java) == null) {
                Invisibility.dispel()
                hero.spend(hero.attackDelay())
                hero.sprite.attack(target.pos)

                return false
            } else {
                if (Level.fieldOfView[target.pos] && hero.getCloser(target.pos))
                    return true

                hero.ready()
                return false
            }
        }
    }
}
