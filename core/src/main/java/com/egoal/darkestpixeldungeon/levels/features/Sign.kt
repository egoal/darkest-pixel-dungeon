package com.egoal.darkestpixeldungeon.levels.features

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.levels.DeadEndLevel
import com.egoal.darkestpixeldungeon.levels.Terrain
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndMessage
import com.watabou.noosa.audio.Sample

object Sign {

    fun ShowInDepth(depth: Int) = depth in listOf(0, 5, 6, 11, 15, 16, 20, 21)

    fun Read(pos: Int) {
        if (Dungeon.level is DeadEndLevel)
            GameScene.show(WndMessage(Messages.get(Sign::class.java, "dead_end")))
        else {
            if (ShowInDepth(Dungeon.depth))
                GameScene.show(WndMessage(Messages.get(Sign::class.java, "tip_${Dungeon.depth}")))
            else {
                // destroy 
                Dungeon.level.destroy(pos)
                GameScene.updateMap(pos)
                GameScene.discoverTile(pos, Terrain.SIGN)

                GLog.w(Messages.get(Sign::class.java, "burn"))

                CellEmitter.get(pos).burst(ElmoParticle.FACTORY, 6)
                Sample.INSTANCE.play(Assets.SND_BURNING)
            }
        }
    }

}