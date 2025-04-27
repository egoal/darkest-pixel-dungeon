package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ScholarSprite
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.egoal.darkestpixeldungeon.windows.WndQuest

/**
 * Created by 93942 on 4/29/2018.
 */

class Scholar : NPC.Unbreakable() {
    init {
        name = Messages.get(this, "name")
        spriteClass = ScholarSprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    /// do something
    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        WndDialogue.Show(this, M.L(this, "hello"), M.L(this, "ask")) {
            WndDialogue.Show(this, M.L(this, "work"), M.L(this, "ask_pof")) {
                GameScene.show(WndQuest(this, M.L(this, "info_pof")))
            }
        }

        return false
    }
}
