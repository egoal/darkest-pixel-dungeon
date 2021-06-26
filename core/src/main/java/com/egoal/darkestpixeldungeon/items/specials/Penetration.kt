package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.utils.Bundle
import com.watabou.utils.Callback

class Penetration : Special() {
    private var hits = 0

    init {
        image = ItemSpriteSheet.PENETRATION

        usesTargeting = true
    }

    fun hit(hero: Hero) {
        if (!hero.isUsingPolearm()) return

        if (hits < HIT_TIMES) {
            hits++
            if (hits == HIT_TIMES) {
                GLog.w(M.L(this, "active"))
                image = ItemSpriteSheet.PENETRATION_RDY
            }
            updateQuickslot()
        }
    }

    override fun status(): String? = if (hits == HIT_TIMES) null else "${HIT_TIMES - hits}"

    override fun use(hero: Hero) {
        if (hits == HIT_TIMES) GameScene.selectCell(selector)
    }

    private fun doStab(enemy: Char) {
        hits = 0
        image = ItemSpriteSheet.PENETRATION
        updateQuickslot()

        val hero = curUser

        // delay invoke
        Buff.prolong(hero, stab::class.java, TIME_PREPARE).enemy = enemy

        // hero.sprite.showStatus(CharSprite.NEUTRAL, M.L(this, "prepare"))
        hero.sprite.operate(enemy.pos)
        Buff.prolong(hero, SeeThrough::class.java, TIME_PREPARE + 0.01f).enemyid = enemy.id()
        hero.spendAndNext(TIME_PREPARE + 0.01f) // or may set priority for stab.
    }

    private val selector = object : CellSelector.Listener {
        override fun onSelect(cell: Int?) {
            if (cell == null || !Dungeon.visible[cell]) return
            val enemy = Actor.findChar(cell)
            if (enemy == null || curUser.isCharmedBy(enemy))
                GLog.w(M.L(Penetration::class.java, "bad_target"))
            else {
                val route = Ballistica(curUser.pos, enemy.pos, Ballistica.PROJECTILE)
                if (route.collisionPos != enemy.pos) GLog.w(M.L(Penetration::class.java, "bad_route"))
                else doStab(enemy)
            }
        }

        override fun prompt(): String = M.L(Penetration::class.java, "prompt")
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(HIT_STR, hits)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        hits = bundle.getInt(HIT_STR)
    }

    class stab : FlavourBuff() {
        lateinit var enemy: Char

        override fun detach() {
            if (enemy.isAlive) {
                val hero = target as Hero

                hero.sprite.showStatus(CharSprite.NEUTRAL, M.L(Penetration::class.java, "name"))

                val route = Ballistica(hero.pos, enemy.pos, Ballistica.PROJECTILE)

                hero.busy()

                val op = enemy.pos + (enemy.pos - hero.pos)
                val knock_shot = Ballistica(enemy.pos, op, Ballistica.MAGIC_BOLT)

                val throwdis = WandOfBlastWave.calcThrowDistance(enemy, knock_shot, 1)

                val dst = if (throwdis == 0)
                    if (route.dist > 0) route.path[route.dist - 1] else hero.pos
                else enemy.pos

                Actor.addDelayed(Pushing(hero, hero.pos, dst, Callback {
                    // knock back
                    if (throwdis > 0) WandOfBlastWave.throwChar(enemy, knock_shot, 1)

                    val distance = Dungeon.level.distance(hero.pos, dst)
                    landHero(hero, dst)

                    // simple attack
                    hero.attack(enemy)
                    if (enemy.isAlive && distance <= 2) {
                        prolong(enemy, Unbalance::class.java, 2f)
                    }

                    hero.spendAndNext(TIME_STAB)

                    Camera.main.shake(2f, 0.5f)
                }), -1f)
            }

            super.detach()
        }

        private fun landHero(hero: Hero, dst: Int) {
            hero.move(dst)
            Dungeon.level.press(dst, hero)
            Dungeon.observe()
            GameScene.updateFog()
        }
    }

    companion object {
        private const val HIT_TIMES = 4
        private const val HIT_STR = "hit"

        private const val TIME_PREPARE = 1f
        private const val TIME_STAB = 1f
    }
}