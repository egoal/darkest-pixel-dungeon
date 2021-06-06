package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min

class BoethiahsBlade : MeleeWeapon() {
    init {
        image = ItemSpriteSheet.BOETHIAHS_BLADE
        tier = 3
    }

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) - 1

    override fun proc(dmg: Damage): Damage {
        dmg.addElement(Damage.Element.SHADOW)
        if (dmg.isFeatured(Damage.Feature.CRITICAL))
            dmg.value += (2 + level()) * tier

        val defender = dmg.to as Char
        val dht = if (cursed) {
            level() * 3 / 2 + 1
        } else level() + 1

        defender.HT = max(1, defender.HT - dht)
        defender.HP = min(defender.HP, defender.HT)

        return super.proc(dmg)
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        GLog.n(M.L(this, "unequip_first"))
        return false
    }

    override fun execute(hero: Hero, action: String) {
        if (action == AC_UNEQUIP) {
            WndOptions.Confirm(ItemSprite(this), name, M.L(this, "unequip_warn", name)) {
                unequip(hero)
            }
        } else
            super.execute(hero, action)
    }

    private fun unequip(hero: Hero) {
        if (cursed) {
            GLog.w(M.L(EquipableItem::class.java, "unequip_cursed"))
            return
        }

        hero.spendAndNext(time2equip(hero))

        if (!collect(hero.belongings.backpack)) {
            onDetach()
            Dungeon.quickslot.clearItem(this)
            updateQuickslot()
            Dungeon.level.drop(this, hero.pos)
        }
        hero.belongings.weapon = null

        //todo:
    }
}