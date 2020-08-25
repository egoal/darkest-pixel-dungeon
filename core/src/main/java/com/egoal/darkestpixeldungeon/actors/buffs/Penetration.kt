package com.egoal.darkestpixeldungeon.actors.buffs

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Pushing
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.ActionIndicator
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.noosa.Image
import com.watabou.utils.Bundle
import com.watabou.utils.Callback

class Penetration : Buff(), ActionIndicator.Action {
    private var cooldown = 0f

    override fun act(): Boolean {
        if (cooldown > 0f) cooldown -= Actor.TICK
        else {
            val hero = target as Hero
            if (hero.rangedWeapon == null &&
                    ((target as Hero).belongings.weapon as Weapon?)?.RCH ?: 1 > 1)
                ActionIndicator.setAction(this)
            else ActionIndicator.clearAction(this)
        }
        spend(Actor.TICK)

        return true
    }

    override fun getIcon(): Image {
        val hero = target as Hero
        // fixme
        val weapon = hero.belongings.weapon as Weapon?
        val icon = if ((weapon?.RCH ?: 1) > 1) ItemSprite(weapon!!.image, null)
        else ItemSprite(Item().apply { image = ItemSpriteSheet.WEAPON_HOLDER })

        icon.tint(0xff102a40.toInt())
        return icon
    }

    override fun doAction() {
        GameScene.selectCell(selector)
    }

    private fun doStab(enemy: Char) {
        ActionIndicator.clearAction(this)
        cooldown = ACTION_COOLDOWN

        val hero = target as Hero

        // delay invoke
        prolong(hero, stab::class.java, TIME_PREPARE).enemy = enemy

        hero.sprite.showStatus(CharSprite.NEUTRAL, M.L(this, "prepare"))
        hero.sprite.operate(enemy.pos)
        hero.spendAndNext(TIME_PREPARE + 0.01f) // or may set priority for stab.
    }

    private val selector = object : CellSelector.Listener {
        override fun onSelect(cell: Int?) {
            if (cell == null) return
            val enemy = Actor.findChar(cell)
            if (enemy == null || target.isCharmedBy(enemy))
                GLog.w(M.L(Penetration::class.java, "bad_target"))
            else {
                val route = Ballistica(target.pos, enemy.pos, Ballistica.PROJECTILE)
                if (route.collisionPos != enemy.pos) GLog.w(M.L(Penetration::class.java, "bad_route"))
                else doStab(enemy)
            }
        }

        override fun prompt(): String = M.L(Penetration::class.java, "prompt")
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(COOLDOWN, cooldown)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        cooldown = bundle.getFloat(COOLDOWN)
    }

    private class stab : FlavourBuff() {
        lateinit var enemy: Char

        override fun detach() {
            if (enemy.isAlive) {
                val hero = target as Hero

                hero.sprite.showStatus(CharSprite.NEUTRAL, M.L(Penetration::class.java, "name"))

                val route = Ballistica(hero.pos, enemy.pos, Ballistica.PROJECTILE)
                val dst = enemy.pos

                hero.busy()
                hero.sprite.jump(hero.pos, dst) {
                    val op = enemy.pos + (enemy.pos - hero.pos)

                    // knock back
                    val shot = Ballistica(enemy.pos, op, Ballistica.MAGIC_BOLT)
                    if (WandOfBlastWave.throwChar(enemy, shot, 1) == 0) {
                        // knock back failed, push back to avoid overlap
                        // fixme: this may let the hero fall to chasm...
                        val newpos = route.path[route.dist - 1]
                        Actor.addDelayed(Pushing(hero, hero.pos, newpos, Callback {
                            landHero(hero, newpos)
                        }), -1f)
                    } else landHero(hero, dst)

                    // normal attack process, may do something special later.
                    hero.attack(enemy)
                    hero.spendAndNext(TIME_STAB)

                    Camera.main.shake(2f, 0.5f)
                }
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
        private const val COOLDOWN = "cooldown"
        private const val ACTION_COOLDOWN = 10f

        private const val TIME_PREPARE = 1f
        private const val TIME_STAB = 1f
    }
}