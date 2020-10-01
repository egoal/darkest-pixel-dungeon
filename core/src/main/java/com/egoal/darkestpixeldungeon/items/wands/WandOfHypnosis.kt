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
import com.watabou.utils.PointF
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class WandOfHypnosis : DamageWand.NoDamage(isMissile = true) {
    init {
        image = ItemSpriteSheet.WAND_HYPNOSIS
    }

    override fun initialCharges(): Int = 1

    override fun updateLevel() {
        maxCharges = (1f + sqrt(8f * level() + 1f)).toInt() / 2
        curCharges = min(curCharges, maxCharges)
    }

    override fun onZap(attack: Ballistica) {
        super.onZap(attack)
        
        val cell = attack.collisionPos
        GameScene.add(Blob.seed(cell, 12, ConfusionGas::class.java))
    }

    override fun onHit(damage: Damage) {
        super.onHit(damage)

        val ch = damage.to as Char
        Buff.affect(ch, MagicalSleep.Deep::class.java).ratio = 0.75f - 0.5f * 0.85f.pow(level())
        ch.sprite.centerEmitter().start(Speck.factory(Speck.NOTE), 0.3f, 3 + level())
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        val level = max(0, staff.level())
        if (Random.Int(level + 6) >= 4) {
            val to = damage.to as Char
            Buff.affect(to, Vertigo::class.java, Vertigo.duration(to))
        }
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0x1e0303.toInt())
        particle.am = 0.6f
        particle.setLifespan(1f)
        val angle = Random.Float(PointF.PI2)
        particle.speed.polar(angle, 2f)
        particle.acc.set(0f, -10f)
        particle.setSize(1f, 2f)
        particle.radiateXY(Random.Float(2f))
    }
}
