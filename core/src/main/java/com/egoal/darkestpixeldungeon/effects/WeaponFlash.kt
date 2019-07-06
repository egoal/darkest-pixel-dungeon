package com.egoal.darkestpixeldungeon.effects

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.Char
import com.watabou.noosa.MovieClip
import com.watabou.noosa.TextureFilm
import com.watabou.utils.PointF

class WeaponFlash : MovieClip(), MovieClip.Listener {
    val anim = Animation(15, false)

    init {
        listener = this

        texture("miscs/attack0.png")
        anim.frames(TextureFilm(texture, 8, 9), 0, 1, 2, 3)

        origin.set(0f, 4f)

        play(anim)
    }

    override fun onComplete(anim: Animation) {
        kill()
    }

    fun reset(p: Int) {
        revive()
        val pt = Dungeon.level.cellToPoint(p)
        x = (pt.x * DungeonTilemap.SIZE).toFloat()
        y = (pt.y * DungeonTilemap.SIZE).toFloat() + 4f

        play(anim)
    }

    companion object {
        fun Flash(attacker: Char, defender: Char) {
            if (defender.sprite.parent != null) {
                val wf = defender.sprite.parent.recycle(WeaponFlash::class.java) as WeaponFlash
                defender.sprite.parent.bringToFront(wf)
                wf.reset(defender.pos)
                wf.angle = PointF.angle(attacker.sprite.center(), defender.sprite.center())
                wf.angle = Math.PI.toFloat() / 4f
            }
        }
    }
}