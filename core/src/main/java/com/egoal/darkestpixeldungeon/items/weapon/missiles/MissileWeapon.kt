package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.PinCushion
import com.egoal.darkestpixeldungeon.actors.buffs.Unbalance
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.actors.hero.perks.ExplodeBrokenShot
import com.egoal.darkestpixeldungeon.actors.hero.perks.RangedShot
import com.egoal.darkestpixeldungeon.items.EquipableItem
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.rings.Ring
import com.egoal.darkestpixeldungeon.items.rings.RingOfSharpshooting
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.inscriptions.Projecting
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

abstract class MissileWeapon(val tier: Int, protected val stick: Boolean = false) : Weapon() {
    init {
        stackable = true
        levelKnown = true

        defaultAction = Item.AC_THROW
        usesTargeting = true

        DLY = 0.4f + 0.3f * tier
    }

    // lvl: 0 
    // 1: 1~ 5
    // 2: 2~ 10
    override fun min(lvl: Int): Int = 1 + tier / 2

    override fun max(lvl: Int): Int = 4 + tier * 2

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { remove(AC_EQUIP) }

    override fun throwPos(user: Hero, dst: Int): Int {
        if (isInscribed(Projecting::class.java) &&
                !Level.solid[dst] && Dungeon.level.distance(user.pos, dst) <= 4)
            return dst

        return super.throwPos(user, dst)
    }

    override fun onThrow(cell: Int) {
        val enemy = Actor.findChar(cell)
        if (enemy == null || enemy == Item.curUser) {
            miss(cell)
        } else {
            // shoot others
            if (Dungeon.level.adjacent(Item.curUser.pos, enemy.pos)) {
                //todo: fixme
                Item.curUser.rangedWeapon = this
                val f = if (Item.curUser.heroClass == HeroClass.HUNTRESS) 0.75f else 1.5f
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
                } else if (Item.curUser.heroPerk.has(ExplodeBrokenShot::class.java)) {
                    // explode shot
                    val extra = strCorrection(Item.curUser) + Dungeon.depth
                    for (i in PathFinder.NEIGHBOURS9) {
                        Actor.findChar(i + cell)?.let {
                            if (it.isAlive)
                                it.takeDamage(Damage(Random.IntRange(min(level()), max(level())) + extra, Item.curUser, it)
                                        .type(Damage.Type.MAGICAL).addElement(Damage.Element.FIRE))
                        }
                    }

                    if (Dungeon.visible[cell]) Sample.INSTANCE.play(Assets.SND_BONES)
                }
            }
        }
    }

    protected open fun miss(cell: Int) {
        val bonus = Ring.getBonus(Item.curUser, RingOfSharpshooting.Aim::class.java)

        // degraded ring of sharpshooting will even make missed shots break.
        if (this is Boomerang || Random.Float() < Math.pow(0.7, -bonus.toDouble()))
            super.onThrow(cell)
    }

    protected open fun breakChance(): Float {
        var bc = 0.65f - 1.25f.pow(tier) / 6f // base
        if (Dungeon.hero.heroClass == HeroClass.HUNTRESS) bc *= 0.7f
        return bc
    }

    override fun proc(dmg: Damage): Damage {
        val hero = dmg.from as Hero
        // remove self 
        if (hero.rangedWeapon == null && stackable) {
            if (quantity == 1) doUnequip(hero, false, false)
            else detach(hero.belongings.backpack) //! null bag place holder.
        }

        return super.proc(dmg)
    }

    override fun accuracyFactor(hero: Hero, target: Char): Float {
        var f = super.accuracyFactor(hero, target)
        if (Dungeon.level.adjacent(hero.pos, target.pos))
            f *= 0.4f
        if (hero.subClass == HeroSubClass.SNIPER)
            f *= 1.25f

        return f
    }

    override fun giveDamage(hero: Hero, target: Char): Damage {
        var value = Random.NormalIntRange(min(), max())

        // extra str
        val strc = strCorrection(hero)
        if (strc > 0) value += Random.Int(1, strc)

        value = imbue.damageFactor(value)
        val dmg = Damage(value, hero, target).addFeature(Damage.Feature.RANGED)

        val bonus = Ring.getBonus(hero, RingOfSharpshooting.Aim::class.java)
        if (bonus != 0) {
            val ratio = 2.5f - 1.5f * 0.9f.pow(bonus)
            dmg.value = round(dmg.value * ratio).toInt()
        }
        hero.heroPerk.get(RangedShot::class.java)?.affectDamage(dmg)

        return dmg
    }

    override fun random(): Item = this.apply {
        quantity = Random.Int(6 - tier, 16 - tier * 2)
    }

    override val isUpgradable: Boolean
        get() = false
    override val isIdentified: Boolean
        get() = true

    final override fun STRReq(lvl: Int): Int = 10

    override fun price(): Int = unitPrice() * quantity

    protected open fun unitPrice(): Int = tier * 3 - 1

    protected open fun strCorrection(hero: Hero): Int {
        val dstr = hero.STR() - STRReq()
        if (dstr <= 0) return 0

        val fix = sqrt(2f * tier - 1f) * (if (hero.subClass == HeroSubClass.SNIPER) 1.5f else 1f)

        return round(dstr * fix).toInt()
    }

    override fun info(): String {
        var info = desc()

        info += "\n\n" + M.L(MissileWeapon::class.java, "stats", tier,
                imbue.damageFactor(min()), imbue.damageFactor(max()), "${Math.round(breakChance() * 100f)}")

        if (Dungeon.hero.STR() > STRReq())
            info += " " + M.L(MissileWeapon::class.java, "excess_str", strCorrection(Dungeon.hero))

        if (inscription != null && (cursedKnown || !inscription!!.curse)) {
            info += "\n\n" + M.L(Weapon::class.java, "inscribed", inscription!!.name())
            info += " " + M.L(inscription!!, "desc")
        }

        if (enchantment != null) {
            info += "\n\n" + M.L(Weapon::class.java, "enchanted", enchantment!!.name())
            info += " " + enchantment!!.desc()
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