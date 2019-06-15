package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KindOfWeapon
import com.egoal.darkestpixeldungeon.items.unclassified.GreatBlueprint
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.sprites.MissileSprite
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton
import com.egoal.darkestpixeldungeon.utils.BArray
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Callback
import com.watabou.utils.PathFinder
import kotlin.math.max
import kotlin.math.sqrt

open class Boomerang : MissileWeapon(1), GreatBlueprint.Enchantable {
    private var ejection = 0
    private var enchanted = false

    init {
        image = ItemSpriteSheet.BOOMERANG

        stackable = false

        unique = true
        bones = false

        DLY = 1f // normal speed
    }
    
    override fun min(lvl: Int): Int = tier + lvl

    override fun max(lvl: Int): Int = 5 * tier + 2 * lvl

    override fun breakChance(): Float = 0f // never break 

    override fun isUpgradable(): Boolean = true

    override fun price(): Int = 0

    override fun upgrade(): Item = super.upgrade(false)

    override fun random(): Item = this

    override fun upgrade(enchant: Boolean): Item {
        super.upgrade(enchant)
        updateQuickslot()
        return this
    }

    override fun proc(dmg: Damage): Damage {
        if (dmg.from is Hero && (dmg.from as Hero).rangedWeapon === this) {
            if (ejection > 0) {
                val ch = findCharToEject(dmg.to as Char)
                if (ch != null) {
                    ejection--
                    eject((dmg.to as Char).pos, ch, dmg.from as Hero)
                } else
                    circleBack((dmg.to as Char).pos, dmg.from as Hero)
            } else
                circleBack((dmg.to as Char).pos, dmg.from as Hero)
        }
        return super.proc(dmg)
    }

    override fun miss(cell: Int) {
        circleBack(cell, Item.curUser!!)
    }

    private fun findCharToEject(ch: Char): Char? {
        val passable = BooleanArray(Level.solid.size){ !Level.solid[it] && !Level.losBlocking[it] }
        PathFinder.buildDistanceMap(ch.pos, passable, 4)
        for (i in 0 until PathFinder.distance.size)
            if (PathFinder.distance[i] < Int.MAX_VALUE) {
                val other = Actor.findChar(i)
                if (other !== ch && other is Mob && other.hostile) return other
            }
        return null
    }

    private fun eject(from: Int, to: Char, owner: Hero) {
        (owner.sprite.parent.recycle(MissileSprite::class.java) as MissileSprite).reset(
                from, to.pos, Item.curItem, Callback {
            // simple shoot
            if(to.isAlive) owner.shoot(to, this)
            else circleBack(to.pos, owner)
        })
    }

    private var throwEquiped = false // this would never be true, now

    private fun circleBack(from: Int, owner: Hero) {
        (owner.sprite.parent.recycle(MissileSprite::class.java) as MissileSprite).reset(from,
                owner.pos, Item.curItem, null)

        if (throwEquiped) {
            owner.belongings.weapon = this
            owner.spend(-KindOfWeapon.TIME_TO_EQUIP)
            Dungeon.quickslot.replaceSimilar(this)
            updateQuickslot()
        } else if (!collect(owner.belongings.backpack)) {
            Dungeon.level.drop(this, owner.pos).sprite.drop()
        }
        
        ejection = if(enchanted) 1 else 0
        owner.next()
    }

    override fun cast(user: Hero, dst: Int) {
        throwEquiped = isEquipped(user) && !cursed
        if (throwEquiped) Dungeon.quickslot.convertToPlaceholder(this)
        
        //fixme: copy from parent, this is a patch just for ejection
        if (isEquipped(user)) {
            if (quantity == 1 && !this.doUnequip(user, false, false)) {
                return
            }
        }
        
        val cell = throwPos(user, dst)
        user.sprite.zap(cell)
        user.busy()
        
        Sample.INSTANCE.play(Assets.SND_MISS, 0.6f, 0.6f, 1.5f)
        val enemy = Actor.findChar(cell)
        QuickSlotButton.target(enemy)
        
        val delay = Item.TIME_TO_THROW* speedFactor(user)
        (user.sprite.parent.recycle(MissileSprite::class.java) as MissileSprite)
                .reset(user.pos, cell, this, Callback { 
                    (this@Boomerang.detach(user.belongings.backpack) as Boomerang).onThrow(cell)
                    user.spend(delay) // spend but not next, next when the boomerang back to hand
                })
    }

    override fun onThrow(cell: Int) {
        super.onThrow(cell)
        Dungeon.hero.next() // patch to next
    }
    
    override fun desc(): String {
        val info = super.desc()
        return when (imbue) {
            Imbue.LIGHT -> info + "\n\n" + Messages.get(Weapon::class.java, "lighter")
            Imbue.HEAVY -> info + "\n\n" + Messages.get(Weapon::class.java, "heavier")
            Imbue.NONE -> info
        }
    }

    override fun enchantByBlueprint(){
        enchanted = true
        ejection = 1
        
        image = ItemSpriteSheet.ENHANCED_BOOMERANG
    }
    
    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(ENCHANTED, enchanted)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        enchanted = bundle.getBoolean(ENCHANTED)
        if (enchanted) enchantByBlueprint()
    }
    
    companion object{
        private const val ENCHANTED = "enchanted"
    }
}