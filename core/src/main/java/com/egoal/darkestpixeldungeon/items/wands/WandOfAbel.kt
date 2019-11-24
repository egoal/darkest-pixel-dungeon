package com.egoal.darkestpixeldungeon.items.wands

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.items.Heap
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Stunning
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Callback

class WandOfAbel : DamageWand() {
    init {
        image = ItemSpriteSheet.WAND_SWAP

        collisionProperties = Ballistica.STOP_TARGET or Ballistica.STOP_TERRAIN
    }

    override fun min(lvl: Int): Int = 2 + lvl

    override fun max(lvl: Int): Int = 8 + lvl * 5 / 2

    override fun giveDamage(enemy: Char): Damage = super.giveDamage(enemy).addElement(Damage.Element.SHADOW)

    override fun onZap(bolt: Ballistica) {
        // swap with char
        val ch = Actor.findChar(bolt.collisionPos)
        if (ch != null) {
            // swap
            if (!ch.rooted && !Dungeon.hero.rooted && !ch.properties().contains(Char.Property.IMMOVABLE)) {
                swap(ch, Dungeon.hero)
                Dungeon.observe()
            } else GLog.w(M.L(this, "cannot_swap"))

            val dmg = giveDamage(ch)
            onMissileHit(ch, Item.curUser, dmg)
            ch.takeDamage(dmg)

            if (ch.isAlive)
                Buff.prolong(ch, Paralysis::class.java, 0.5f + level() / 6f)

            return
        }

        // swap with heap
        val heap = Dungeon.level.heaps.get(bolt.collisionPos)
        if (heap != null && !heap.empty()) {
            val heroPos = Item.curUser.pos
            moveChar(Item.curUser, bolt.collisionPos)

            val newHeap = Heap().apply {
                type = heap.type
                drop(heap.pickUp())
                pos = heroPos
            }
            Dungeon.level.heaps.put(newHeap.pos, newHeap)
            GameScene.add(newHeap)

            return
        }
    }

    override fun fx(bolt: Ballistica, callback: Callback) {
        MagicMissile.coldLight(Item.curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback)
        Sample.INSTANCE.play(Assets.SND_ZAP)
    }

    override fun onHit(staff: MagesStaff, damage: Damage) {
        Stunning().proc(staff, damage)
    }

    override fun staffFx(particle: MagesStaff.StaffParticle) {
        particle.color(0x9efaff)
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