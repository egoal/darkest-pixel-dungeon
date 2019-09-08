package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Burning
import com.egoal.darkestpixeldungeon.actors.buffs.Ooze
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.CharSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.GameMath
import java.util.ArrayList
import kotlin.math.min

class DewVial : Item() {

    init {
        image = ItemSpriteSheet.VIAL

        defaultAction = AC_DRINK
        unique = true
    }

    private var volume = 0
    var rune: Rune? = null

    var Volume: Int
        get() = volume
        set(value) {
            volume = GameMath.clamp(value, 0, MAX_VOLUME)
        }

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)

        if (rune != null) actions.add(AC_CONSUME)
        else if (volume > 0) {
            actions.add(AC_DRINK)
            actions.add(AC_SIP)
            if (volume >= WASH_COST)
                actions.add(AC_WASH)
        }

        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        when (action) {
            AC_DRINK -> {
                if (rune != null) {
                    GLog.w(Messages.get(this, "has-rune"))
                } else if (volume > 0) {
                    val require = (hero.HT - hero.HP) / dhp(hero)
                    val drink = Math.min(volume, require)

                    consume(drink, hero)
                } else GLog.w(Messages.get(this, "empty"))
            }
            AC_SIP -> {
                if (volume > 0) {
                    val require = (hero.HT - hero.HP) / dhp(hero)
                    val drink = minOf(volume, 5, require)

                    consume(drink, hero)
                }
            }
            AC_WASH -> {
                if (volume >= WASH_COST) {
                    Buff.detach(curUser, Ooze::class.java)
                    Buff.detach(curUser, Burning::class.java)

                    volume -= WASH_COST
                    with(curUser) {
                        sprite.showStatus(CharSprite.POSITIVE, Messages.get(DewVial::class.java, "ac_wash"))
                        spend(TIME_TO_WASH)
                        busy()
                        sprite.operate(pos)
                    }

                    updateQuickslot()
                }
            }
            AC_CONSUME -> {
                GLog.i(Messages.get(this, "use-rune", rune!!.name()))
                rune!!.consume(curUser!!)
                rune = null

                Sample.INSTANCE.play(Assets.SND_DRINK)

                updateQuickslot()
            }
        }
    }

    override fun desc(): String {
        var desc = super.desc()
        if (rune != null)
            desc += "\n\n" + Messages.get(this, "desc-rune", rune!!.name()) + rune!!.desc()

        return desc
    }

    private fun dhp(hero: Hero): Int = ((if (hero.heroClass == HeroClass.SORCERESS) 0.05f else 0.03f) * hero.HT).toInt() + 1

    private fun consume(vol: Int, hero: Hero) {
        volume -= vol

        val effect = min(hero.HT - hero.HP, vol * dhp(hero))
        with(hero) {
            HP += effect
            sprite.emitter().burst(Speck.factory(Speck.HEALING), if (vol > 5) 2 else 1)
            sprite.showStatus(CharSprite.POSITIVE, Messages.get(DewVial::class.java, "value", effect))

            spend(TIME_TO_DRINK)
            sprite.operate(pos)
            busy()
        }

        Sample.INSTANCE.play(Assets.SND_DRINK)

        updateQuickslot()
    }

    fun empty() {
        volume = 0
        updateQuickslot()
    }

    override fun isUpgradable(): Boolean = false
    override fun isIdentified(): Boolean = true

    override fun glowing(): ItemSprite.Glowing? = rune?.glowing()

    val full: Boolean get() = volume >= MAX_VOLUME

    fun collectDew(dew: Dewdrop) {
        Volume += dew.quantity()
        if (Volume > MAX_VOLUME) Volume = MAX_VOLUME

        GLog.i(Messages.get(this, "collected", dew.quantity()))
        if (full)
            GLog.p(Messages.get(this, "full"))
        updateQuickslot()
    }

    fun collectRune(rune: Rune) {
        this.rune = rune
        GLog.w(Messages.get(this, "collect-rune", rune.name()))

        updateQuickslot()
    }

    fun fill() {
        volume = MAX_VOLUME
        updateQuickslot()
    }

    override fun status(): String = "$volume/$MAX_VOLUME"

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(VOLMUE, volume)
        bundle.put(RUNE, rune)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        volume = bundle.getInt(VOLMUE)
        rune = bundle.get(RUNE) as Rune?
    }

    companion object {
        private const val AC_CONSUME = "consume"
        private const val AC_DRINK = "drink"
        private const val AC_SIP = "sip"
        private const val AC_WASH = "wash"

        private const val TIME_TO_DRINK = 1f
        private const val TIME_TO_WASH = 1f

        private const val WASH_COST = 5
        private const val MAX_VOLUME = 30

        private const val VOLMUE = "volume"
        private const val RUNE = "rune"
    }

}