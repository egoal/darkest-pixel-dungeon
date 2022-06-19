package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random
import kotlin.math.pow

class ShortSticks : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.SHORT_STICKS
        tier = 2
        DLY = 0.5f
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) - 1

    override fun min(lvl: Int): Int = tier + 1 + lvl
    override fun max(lvl: Int): Int = 3 * (tier + 1) + lvl * tier

    override fun defendDamage(dmg: Damage): Damage {
        if (dmg.type == Damage.Type.NORMAL) {
            val c = .1f + .25f * (1f - 0.7f.pow(level() / 3f))
            if (Random.Float() < c) {
                dmg.value = 0
                (dmg.to as Char).sprite.showStatus(CharSprite.POSITIVE, M.L(this, "block"))
            } else dmg.value -= Random.IntRange(1, level() + 1)
        }

        return super.defendDamage(dmg)
    }
}