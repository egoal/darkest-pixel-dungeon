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
package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Frost
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mimic
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.unclassified.Bomb
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRecharging
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.levels.traps.CursingTrap
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.ui.HealthIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.ConfusionGas
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.blobs.ParalyticGas
import com.egoal.darkestpixeldungeon.actors.blobs.Regrowth
import com.egoal.darkestpixeldungeon.actors.blobs.ToxicGas
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Sheep
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.effects.SpellSprite
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap
import com.egoal.darkestpixeldungeon.messages.Languages
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene
import com.watabou.noosa.Game
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.Random

import java.io.IOException
import java.util.ArrayList

//helper class to contain all the cursed wand zapping logic, so the main wand
// class doesn't get huge.
object CursedWand {
    private const val COMMON_CHANCE = 0.6f
    private const val UNCOMMON_CHANCE = 0.3f
    private const val RARE_CHANCE = 0.09f
    private const val VERY_RARE_CHANCE = 0.01f

    fun cursedZap(wand: Wand, user: Hero, bolt: Ballistica) {
        when (Random.chances(floatArrayOf(COMMON_CHANCE, UNCOMMON_CHANCE, RARE_CHANCE, VERY_RARE_CHANCE))) {
            0 -> commonEffect(wand, user, bolt)
            1 -> uncommonEffect(wand, user, bolt)
            2 -> rareEffect(wand, user, bolt)
            3 -> veryRareEffect(wand, user, bolt)
            else -> commonEffect(wand, user, bolt)
        }
    }

    private fun commonEffect(wand: Wand, user: Hero, bolt: Ballistica) {
        when (Random.Int(4)) {

            //anti-entropy
            0 -> cursedFX(user, bolt, Callback {
                val target = Actor.findChar(bolt.collisionPos)
                when (Random.Int(2)) {
                    0 -> {
                        if (target != null) Buff.affect(target, Burning::class.java).reignite(target)
                        Buff.affect(user, Frost::class.java, Frost.duration(user) * Random.Float(3f, 5f))
                    }
                    1 -> {
                        Buff.affect(user, Burning::class.java).reignite(user)
                        if (target != null)
                            Buff.affect(target, Frost::class.java, Frost.duration(target) * Random.Float(3f, 5f))
                    }
                }
                wand.wandUsed()
            })

            //spawns some regrowth
            1 -> cursedFX(user, bolt, Callback {
                val c = Dungeon.level.map[bolt.collisionPos]
                if (c == Terrain.EMPTY || c == Terrain.EMBERS || c == Terrain.EMPTY_DECO || c == Terrain.GRASS || c == Terrain.HIGH_GRASS) {
                    GameScene.add(Blob.seed(bolt.collisionPos, 30, Regrowth::class.java))
                }
                wand.wandUsed()
            })

            //random teleportation
            2 -> when (Random.Int(2)) {
                0 -> {
                    ScrollOfTeleportation.teleportHero(user)
                    wand.wandUsed()
                }
                1 -> cursedFX(user, bolt, Callback {
                    val ch = Actor.findChar(bolt.collisionPos)
                    if (ch != null && !ch.properties().contains(Char.Property.IMMOVABLE)) {
                        var count = 10
                        var pos: Int
                        do {
                            pos = Dungeon.level.randomRespawnCell()
                            if (count-- <= 0) {
                                break
                            }
                        } while (pos == -1)
                        if (pos == -1 || Dungeon.bossLevel()) {
                            GLog.w(Messages.get(ScrollOfTeleportation::class.java, "no_tele"))
                        } else {
                            ch.pos = pos
                            if (ch is Mob && ch.state === ch.HUNTING)
                                ch.state = ch.WANDERING

                            ch.sprite.place(ch.pos)
                            ch.sprite.visible = Dungeon.visible[pos]
                        }
                    }
                    wand.wandUsed()
                })
            }

            //random gas at location
            3 -> cursedFX(user, bolt, Callback {
                when (Random.Int(3)) {
                    0 -> GameScene.add(Blob.seed(bolt.collisionPos, 800, ConfusionGas::class.java))
                    1 -> GameScene.add(Blob.seed(bolt.collisionPos, 500, ToxicGas::class.java))
                    2 -> GameScene.add(Blob.seed(bolt.collisionPos, 200, ParalyticGas::class.java))
                }
                wand.wandUsed()
            })
        }

    }

    private fun uncommonEffect(wand: Wand, user: Hero, bolt: Ballistica) {
        when (Random.Int(4)) {

            //Random plant
            0 -> cursedFX(user, bolt, Callback {
                var pos = bolt.collisionPos
                //place the plant infront of an enemy so they walk into it.
                if (Actor.findChar(pos) != null && bolt.dist > 1) {
                    pos = bolt.path[bolt.dist - 1]
                }

                if (pos == Terrain.EMPTY ||
                        pos == Terrain.EMBERS ||
                        pos == Terrain.EMPTY_DECO ||
                        pos == Terrain.GRASS ||
                        pos == Terrain.HIGH_GRASS) {
                    Dungeon.level.plant(Generator.SEED.generate() as Plant.Seed, pos)
                }
                wand.wandUsed()
            })

            //Health transfer
            1 -> {
                val target = Actor.findChar(bolt.collisionPos)
                if (target != null) {
                    cursedFX(user, bolt, object : Callback {
                        override fun call() {
                            // int damage = user.lvl * 2;
                            val damage = Damage(user.lvl * 2, this, target).type(Damage.Type.MAGICAL)
                            when (Random.Int(2)) {
                                0 -> {
                                    user.HP = Math.min(user.HT, user.HP + damage.value)
                                    user.sprite.emitter().burst(Speck.factory(Speck.HEALING), 3)
                                    target.takeDamage(damage)
                                    target.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)
                                }
                                1 -> {
                                    user.takeDamage(damage)
                                    user.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10)
                                    target.HP = Math.min(target.HT, target.HP + damage.value)
                                    target.sprite.emitter().burst(Speck.factory(Speck.HEALING),
                                            3)
                                    Sample.INSTANCE.play(Assets.SND_CURSED)
                                    if (!user.isAlive) {
                                        Dungeon.fail(wand.javaClass)
                                        Badges.validateSuicide()
                                        GLog.n(Messages.get(CursedWand::class.java, "ondeath", wand.name()))
                                    }
                                }
                            }
                            wand.wandUsed()
                        }
                    })
                } else {
                    GLog.i(Messages.get(CursedWand::class.java, "nothing"))
                    wand.wandUsed()
                }
            }

            //Bomb explosion
            2 -> cursedFX(user, bolt, Callback {
                Bomb().explode(bolt.collisionPos)
                wand.wandUsed()
            })

            //shock and recharge
            3 -> {
                LightningTrap().set(user.pos).activate()
                Buff.prolong(user, Recharging::class.java, 20f)
                ScrollOfRecharging.charge(user)
                SpellSprite.show(user, SpellSprite.CHARGE)
                wand.wandUsed()
            }
        }

    }

    private fun rareEffect(wand: Wand, user: Hero, bolt: Ballistica) {
        when (Random.Int(4)) {

            //sheep transformation
            0 -> cursedFX(user, bolt, Callback {
                val ch = Actor.findChar(bolt.collisionPos)

                if (ch != null && ch !== user
                        && !ch.properties().contains(Char.Property.BOSS)
                        && !ch.properties().contains(Char.Property.MINIBOSS)) {
                    val sheep = Sheep()
                    sheep.lifespan = 10f
                    sheep.pos = ch.pos
                    ch.destroy()
                    ch.sprite.killAndErase()
                    Dungeon.level.mobs.remove(ch)
                    HealthIndicator.instance.target(null)
                    GameScene.add(sheep)
                    CellEmitter.get(sheep.pos).burst(Speck.factory(Speck.WOOL), 4)
                } else {
                    GLog.i(Messages.get(CursedWand::class.java, "nothing"))
                }
                wand.wandUsed()
            })

            //curses!
            1 -> {
                CursingTrap.curse(user)
                wand.wandUsed()
            }

            //inter-level teleportation
            2 -> if (Dungeon.depth > 1 && !Dungeon.bossLevel()) {

                //each depth has 1 more weight than the previous depth.
                val depths = FloatArray(Dungeon.depth - 1)
                for (i in 1 until Dungeon.depth) depths[i - 1] = i.toFloat()
                val depth = 1 + Random.chances(depths)

                val buff = Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze::class.java)
                buff?.detach()

                for (mob in Dungeon.level.mobs.toTypedArray())
                    if (mob is GhostHero) mob.destroy()

                InterlevelScene.mode = InterlevelScene.Mode.RETURN
                InterlevelScene.returnDepth = depth
                InterlevelScene.returnPos = -1
                Game.switchScene(InterlevelScene::class.java)

            } else {
                ScrollOfTeleportation.teleportHero(user)
                wand.wandUsed()
            }

            //summon monsters
            3 -> {
                SummoningTrap().set(user.pos).activate()
                wand.wandUsed()
            }
        }
    }

    private fun veryRareEffect(wand: Wand, user: Hero, bolt: Ballistica) {
        when (Random.Int(3)) {

            //great forest fire!
            0 -> {
                for (i in 0 until Dungeon.level.length()) {
                    val c = Dungeon.level.map[i]
                    if (c == Terrain.EMPTY || c == Terrain.EMBERS || c == Terrain.EMPTY_DECO ||
                            c == Terrain.GRASS || c == Terrain.HIGH_GRASS) {
                        GameScene.add(Blob.seed(i, 15, Regrowth::class.java))
                    }
                }
                do {
                    GameScene.add(Blob.seed(Dungeon.level.randomDestination(), 10, Fire::class.java))
                } while (Random.Int(5) != 0)
                Flare(8, 32f).color(0xFFFF66, true).show(user.sprite, 2f)
                Sample.INSTANCE.play(Assets.SND_TELEPORT)
                GLog.p(Messages.get(CursedWand::class.java, "grass"))
                GLog.w(Messages.get(CursedWand::class.java, "fire"))
                wand.wandUsed()
            }

            //superpowered mimic
            1 -> cursedFX(user, bolt, Callback {
                val mimic = Mimic.SpawnAt(bolt.collisionPos, ArrayList())
                mimic!!.adjustStatus(Dungeon.depth + 10)
                mimic.HP = mimic.HT
                var reward: Item
                do {
                    reward = Random.oneOf(Generator.WEAPON, Generator.ARMOR, Generator.RING, Generator.WAND).generate()
                } while (reward.level() < 2 && reward !is MissileWeapon)
                Sample.INSTANCE.play(Assets.SND_MIMIC, 1f, 1f, 0.5f)
                mimic.items.clear()
                mimic.items.add(reward)

                wand.wandUsed()
            })

            //crashes the game, yes, really.
//            2 -> try {
//                Dungeon.saveAll()
//                if (Messages.lang() !== Languages.ENGLISH) {
//                    //Don't bother doing this joke to none-english speakers, I doubt it would translate.
//                    GLog.i(Messages.get(CursedWand::class.java, "nothing"))
//                    wand.wandUsed()
//                } else {
//                    GameScene.show(
//                            object : WndOptions("CURSED WAND ERROR", "this application will now self-destruct", "abort", "retry", "fail") {
//                                override fun hide() {
//                                    throw RuntimeException("critical wand exception")
//                                }
//                            }
//                    )
//                }
//            } catch (e: IOException) {
//                DarkestPixelDungeon.reportException(e)
//                //oookay maybe don't kill the game if the save failed.
//                GLog.i(Messages.get(CursedWand::class.java, "nothing"))
//                wand.wandUsed()
//            }

            //random transmogrification
            2 -> {
                wand.wandUsed()
                wand.detach(user.belongings.backpack)
                var result: Item
                do {
                    result = Random.oneOf(Generator.WEAPON, Generator.ARMOR, Generator.RING, Generator.ARTIFACT).generate()
                } while (result.level() < 0 && result !is MissileWeapon)
                if (result.isUpgradable) result.upgrade()
                result.cursedKnown = true
                result.cursed = result.cursedKnown
                GLog.w(Messages.get(CursedWand::class.java, "transmogrify"))
                Dungeon.level.drop(result, user.pos).sprite.drop()
                wand.wandUsed()
            }
        }
    }

    private fun cursedFX(user: Hero, bolt: Ballistica, callback: Callback) {
        MagicMissile.rainbow(user.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

}
