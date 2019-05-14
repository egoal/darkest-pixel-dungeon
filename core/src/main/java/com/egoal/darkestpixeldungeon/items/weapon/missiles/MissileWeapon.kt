package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.PinCushion
import com.egoal.darkestpixeldungeon.actors.buffs.Unbalance
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KindOfWeapon
import com.egoal.darkestpixeldungeon.items.rings.RingOfSharpshooting
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Projecting
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.watabou.utils.Random
import java.util.ArrayList

abstract class MissileWeapon(val tier: Int, protected val stick: Boolean = false) : Weapon() {
    init {
        stackable = true
        levelKnown = true

        defaultAction = Item.AC_THROW
        usesTargeting = true

        DLY = 0.4f + 0.2f * tier
    }

    // lvl: 0 
    // 1: 1~ 5
    // 2: 2~ 10
    override fun min(lvl: Int): Int = tier

    override fun max(lvl: Int): Int = 5 * tier

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { remove(EquipableItem.AC_EQUIP) }

    override fun throwPos(user: Hero, dst: Int): Int {
        if (hasEnchant(Projecting::class.java) &&
                !Level.solid[dst] && Dungeon.level.distance(user.pos, dst) <= 4)
            return dst

        return super.throwPos(user, dst)
    }

    override fun onThrow(cell: Int) {
        val enemy = Actor.findChar(cell)
        if (enemy == null || enemy == Item.curUser) {
            if (this is Boomerang) super.onThrow(cell)
            else miss(cell)
        } else {
            // shoot others
            if (Dungeon.level.adjacent(Item.curUser.pos, enemy.pos)) {
                //todo: fixme
                Item.curUser.rangedWeapon = this
                val f = if (Item.curUser.heroPerk.contain(HeroPerk.Perk.SHOOTER)) 0.75f else 1.5f
                Buff.prolong(Item.curUser, Unbalance::class.java, Item.curUser.attackDelay() * f)

                Item.curUser.rangedWeapon = null
            }

            if (!Item.curUser.shoot(enemy, this)) {
                miss(cell)
            } else if (this !is Boomerang) {
                // chane to not break
                if (Random.Float() > breakChance()) {
                    if (enemy.isAlive && stick) Buff.affect(enemy, PinCushion::class.java).stick(this)
                    else Dungeon.level.drop(this, enemy.pos).sprite.drop()
                }
            }
        }
    }

    protected open fun miss(cell: Int) {
        val bonus = RingOfSharpshooting.getBonus(Item.curUser, RingOfSharpshooting.Aim::class.java)

        // degraded ring of sharpshooting will even make missed shots break.
        if (Random.Float() < Math.pow(0.7, -bonus.toDouble()))
            super.onThrow(cell)
    }

    protected open fun breakChance(): Float {
        val base = Math.pow(0.55, tier / 2.0 + 1.0).toFloat()
        var bonus = RingOfSharpshooting.getBonus(Dungeon.hero, RingOfSharpshooting.Aim::class.java)

        // huntress bonus
        if (Dungeon.hero.heroPerk.contain(HeroPerk.Perk.SHOOTER)) bonus += 3

        return base * Math.pow(0.9, bonus.toDouble()).toFloat()
    }

    override fun proc(dmg: Damage): Damage {
        val hero = dmg.from as Hero
        // remove self 
        if (hero.rangedWeapon == null && stackable) {
            if (quantity == 1) doUnequip(hero, false, false)
            else detach(null)
        }

        return super.proc(dmg)
    }

    override fun accuracyFactor(hero: Hero, target: Char): Float {
        return super.accuracyFactor(hero, target) * if (Dungeon.level.adjacent(hero.pos, target.pos)) 0.5f else 1f
    }

    override fun giveDamage(hero: Hero, target: Char): Damage {
        var value = Random.NormalIntRange(min(), max())

        // extra str
        value += strCorrection(hero)

        value = imbue.damageFactor(value);
        return Damage(value, hero, target).addFeature(Damage.Feature.RANGED)
    }

    override fun random(): Item = this.apply {
        quantity = Random.Int(8 - tier, 18 - tier * 2)
    }

    override fun isUpgradable(): Boolean = false

    override fun isIdentified(): Boolean = true

    final override fun STRReq(lvl: Int): Int = 10

    override fun price(): Int = unitPrice() * quantity

    protected open fun unitPrice(): Int = tier * 3 - 1

    protected open fun strCorrection(hero: Hero): Int {
        val dstr = hero.STR() - STRReq()
        return if (dstr <= 0) 0 else dstr * tier / 2
    }

    override fun info(): String {
        var info = desc()

        info += "\n\n" + M.L(MissileWeapon::class.java, "stats", tier,
                imbue.damageFactor(min()), imbue.damageFactor(max()), "${Math.round(breakChance() * 100f)}")

        if (Dungeon.hero.STR() > STRReq())
            info += " " + M.L(MissileWeapon::class.java, "excess_str", strCorrection(Dungeon.hero))

        if (enchantment != null && (cursedKnown || !enchantment.curse())) {
            info += "\n\n" + M.L(Weapon::class.java, "enchanted", enchantment.name())
            info += " " + M.L(enchantment, "desc")
        }

        if (cursed && isEquipped(Dungeon.hero)) {
            // cannot wear, never be here, for now 
            info += "\n\n" + M.L(Weapon::class.java, "cursed_worn")
        } else if (cursedKnown && cursed)
            info += "\n\n" + M.L(Weapon::class.java, "cursed")

        info += "\n\n" + M.L(MissileWeapon::class.java, "distance")

        return info
    }
}