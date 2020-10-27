package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.Statistics
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.MobSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.egoal.darkestpixeldungeon.windows.WndQuest
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm

/**
 * Created by 93942 on 10/10/2018.
 */

class Minstrel : NPC.Unbreakable() {
    init {
        spriteClass = MinstrelSprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        WndDialogue.Show(this, M.L(this, "hello"),
                M.L(this, "ac_sing"), M.L(this, "ac_yourself"), M.L(this, "ac_leave")) {
            onSelectHello(it)
        }

        return false
    }

    // unbreakable
    override fun reset() = true

    override fun act(): Boolean {
        // leave after some time
        if (Statistics.Duration > 8000f) {
            die(null)
        }

        return super.act()
    }

    private fun onSelectHello(index: Int) {
        // 0 sing, 1 leave
        when (index) {
            0 -> {
                val poetries = arrayOf("away")
                GameScene.show(object : WndOptions(MinstrelSprite(), name,
                        Messages.get(Minstrel::class.java, "select_poetry"),
                        *poetries) {
                    override fun onSelect(index: Int) {
                        tell(Messages.get(Minstrel::class.java, "poetry_" + poetries[index]))
                    }
                })
            }
            1 -> tell(Messages.get(Minstrel::class.java, "introduction"))
            2 -> say(Messages.get(Minstrel::class.java, "farewell"))
        }
    }

    class MinstrelSprite : MobSprite() {
        init {
            texture(Assets.MINSTREL)

            // set animations
            val frames = TextureFilm(texture, 12, 15)
            idle = Animation(1, true)
            idle.frames(frames, 0, 1)

            run = Animation(20, true)
            run.frames(frames, 0)

            die = Animation(20, true)
            die.frames(frames, 0)

            play(idle)
        }

    }
}
