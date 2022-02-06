package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.armor.Armor
import com.egoal.darkestpixeldungeon.items.weapon.Inscription
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample
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

        val ht = defender.HT
        defender.HT = max(1, defender.HT - dht)
        defender.HP = min(defender.HP, defender.HT)

        val attacker = dmg.from as Char
        attacker.SHLD += ht - defender.HT

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

    override fun doPickUp(hero: Hero): Boolean {
        GLog.n(M.L(this, "cannot_pickup"))
        return false
    }

    private fun unequip(hero: Hero) {
        if (cursed) {
            GLog.w(M.L(EquipableItem::class.java, "unequip_cursed"))
            return
        }

        hero.spendAndNext(time2equip(hero))

        // drop
        onDetach()
        Dungeon.quickslot.clearItem(this)
        updateQuickslot()
        Dungeon.level.drop(this, hero.pos)

        hero.belongings.weapon = null

        // curse an item
        val equipToCurse = hero.belongings.equippedItems().filter { it != null && !it.cursed }
        if (equipToCurse.isEmpty()) {
            // nothing to curse
            hero.regeneration -= 0.2f
            Wound.hit(hero)
        } else {
            val item = equipToCurse.random() as Item
            item.cursed = true
            item.cursedKnown = true

            if (item is Weapon) item.inscribe(Inscription.randomNegative())
            else if (item is Armor) item.glyph = Armor.Glyph.randomCurse()

            CellEmitter.get(hero.pos).burst(ShadowParticle.CURSE, 5)
            Sample.INSTANCE.play(Assets.SND_BURNING)
        }
    }
}