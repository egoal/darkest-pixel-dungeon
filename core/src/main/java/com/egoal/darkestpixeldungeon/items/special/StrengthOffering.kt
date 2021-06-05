package com.egoal.darkestpixeldungeon.items.special

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import java.util.ArrayList

class StrengthOffering : Item() {
    init {
        image = ItemSpriteSheet.STRENGTH_OFFERING

        stackable = false
    }

    override val isIdentified: Boolean
        get() = true
    override val isUpgradable: Boolean
        get() = false

    override fun actions(hero: Hero): ArrayList<String> = arrayListOf(AC_USE)

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_USE) {
            if (hero.STR < 1) {
                GLog.w(M.L(this, "no_str"))
                return
            }

            with(hero) {
                sprite.operate(pos)

                STR -= 1
                arcaneFactor += 0.05f

                spend(1f)
                busy()

                CellEmitter.get(pos).burst(ShadowParticle.UP, 5)
                Sample.INSTANCE.play(Assets.SND_CURSED)
            }

        }
    }

    companion object {
        const val AC_USE = "use"
    }
}