package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.watabou.noosa.TextureFilm

class ArchDemon : NPC.Unbreakable() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun interact(): Boolean {
        WndDialogue.Show(this, M.L(this, "greetings"), M.L(this, "skillmodify"), M.L(this, "skillup")) {}

        return false
    }

    class Sprite : MobSprite() {
        init {
            texture(Assets.ARCH_DEMON)

            val frames = TextureFilm(texture, 16, 16)
            idle = Animation(1, true)
            idle.frames(frames, 0, 1, 2)

            die = Animation(20, false)
            die.frames(frames, 0)

            run = idle.clone()
            attack = idle.clone()

            play(idle)
        }
    }
}