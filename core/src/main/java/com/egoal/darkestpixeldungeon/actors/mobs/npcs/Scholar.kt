package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ScholarSprite
import com.egoal.darkestpixeldungeon.utils.GLog
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
        tell(Messages.get(this, "hello"))

        return false
    }

    override fun reset(): Boolean = true
}
