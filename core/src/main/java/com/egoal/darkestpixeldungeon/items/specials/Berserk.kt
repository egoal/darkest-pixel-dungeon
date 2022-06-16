package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Wound
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.Camera
import com.watabou.noosa.audio.Sample

class Berserk : Special() {
    init {
        image = ItemSpriteSheet.NULLWARN

        usesTargeting = true
    }

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
        GameScene.selectCell(selector)
    }

    private fun attack(hero: Hero, char: Char) {
        val sac = hero.HT * 3 / 20
        hero.takeDamage(Damage(sac, hero, hero).type(Damage.Type.MAGICAL))
        char.takeDamage(char.defendDamage(hero.giveDamage(char)).apply {
            value *= 2
            addFeature(Damage.Feature.ACCURATE or Damage.Feature.CRITICAL)
        })

        hero.sprite.spriteBurst(hero.sprite.center(), 10)
        char.sprite.spriteBurst(char.sprite.center(), 10)
        Sample.INSTANCE.play(Assets.SND_CRITICAL, 1f, 1f, 1f)
        Wound.hit(char.pos)
        Camera.main.shake(2f, .3f)
    }
}