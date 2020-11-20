package com.egoal.darkestpixeldungeon.items.weapon.curses

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Rage
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class Provocation : Inscription.Curse(6) {
    override fun proc(weapon: Weapon, damage: Damage): Damage {
        if (Random.Int(10) == 0 && damage.to is Char) {
            (damage.from as Char).sprite.centerEmitter().start(Speck.factory(Speck.SCREAM), 0.3f, 3)
            Sample.INSTANCE.play(Assets.SND_MIMIC)

            GLog.w(M.L(this, "msg_" + (Random.Int(4))))
            Buff.prolong(damage.to as Char, Rage::class.java, Random.Float(2f, 5f))
        }

        return damage
    }
}