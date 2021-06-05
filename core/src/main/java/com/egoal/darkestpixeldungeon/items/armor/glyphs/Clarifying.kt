package com.egoal.darkestpixeldungeon.items.armor.glyphs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.KRandom
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Recharging
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.watabou.utils.GameMath

class Clarifying : Armor.Glyph() {
    override fun proc(armor: Armor, damage: Damage): Damage {
        val p = GameMath.clamp(25 + armor.level() * 5, 0, 50)
        if (KRandom.Percent(p))
            Buff.affect(Dungeon.hero, Recharging::class.java, 1f)

        return damage
    }

    override fun glowing(): ItemSprite.Glowing = CLR

    companion object {
        private val CLR = ItemSprite.Glowing(0x29afff)
    }
}