package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

class GuardHelmet : Helmet() {
    init {
        image = ItemSpriteSheet.HELMET_GUARD
    }

    override fun doEquip(hero: Hero): Boolean {
        return if (super.doEquip(hero)) {
            attach(hero)
            true
        } else false
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        return if (super.doUnequip(hero, collect, single)) {
            detach(hero)
            true
        } else false
    }

    override fun uncurse() {
        super.uncurse()
        if (!cursed) return

        // redo the modifications
        //fixme:
        detach(Dungeon.hero)
        cursed = false
        attach(Dungeon.hero)
    }

    private fun detach(hero: Hero) {
        if (cursed) {
            hero.magicalResistance += 0.09f
            for (i in 0 until hero.elementalResistance.size) hero.elementalResistance[i] += 0.06f
        } else hero.MSHLD -= SHLD
    }

    private fun attach(hero: Hero) {
        if (cursed) {
            hero.magicalResistance += -0.09f
            for (i in 0 until hero.elementalResistance.size) hero.elementalResistance[i] += -0.06f
        } else
            hero.MSHLD += SHLD
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isIdentified) {
            desc += "\n\n" + M.L(this, "effect-desc")
            if (cursed)
                desc += "\n\n" + M.L(this, "cursed-desc")
        }

        return desc
    }

    companion object {
        private const val SHLD = 5
    }
}