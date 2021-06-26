package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.roundToInt

class StrengthOffering : Special() {
    private var times = 0

    init {
        image = ItemSpriteSheet.STRENGTH_OFFERING

        defaultAction = ""
    }

    override fun use(hero: Hero) {
        if (hero.STR < 1) {
            GLog.w(M.L(this, "no_str"))
            return
        }

        with(hero) {
            sprite.operate(pos)

            STR -= 1
            arcaneFactor += af()

            spend(1f)
            busy()

            CellEmitter.get(pos).burst(ShadowParticle.UP, 5)
            Sample.INSTANCE.play(Assets.SND_CURSED)
        }
        times++
    }

    private fun af() = max(0.01f, 0.05f - times / 5 * 0.01f)

    override fun desc(): String = M.L(this, "desc", (af() * 100f).roundToInt(), times)

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put("times", times)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        times = bundle.getInt("times")
    }
}