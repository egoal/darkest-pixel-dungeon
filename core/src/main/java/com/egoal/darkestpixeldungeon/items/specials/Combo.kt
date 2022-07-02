package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelectListener
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndActionList
import com.watabou.noosa.Camera
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class Combo : Special() {
    init {
        image = ItemSpriteSheet.NULLWARN
    }

    private var comboTime = 0
    private var count = 0
    private var adrenaline = 0
    private var focusId = 0
    private var focusCount = 0

    val AttackSpeedFactor get() = 0.2f + .8f * .8f.pow(focusCount)

    private val actions = listOf(Endurance(), Kick(), Dash(), Crush())

    override fun tick() {
        if (comboTime > 0) --comboTime
        else {
            count = 0
            adrenaline = max(0, adrenaline - 1)
        }

        for (a in actions) a.cd = max(0, a.cd - 1)

        //todo: update image?
        updateQuickslot()
    }

    fun hit(target: Char) {
        comboTime = 5
        adrenaline = min(adrenaline + 3, 20)
        ++count

        if (count >= 5) {
            Badges.validateMasteryCombo(count)
            GLog.p(M.L(this, "combo", count))
        }

        if (target.id() != focusId) {
            focusId = target.id()
            focusCount = 1
        } else ++focusCount
    }

    override fun use(hero: Hero) {
        GameScene.show(WndActionList(ItemSprite(image, null), name, actions))
    }

    override fun status(): String? = if (adrenaline > 0) "$adrenaline" else null

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(TIME, comboTime)
        bundle.put(COUNT, count)
        bundle.put(ADRENALINE, adrenaline)
        bundle.put(FOCUS_ID, focusId)
        bundle.put(FOCUS_COUNT, focusCount)

        bundle.put("cds", actions.map { it.cd }.toIntArray())
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        comboTime = bundle.getInt(TIME)
        count = bundle.getInt(COUNT)
        adrenaline = bundle.getInt(ADRENALINE)
        focusId = bundle.getInt(FOCUS_ID)
        focusCount = bundle.getInt(FOCUS_COUNT)

        val cds = bundle.getIntArray("cds")
        (0 until min(actions.size, cds.size)).forEach { actions[it].cd = cds[it] }
    }

    companion object {
        private const val COUNT = "count"
        private const val TIME = "combotime"
        private const val ADRENALINE = "adrenaline"
        private const val FOCUS_ID = "focusid"
        private const val FOCUS_COUNT = "focus-count"
    }

    abstract inner class ComboAction(val cost: Int, val cooldown: Int) : WndActionList.Action() {
        var cd = 0

        override fun Name(): String = M.L(this, "name") + "($cost)"
        override fun Info(): String = M.L(this, "info", cost, cooldown)
        override fun Disabled(): Boolean = cost > adrenaline || cd > 0

        override fun Execute() {
            Execute(Dungeon.hero)
            updateQuickslot()
        }

        protected abstract fun Execute(hero: Hero)
    }

    inner class Endurance : ComboAction(5, 10) {
        override fun Execute(hero: Hero) {
            cd = cooldown
            adrenaline -= cost
            hero.apply {
                SHLD += Random.NormalIntRange(10, hero.HT / 3)

                spend(1f)
                busy()
                sprite.operate(pos)
            }
        }
    }

    inner class Kick : ComboAction(8, 3) {
        override fun Execute(hero: Hero) {
            GameScene.selectCell(object : CellSelectListener(FLAG_ENEMY, 1) {
                override fun onSelected(cell: Int) {
                    cd = cooldown
                    adrenaline -= cost

                    hero.sprite.attack(cell) {
                        val enemy = Actor.findChar(cell)!!
                        Char.ProcessAttackDamage(hero.giveDamage(enemy).addFeature(Damage.Feature.CRITICAL or Damage.Feature.ACCURATE))
                        if (enemy.isAlive && !enemy.properties().contains(Char.Property.IMMOVABLE)) {
                            val opposite = enemy.pos + (enemy.pos - hero.pos)
                            WandOfBlastWave.throwChar(enemy, Ballistica(enemy.pos, opposite, Ballistica.MAGIC_BOLT), 3)
                            Buff.prolong(enemy, Vertigo::class.java, Random.NormalIntRange(1, 4).toFloat())
                        }

                        hero.spendAndNext(hero.attackDelay())
                    }
                }
            })
        }
    }

    inner class Dash : ComboAction(12, 5) {
        override fun Execute(hero: Hero) {
            GameScene.selectCell(object : CellSelectListener(FLAG_CELL, 8, false) {
                override fun onSelected(cell: Int) {
                    doDash(hero, cell)
                }
            })
        }

        private fun doDash(hero: Hero, cell: Int) {
            if (cell == hero.pos) {
                GLog.w(M.L(this, "not_yourself"))
                return
            }

            cd = cooldown
            adrenaline -= cost

            val route = Ballistica(hero.pos, cell, Ballistica.PROJECTILE)
            Actor.addDelayed(Pushing(hero, hero.pos, cell) {
                //todo: throw & damage all n8 chars
                for (i in 1 until route.dist + 1) {
                    val p = route.path[i]
                    WandOfBlastWave.BlastWave.blast(p)

                    Actor.findChar(p)?.let {
                        val damage = hero.giveDamage(it)
                        it.takeDamage(it.defendDamage(damage))
                        if (it.isAlive) Buff.prolong(it, Cripple::class.java, 3f)
                        it.sprite.flash()
                    }
                }
                Actor.findChar(cell)?.let {
                    if (it.isAlive && route.path.size > route.dist + 1) {
                        val traj = Ballistica(it.pos, route.path[route.dist + 1], Ballistica.MAGIC_BOLT)
                        WandOfBlastWave.throwChar(it, traj, 3)
                    }
                }

                //land
                hero.move(cell)
                Dungeon.level.press(cell, hero)
                Dungeon.observe()
                GameScene.updateFog()

                hero.spendAndNext(1f)
                Camera.main.shake(2f, .5f)
            }, -1f)
            hero.next()
        }
    }

    inner class Crush : ComboAction(15, 2) {
        private var count = 0
        override fun Execute(hero: Hero) {
            GameScene.selectCell(object : CellSelectListener(FLAG_ENEMY, hero.belongings.weapon?.reachFactor(hero)
                    ?: 1) {
                override fun onSelected(cell: Int) {
                    cd = cooldown
                    adrenaline -= cost

                    count = 10 // at most 10 times
                    doAttack(hero, Actor.findChar(cell)!!)
                }
            })
        }

        private fun doAttack(hero: Hero, enemy: Char) {
            hero.sprite.attack(enemy.pos) {
                --count
                val dmg = hero.giveDamage(enemy).apply {
                    value = max(1, value * 3 / 5)
                    addFeature(Damage.Feature.CRITICAL or Damage.Feature.ACCURATE or Damage.Feature.PURE)
                }
                Char.ProcessAttackDamage(dmg)
                if (count > 0 && enemy.isAlive) doAttack(hero, enemy)
                else hero.spendAndNext(hero.attackDelay())
            }
        }
    }
}