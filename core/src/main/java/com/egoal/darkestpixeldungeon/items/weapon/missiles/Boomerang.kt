package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KindOfWeapon
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.sprites.MissileSprite
import kotlin.math.max
import kotlin.math.sqrt

open class Boomerang : MissileWeapon(1) {
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
        if (dmg.from is Hero && (dmg.from as Hero).rangedWeapon === this)
            circleBack((dmg.to as Char).pos, dmg.from as Hero)
        return super.proc(dmg)
    }

    override fun miss(cell: Int) {
        circleBack(cell, Item.curUser!!)
    }

    private var throwEquiped = false

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
    }

    override fun cast(user: Hero, dst: Int) {
        throwEquiped = isEquipped(user) && !cursed
        if (throwEquiped) Dungeon.quickslot.convertToPlaceholder(this)
        super.cast(user, dst)
    }

    override fun desc(): String {
        var info = super.desc()
        return when (imbue) {
            Imbue.LIGHT -> info + "\n\n" + Messages.get(Weapon::class.java, "lighter")
            Imbue.HEAVY -> info + "\n\n" + Messages.get(Weapon::class.java, "heavier")
            Imbue.NONE -> info
        }
    }

    ///
    class Enhanced : Boomerang() {
        init {
            image = ItemSpriteSheet.ENHANCED_BOOMERANG
        }
    }
}