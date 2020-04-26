package com.egoal.darkestpixeldungeon.items.scrolls

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Drowsy
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample

class ScrollOfLullaby : Scroll() {
    init {
        initials = 1
    }

    override fun doRead() {
        Item.curUser.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 5)
        Sample.INSTANCE.play(Assets.SND_LULLABY)
        Invisibility.dispel()

        Dungeon.level.mobs.filter { Level.fieldOfView[it.pos] }.forEach {
            Buff.affect(it, Drowsy::class.java)
            it.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 5)
        }

        Buff.affect(Item.curUser, Drowsy::class.java)

        GLog.i(M.L(this, "sooth"))
        setKnown()
        readAnimation()
    }

    override fun price(): Int = if (isKnown) 50 * quantity else super.price()
}