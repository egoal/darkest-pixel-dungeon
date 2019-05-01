package com.egoal.darkestpixeldungeon.levels.features

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.AbyssHero
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass
import com.egoal.darkestpixeldungeon.levels.RegularLevel
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

object Chasm {
    var JumpConfirmed = false

    fun HeroJump(hero: Hero) {
        GameScene.show(object : WndOptions(Messages.get(Chasm::class.java, "chasm"),
                Messages.get(Chasm::class.java, "jump"),
                Messages.get(Chasm::class.java, "yes"), Messages.get(Chasm::class.java, "no")) {
            override fun onSelect(index: Int) {
                if (index == 0) {
                    JumpConfirmed = true
                    hero.resume()
                }
            }
        })
    }

    fun HeroFall(pos: Int) {
        JumpConfirmed = false

        Sample.INSTANCE.play(Assets.SND_FALLING)

        Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze::class.java)?.detach()

        Dungeon.level.mobs.filter { it is GhostHero || it is AbyssHero }.forEach { it.destroy() }

        if (!Dungeon.hero.isAlive) {
            // dead before jump?...
            Dungeon.hero.sprite.visible = false
        } else {
            Dungeon.hero.interrupt()
            InterlevelScene.mode = InterlevelScene.Mode.FALL

            InterlevelScene.fallIntoPit = (Dungeon.level is RegularLevel) &&
                    (Dungeon.level as RegularLevel).spaceAt(pos)?.type == DigResult.Type.WeakFloor

            Game.switchScene(InterlevelScene::class.java)
        }
    }

    fun HeroLand() {
        Dungeon.hero.apply {
            sprite.burst(sprite.blood(), 10)
            takeDamage(Damage(Random.NormalIntRange(HP / 4, HT / 4), {
                Badges.validateDeathFromFalling()
                Dungeon.fail(javaClass)
                GLog.n(Messages.get(Chasm::class.java, "ondeath"))
            }, this))

            Buff.prolong(this, Cripple::class.java, Cripple.DURATION)
            Buff.affect(this, Bleeding::class.java).set(HT / 8)
        }

        Camera.main.shake(4f, 0.2f)
    }

    fun MobFall(mob: Mob) {
        mob.die(null)
        (mob.sprite as MobSprite).fall()
    }
}