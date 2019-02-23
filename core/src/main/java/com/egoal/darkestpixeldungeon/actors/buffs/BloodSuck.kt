package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.ui.BuffIndicator
import com.watabou.utils.Random

// called in Hero::onKillChar
class BloodSuck : FlavourBuff() {
    init {
        type = buffType.POSITIVE
    }
    
    override fun icon(): Int = BuffIndicator.BLOOD_SUCK

    override fun toString(): String = Messages.get(this, "name")

    override fun desc(): String = Messages.get(this, "desc", dispTurns())

    fun onEnemySlayed(c: Char) {
        var d = Random.IntRange(1, 3)
        if (c is Mob && c.exp() > 0)
            d += Random.IntRange(1, 3)

        target.HT += d
        target.HP += d

        target.sprite.showStatus(CharSprite.WARNING, Messages.get(this, "nibble"))
    }
}