package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Chill
import com.egoal.darkestpixeldungeon.actors.buffs.FlavourBuff
import com.egoal.darkestpixeldungeon.actors.buffs.Frost
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.PointF
import com.watabou.utils.Random
import kotlin.math.round

class WandOfFrost : DamageWand() {
    init {
        image = ItemSpriteSheet.WAND_FROST
    }

    override fun min(lvl: Int): Int = 2 + lvl

    override fun max(lvl: Int): Int = 8 + 5 * lvl

    override fun onZap(attack: Ballistica) {
        Dungeon.level.heaps.get(attack.collisionPos)?.freeze()

        Actor.findChar(attack.collisionPos)?.let { ch ->
            var dmg = damageRoll()

            // nothing to do wit a frozen target
            if (ch.buff(Frost::class.java) != null) return

            if (ch.buff(Chill::class.java) != null) {
                val chill = ch.buff(Chill::class.java).cooldown()
                dmg = round(dmg * Math.pow(0.95, chill.toDouble())).toInt()
            } else
                ch.sprite.burst(0xff99ccff.toInt(), level() / 2 + 2)

            ch.takeDamage(Damage(dmg, Item.curUser, ch).type(Damage.Type.MAGICAL).addElement(Damage.Element.ICE))

            if (ch.isAlive) {
                val duration = if (Level.water[ch.pos]) 4 + level() else 2 + level()
                Buff.prolong(ch, Chill::class.java, duration.toFloat())
            }
        }
    }

    override fun fx(bolt: Ballistica, callback: Callback) {
        MagicMissile.blueLight(Item.curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        val defender = damage.to as Char
        val chill = defender.buff(Chill::class.java)
        if (chill != null && Random.IntRange(2, 10) > chill.cooldown()) {
            // delay to no broken by the current staff attack
            object : FlavourBuff() {
                init {
                    actPriority = Int.MIN_VALUE
                }

                override fun act(): Boolean {
                    affect(target, Frost::class.java, Frost.duration(target) * Random.Float(1f, 2f))
                    return super.act()
                }
            }.attachTo(defender)
        }
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0x88CCFF)
        particle.am = 0.6f
        particle.setLifespan(1.5f)
        val angle = Random.Float(PointF.PI2)
        particle.speed.polar(angle, 2f)
        particle.acc.set(0f, 1f)
        particle.setSize(0f, 1.5f)
        particle.radiateXY(Random.Float(2f))
    }
}