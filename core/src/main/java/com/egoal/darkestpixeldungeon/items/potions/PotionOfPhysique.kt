package com.egoal.darkestpixeldungeon.items.potions

import android.util.Log
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.utils.Random
import kotlin.math.max

class PotionOfPhysique : Potion() {
    init {
        initials = 12

        bones = true
    }

    override fun apply(hero: Hero) {
        setKnown()
        var value = Random.NormalIntRange(5, 10)
        if (reinforced)
            value = max(value, hero.HT / 10) + 4
        hero.HT += value
        hero.HP += value

        hero.sprite.showStatus(CharSprite.POSITIVE, M.L(this, "msg", value))
        hero.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.2f, 2)
        GLog.p(M.L(this, "stronger"))
    }

    override fun canBeReinforced(): Boolean = true

    override fun price(): Int = if (isKnown) quantity * (if (reinforced) 75 else 50) else super.price()

    override fun doPickUp(hero: Hero): Boolean {
        if (super.doPickUp(hero)) {
            if (!isIdentified && hero.heroClass == HeroClass.SORCERESS) {
                identify()
                hero.sayShort(HeroLines.THIS_IS_IT)
            }
            return true
        }

        return false
    }

    override fun desc(): String {
        var desc = super.desc()
        if (Dungeon.hero?.heroClass == HeroClass.SORCERESS)
            desc += "\n\n" + M.L(this, "real-desc")

        return desc
    }
}