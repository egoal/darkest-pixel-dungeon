package com.egoal.darkestpixeldungeon.actors.mobs.npcs

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.unclassified.Amulet
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.SimpleMobSprite
import com.egoal.darkestpixeldungeon.windows.WndDialogue
import com.watabou.utils.Random

class SPDBattleMage : NPC.Unbreakable() {
    init {
        spriteClass = Sprite::class.java

        properties.add(Property.IMMOVABLE)
    }

    override fun interact(): Boolean {
        sprite.turnTo(pos, Dungeon.hero.pos)

        if (Dungeon.hero.belongings.getItem(Amulet::class.java) == null) {
        } else {
        }

        WndDialogue.Show(this, M.L(this, "greetings"), M.L(this, "ac_yourself"), M.L(this, "ac_suggest")) {
            onSelectHero(it)
        }

        return false
    }

    private fun onSelectHero(index: Int) {
        when (index) {
            0 -> tell(Messages.get(this, "introduction"))
            1 -> {
                val a = Dungeon.hero.belongings.getItem(Amulet::class.java)
                tell(Messages.get(this, if (a == null) "suggestion_${Random.Int(4)}" else "suggestion_no"))
            }
        }
    }

    companion object {
        class Sprite : SimpleMobSprite(Assets.SPD_BATTLE_MAGE) {
            override fun link(ch: Char) {
                super.link(ch)

                add(State.CHILLED)
            }
        }
    }

    override fun reset() = true
}