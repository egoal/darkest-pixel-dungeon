package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.Frog
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.HealthIndicator
import com.egoal.darkestpixeldungeon.utils.GLog

class ReagentOfSorcery : Reagent(false) {
    init {
        image = ItemSpriteSheet.REAGENT_SORCERY
    }

    override fun shatter(cell: Int) {
        super.shatter(cell)

        Actor.findChar(cell)?.let {
            val props = it.properties()
            if (it.immunizedBuffs().contains(ReagentOfSorcery::class.java)) it.say("?")
            else if (props.contains(Char.Property.BOSS) || props.contains(Char.Property.MINIBOSS)) {
                it.say("?")
                GLog.n(M.L(this, "powerful"))
            } else if (it is Mob && !props.contains(Char.Property.PHANTOM)) {
                Actor.remove(it)
                Dungeon.level.mobs.remove(it)
                it.sprite.killAndErase()
                HealthIndicator.instance.target(null)

                val frog = Frog().apply {
                    lifespan = 5f
                    pos = it.pos
                    mob = it
                }
                GameScene.add(frog)
                CellEmitter.get(frog.pos).burst(Speck.factory(Speck.WOOL), 4)
                frog.say("咩？")
            } else if (it != curUser) {
                it.destroy()
                it.sprite.killAndErase()
                Dungeon.level.mobs.remove(it)
                HealthIndicator.instance.target(null)
            }
        }
    }
}