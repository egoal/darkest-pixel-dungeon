package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Sheep
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
            if (props.contains(Char.Property.BOSS) || props.contains(Char.Property.MINIBOSS))
                GLog.n(M.L(this, "powerful"))
            else if (props.contains(Char.Property.PHANTOM)) {
                it.destroy()
                it.sprite.killAndErase()
                Dungeon.level.mobs.remove(it)
                HealthIndicator.instance.target(null)
            } else {
                // todo: keep it, just transformation
                it.destroy()
                it.sprite.killAndErase()
                Dungeon.level.mobs.remove(it)
                HealthIndicator.instance.target(null)

                val sheep = Sheep().apply {
                    lifespan = 5f
                    pos = it.pos
                }
                GameScene.add(sheep)
                CellEmitter.get(sheep.pos).burst(Speck.factory(Speck.WOOL), 4)
                sheep.say("咩？")
            }
        }
    }
}