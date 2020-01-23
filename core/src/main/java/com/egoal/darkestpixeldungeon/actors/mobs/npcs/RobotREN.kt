package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm

class RobotREN : NPC.Unbreakable() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        WndDialogue.Show(this, M.L(this, "greetings"), M.L(this, "ac_yourself"), M.L(this, "ac_wherefrom")) {
            when (it) {
                0 -> tell(M.L(this, "introduction"))
                1 -> tell(M.L(this, "wherefrom"))
            }
        }

        return false
    }

    companion object {
        class Sprite : MobSprite() {
            init {
                texture(Assets.REN)

                val frames = TextureFilm(texture, 12, 14)
                idle = Animation(1, true)
                idle.frames(frames, 0, 1, 2, 3)

                run = Animation(20, true)
                run.frames(frames, 0)

                die = Animation(20, true)
                die.frames(frames, 0)

                play(idle)
            }
        }
    }
}