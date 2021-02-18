package com.egoal.darkestpixeldungeon.items.artifacts

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.Fire
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Shock
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.BArray
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.PathFinder
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sqrt

class DragonsSquama : Artifact() {
    init {
        image = ItemSpriteSheet.DARGONS_SQUAMA

        levelCap = 10
        chargeCap = 100
        charge = chargeCap

        defaultAction = AC_TAP
    }

    override fun actions(hero: Hero): ArrayList<String> = super.actions(hero).apply { add(AC_TAP) }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_TAP) {
            if (!isEquipped(hero)) GLog.w(M.L(Artifact::class.java, "need_to_equip"))
            else if (cursed) GLog.w(M.L(this, "cursed"))
            else if (charge < chargeCap) GLog.w(M.L(this, "no_charge"))
            else superNova(hero)
        }
    }

    override fun doEquip(hero: Hero): Boolean {
        val re = super.doEquip(hero)
        if (re) hero.elementalResistance[0] += 0.6f //todo: 0-> fire, this is fragile
        return re
    }

    override fun doUnequip(hero: Hero, collect: Boolean, single: Boolean): Boolean {
        val re = super.doUnequip(hero, collect, single)
        if (re) hero.elementalResistance[0] -= 0.6f
        return re
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isEquipped(Dungeon.hero))
            if (cursed) desc += "\n\n" + M.L(this, "desc_cursed")
            else desc += "\n\n" + M.L(this, "desc_hint")
        return desc
    }

    private fun superNova(hero: Hero) {
        charge = 0
        exp += 1
        val requireExp = 2 + level()// (6f * sqrt(level().toFloat())).toInt() + 3 * level() + 1
        if (exp >= requireExp && level() < levelCap) {
            exp -= requireExp
            upgrade()
            GLog.p(M.L(this, "levelup"))
        }

        hero.sprite.operate(hero.pos)

        val radius = 3 + level() / 3
        PathFinder.buildDistanceMap(hero.pos, BArray.not(Level.solid, null), radius)

        // (0 until PathFinder.distance)
        val affectedCells = PathFinder.distance.indices.filter {
            val dis = PathFinder.distance[it]
            dis < Int.MAX_VALUE
        }

        val last = affectedCells.maxBy { PathFinder.distance[it] }

        for (cell in affectedCells) {
            if (cell != last) MagicMissile.fire(hero.sprite.parent, hero.pos, cell, null)
            else
                MagicMissile.fire(hero.sprite.parent, hero.pos, hero.pos) {
                    // burn the ground
                    for (i in affectedCells)
                        if (i != hero.pos) GameScene.add(Blob.seed(i, 1, Fire::class.java))

                    affectedCells.mapNotNull { Actor.findChar(it) }.forEach { burnChar(it) }
                    hero.spendAndNext(1f)
                }
        }
    }

    private fun burnChar(char: Char) {
        val value = Random.IntRange(2 + 2 * level(), 6 + 5 * level())
        val dmg = Damage(value, Dungeon.hero, char).type(Damage.Type.MAGICAL).addElement(Damage.Element.FIRE)
        char.takeDamage(dmg)
        if (char.isAlive && char !is Hero) {
            Buff.prolong(char, Shock::class.java, 1.5f)
        }
    }

    override fun passiveBuff(): ArtifactBuff = Recharge()

    inner class Recharge : ArtifactBuff() {
        fun procTakenDamage(damage: Damage) {
            if (damage.type == Damage.Type.MENTAL) return

            val isCrit = damage.isFeatured(Damage.Feature.CRITICAL)
            if (isCrit) {
                val block = round(damage.value * (0.12f + 0.01f * level())).toInt()
                damage.value += if (cursed) block else -block
            }

            if (charge < chargeCap && damage.from is Char && damage.from !== Char.Nobody) {
                charge = min(chargeCap, charge + (4 - level() / 5) + if (isCrit) 1 else 0)
                updateQuickslot()
            }
        }

        override fun act(): Boolean {
            if (charge < chargeCap) {
                charge = min(chargeCap, charge + 1)
                updateQuickslot()
            }

            spend(2f)
            return true
        }
    }

    companion object {
        private const val AC_TAP = "tap"
    }
}