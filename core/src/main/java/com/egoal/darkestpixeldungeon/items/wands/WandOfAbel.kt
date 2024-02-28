package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback
import com.watabou.utils.Random

class WandOfAbel : DamageWand(isMissile = true) {
    init {
        image = ItemSpriteSheet.WAND_SWAP

        collisionProperties = Ballistica.STOP_TARGET or Ballistica.STOP_TERRAIN
    }

    override fun min(lvl: Int): Int = 3 + lvl

    override fun max(lvl: Int): Int = 9 + lvl * 3

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy)

    override fun onZap(attack: Ballistica) {
        // swap with char
        if (Actor.findChar(attack.collisionPos) != null) {
            super.onZap(attack) // see on Hit
            return
        }
        
        // swap with heap, wont miss
        val heap = Dungeon.level.heaps.get(attack.collisionPos)
        if (heap != null && !heap.empty()) {
            val heroPos = Item.curUser.pos
            moveChar(Item.curUser, attack.collisionPos)

            if (heap.type == Heap.Type.HEAP)
                Dungeon.level.drop(heap.pickUp(), heroPos) // simply switch with the top item...
            else {
                val newHeap = Heap().apply {
                    type = heap.type
                    for (item in heap.items) drop(item)
                    pos = heroPos
                }
                heap.destroy()
                // merge with the current heap
                val theHeap = Dungeon.level.heaps.get(heroPos)
                if (theHeap != null) {
                    for (item in theHeap.items) newHeap.drop(item)
                    theHeap.destroy()
                }

                Dungeon.level.heaps.put(newHeap.pos, newHeap)
                GameScene.add(newHeap)
            }
//            val newHeap = Heap().apply {
//                type = heap.type
//                drop(heap.pickUp())
//                pos = heroPos
//            }
//            Dungeon.level.heaps.put(newHeap.pos, newHeap)
//            GameScene.add(newHeap)

            return
        }
    }

    override fun onHit(damage: Damage) {
        super.onHit(damage)
        
        val ch = damage.to as Char
        // swap
        if (!ch.rooted && !curUser.rooted && !ch.properties().contains(Char.Property.IMMOVABLE)) {
            swap(ch, curUser)
            Dungeon.observe()
        } else GLog.w(M.L(this, "cannot_swap"))

        Buff.prolong(ch, Paralysis::class.java, 0.2f + level() / 5f)
    }

    override fun fx(bolt: Ballistica, callback: Callback) {
        MagicMissile.coldLight(Item.curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
//        Stunning().proc(staff, damage)
        if (Random.Float() < 0.25f) {
            val defender = damage.to as Char
            Buff.prolong(defender, Paralysis::class.java, Random.Float(1f, 2f))
            defender.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12)
        }

    }

    override fun particleColor(): Int = 0x9efaff

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(particleColor())
        particle.am = 0.6f
        particle.setLifespan(0.6f)
        particle.acc.set(0f, 40f)
        particle.setSize(2f, 4f)
        particle.shuffleXY(2f)
    }

    private fun moveChar(ch: Char, pos: Int) {
        ch.sprite.move(ch.pos, pos)
        ch.move(pos)
    }

    private fun swap(ch1: Char, ch2: Char) {
        val pos1 = ch1.pos
        moveChar(ch1, ch2.pos)
        moveChar(ch2, pos1)
    }
}