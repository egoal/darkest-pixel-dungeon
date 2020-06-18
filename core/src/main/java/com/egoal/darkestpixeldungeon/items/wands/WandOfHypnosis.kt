package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.ConfusionGas
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Drowsy
import com.egoal.darkestpixeldungeon.actors.buffs.MagicalSleep
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Random
import kotlin.math.max

class WandOfHypnosis : Wand() {
    init {
        image = ItemSpriteSheet.WAND_HYPNOSIS
    }

    override fun onZap(attack: Ballistica) {
        val cell = attack.collisionPos
        Actor.findChar(cell)?.let {
            Buff.affect(it, MagicalSleep.Deep::class.java)
            it.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 5)
        }

        GameScene.add(Blob.seed(cell, 100, ConfusionGas::class.java))
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        val level = max(0, staff.level())
        if (Random.Int(level + 6) >= 4) {
            val to = damage.to as Char
            Buff.affect(to, Vertigo::class.java, Vertigo.duration(to))
        }
    }
}