package com.egoal.darkestpixeldungeon.items.scrolls

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility
import com.egoal.darkestpixeldungeon.actors.buffs.Light
import com.egoal.darkestpixeldungeon.actors.buffs.Shock
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.ExpandHalo
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample

/**
 * Created by 93942 on 10/15/2018.
 */

class ScrollOfLight : Scroll() {
    init {
        initials = 12

        bones = true
    }

    override fun doRead() {
        // light!
        ExpandHalo(8f, 48f).show(Item.curUser.sprite, 0.5f)
        Sample.INSTANCE.play(Assets.SND_READ)
        Invisibility.dispel()

        // give light, shock nearby mobs
        Buff.affect(Item.curUser, Light::class.java).prolong(10f)

        Dungeon.level.mobs.filter { Level.fieldOfView[it.pos] }.forEach {
            val dis = Dungeon.level.distance(it.pos, Item.curUser.pos)
            if (dis <= AFFECT_RANGE && it.isAlive) {
                Buff.prolong(it, Shock::class.java, (AFFECT_RANGE - dis).toFloat())

                val b = Ballistica(Item.curUser.pos, it.pos, Ballistica.MAGIC_BOLT)
                if (b.path.size > b.dist + 1) {
                    val bb = Ballistica(it.pos, b.path[b.dist + 1], Ballistica.MAGIC_BOLT)
                    WandOfBlastWave.throwChar(it, bb, AFFECT_RANGE - dis)
                }
            }
        }

        GLog.i(Messages.get(this, "cast"))

        setKnown()
        readAnimation()
    }

    companion object {

        private const val AFFECT_RANGE = 8
    }


}

