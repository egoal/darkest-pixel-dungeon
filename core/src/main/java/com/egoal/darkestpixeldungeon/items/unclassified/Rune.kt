package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.FlavourBuff
import com.egoal.darkestpixeldungeon.actors.buffs.MoonNight
import com.egoal.darkestpixeldungeon.actors.buffs.SharpVision
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

abstract class Rune : Item() {
    init {
        image = ItemSpriteSheet.RUNE
    }

    abstract fun consume(hero: Hero)

    override fun doPickUp(hero: Hero): Boolean {
        val vial = hero.belongings.getItem(DewVial::class.java)
        if (vial == null) {
            GLog.w(Messages.get(this, "no-vial"))
            return false
        } else if (vial.rune != null) {
            GLog.w(Messages.get(this, "already-has-rune"))
            return false
        } else {
            vial.collectRune(this)

            Sample.INSTANCE.play(Assets.SND_DEWDROP)
            hero.spendAndNext(Item.TIME_TO_PICK_UP)

            return true
        }
    }
}

class RegenerationRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0xa6fd4d)

    override fun consume(hero: Hero) {
        hero.regeneration += 0.1f
    }
}

class MendingRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0x00ff00)

    override fun consume(hero: Hero) {
        Buff.prolong(hero, Recovery::class.java, 100f)
    }

    class Recovery : FlavourBuff()
}

class CriticalRune : Rune() {
    override fun consume(hero: Hero) {
        Buff.prolong(hero, Critical::class.java, 20f)
    }

    class Critical : FlavourBuff()
}

class BrightRune : Rune() {
    override fun consume(hero: Hero) {
        Buff.prolong(hero, SharpVision::class.java, 60f)
        Buff.prolong(hero, MoonNight::class.java, 60f)
    }
}

class HasteRune : Rune() {
    override fun consume(hero: Hero) {
        Buff.prolong(hero, Haste::class.java, 40f)
    }

    class Haste : FlavourBuff()
}

class TreasureRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0xfdd14d)

    override fun consume(hero: Hero) {
        Gold().random().apply {
            quantity = (quantity * Random.Float(1.5f, 3f)).toInt()
        }.collect()
    }
}
