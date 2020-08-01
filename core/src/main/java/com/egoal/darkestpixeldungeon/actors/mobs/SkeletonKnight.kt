package com.egoal.darkestpixeldungeon.actors.mobs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.PropertyConfiger
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.artifacts.HandOfTheElder
import com.egoal.darkestpixeldungeon.items.food.BrownAle
import com.egoal.darkestpixeldungeon.items.food.Wine
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.SkeletonKnightSprite
import com.watabou.utils.Bundle
import com.watabou.utils.Random

/**
 * Created by 93942 on 5/13/2018.
 */

class SkeletonKnight : Mob() {
    private var combocd = COMBO_COOLDOWN

    init {
        spriteClass = SkeletonKnightSprite::class.java

        PropertyConfiger.set(this, "SkeletonKnight")
        loot = if (Random.Float() < 0.5f) Wine() else BrownAle()
    }

    override fun act(): Boolean {
        combocd -= 1
        return super.act()
    }

    override fun giveDamage(target: Char): Damage = super.giveDamage(target).addElement(Damage.Element.SHADOW)

    private fun canCounter(): Boolean {
        return buff(Paralysis::class.java) == null
    }

    override fun defenseProc(damage: Damage): Damage {
        val enemy = damage.from as Char?
        if (damage.type == Damage.Type.MAGICAL ||
                damage.isFeatured(Damage.Feature.RANGED or Damage.Feature.ACCURATE) ||
                enemy == null || !Dungeon.level.adjacent(pos, enemy.pos) || !canCounter())
            return super.defenseProc(damage)

        if (Random.Float() < COUNTER) {
            sprite.showStatus(CharSprite.WARNING, Messages.get(this, "counter"))
            enemy.takeDamage(enemy.defendDamage(giveDamage(enemy)))

            damage.value = 0
        }
        return super.defenseProc(damage)
    }

    override fun attack(enemy: Char): Boolean {
        if (combocd <= 0 && Random.Float() < COMBO) {
            combocd = COMBO_COOLDOWN

            spend(-cooldown() * .99f)
            sprite.showStatus(CharSprite.WARNING, Messages.get(this, "combo"))
        }

        return super.attack(enemy)
    }

    override fun createLoot(): Item? {
        if (!Dungeon.limitedDrops.handOfElder.dropped() && Random.Float() < 0.15f) {
            Dungeon.limitedDrops.handOfElder.drop()
            return HandOfTheElder().random()
        }
        return super.createLoot()
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(COOLDOWN_COMBO, combocd)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        combocd = bundle.getInt(COOLDOWN_COMBO)
    }

    companion object {

        private const val COUNTER = .175f
        private const val COMBO = .175f

        private const val COMBO_COOLDOWN = 2

        private const val COOLDOWN_COMBO = "cooldown_combo"
    }
}
