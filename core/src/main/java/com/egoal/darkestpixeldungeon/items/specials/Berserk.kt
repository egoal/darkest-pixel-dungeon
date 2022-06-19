package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.Camera
import com.watabou.utils.Random
import kotlin.math.round

class Berserk : Special() {
    init {
        image = ItemSpriteSheet.NULLWARN

        usesTargeting = true
    }

    private var warned = false // warn once.

    private val selector = object : CellSelector.Listener {
        override fun onSelect(cell: Int?) {
            if (cell == null || !Dungeon.visible[cell]) return
            Actor.findChar(cell)?.let {
                if (it !== Dungeon.hero && Dungeon.hero.canAttack(it))
                    attack(Dungeon.hero, it)
                else GLog.w(M.L(Berserk::class.java, "invalid_target"))
            }
        }

        override fun prompt(): String = M.L(Berserk::class.java, "prompt")
    }

    override fun use(hero: Hero) {
        if (!warned && hero.HP < hero.HT * 3 / 10) {
            warned = true
            WndOptions.Confirm(ItemSprite(this), M.L(this, "name"), M.L(this, "warn")) {
                GameScene.selectCell(selector)
            }
        } else GameScene.selectCell(selector)
    }

    private fun attack(hero: Hero, char: Char) {
        val sac = round(hero.HT * .15f).toInt()
        hero.takeDamage(Damage(sac, hero, hero).type(Damage.Type.MAGICAL))
//        hero.sprite.bloodBurstB(char.sprite.center(), 10) // we only have one instance to splash...
        Wound.hit(hero.pos)

        Char.ProcessAttackDamage(hero.giveDamage(char).apply {
            value += Random.Int(value / 2, value) // extra crit?
            addFeature(Damage.Feature.ACCURATE or Damage.Feature.CRITICAL)
        })

        Camera.main.shake(2f, .3f)
    }
}