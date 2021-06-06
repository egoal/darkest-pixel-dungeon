package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Bundle
import com.watabou.utils.PathFinder
import java.util.ArrayList
import kotlin.math.round

class Scythe : MeleeWeapon() {
    private var form = 2 // 0, 1, 2

    init {
        image = ItemSpriteSheet.SCYTHE + form
        tier = 4

        transform()
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_TRANS) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_TRANS) {
            if (!isIdentified) GLog.w(M.L(this, "identify"))
            else if (cursed) GLog.w(M.L(this, "cursed"))
            else {
                hero.doOperation(1f) { transform() }
            }
        }
    }

    private fun transform() {
        form = (form + 1) % 3
        image = ItemSpriteSheet.SCYTHE + form
        when (form) {
            0 -> {
                DLY = 0.75f
                RCH = 1
            }
            1 -> {
                RCH = 2
            }
            2 -> {
                DLY = 1.5f
                RCH = 2
            }
        }
    }

    // base: 4 ~ 25 [+1, +5]
    // 0:    3 ~ 16 [+1, +4]
    // 1:    4 ~ 20 [+1, +5]
    // 2:    5 ~ 30 [+1, +7.5]
    override fun min(lvl: Int): Int = tier + (form - 1) + lvl

    override fun max(lvl: Int): Int {
        var v = 4 * (tier + form) + lvl * (tier + form)
        if (form == 2) v += 6 + lvl * 3 / 2
        return v
    }

    override fun canSurpriseAttack(): Boolean = form != 2

    override fun STRReq(lvl: Int): Int = super.STRReq(lvl) - 1 + form

    override fun proc(dmg: Damage): Damage {
        if (form == 2) {
            val pos = (dmg.to as Char).pos
            for (i in PathFinder.NEIGHBOURS8) {
                val mob = Dungeon.level.findMobAt(pos + i)
                if (mob != null && mob.camp == Char.Camp.ENEMY)
                    mob.takeDamage(mob.defendDamage(Damage(dmg.value / 2, dmg.from, dmg.to).type(dmg.type)))
            }
        }

        return super.proc(dmg)
    }

    override fun desc(): String {
        return super.desc() + "\n\n" + M.L(this, "desc_$form")
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(FORM, form)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        form = bundle.getInt(FORM)
        form -= 1
        transform()
    }

    companion object {
        private const val AC_TRANS = "trans"
        private const val FORM = "form"
    }
}