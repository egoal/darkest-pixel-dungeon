package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet

open class Salt(n: Int = 1) : MissileWeapon(1) {
    init {
        image = ItemSpriteSheet.SALT

        quantity = n
    }

    override fun breakChance(): Float = 1f // always

    override fun miss(cell: Int) {
        // super.miss(cell)
        // do nothing, it just, disappear...
    }

    override fun giveDamage(hero: Hero, target: Char): Damage = super.giveDamage(hero, target).type(Damage.Type.MAGICAL)
}

class RefinedSalt(n: Int = 1) : MissileWeapon(3) {
    init {
        image = ItemSpriteSheet.SALT_2

        quantity = n
    }

    override fun breakChance(): Float = 1f

    override fun miss(cell: Int) {}

    override fun giveDamage(hero: Hero, target: Char): Damage = super.giveDamage(hero, target).type(Damage.Type.MAGICAL)
}