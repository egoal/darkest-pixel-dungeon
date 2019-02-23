package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.DungeonTilemap
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random
import kotlin.math.min

abstract class Rune : Item() {
    init {
        image = ItemSpriteSheet.RUNE
    }

    fun consume(hero: Hero) {
        affect(hero)

        GameScene.effect(Flare(7, 32f).color(glowing()?.color ?: 0xffffff, true).show(
                hero.sprite.parent, DungeonTilemap.tileCenterToWorld(hero.pos), 2f))
    }

    protected abstract fun affect(hero: Hero)

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

    override fun affect(hero: Hero) {
        hero.regeneration += 0.1f
    }
}

class MendingRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0x00ff00)

    override fun affect(hero: Hero) {
        Buff.prolong(hero, Recovery::class.java, 100f)
    }

    class Recovery : FlavourBuff()
}

class CriticalRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0x0000ff)

    override fun affect(hero: Hero) {
        Buff.prolong(hero, Critical::class.java, 20f)
    }

    class Critical : FlavourBuff()
}

class BrightRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0xffffff)

    override fun affect(hero: Hero) {
        Buff.prolong(hero, SharpVision::class.java, 80f)
        Buff.prolong(hero, MoonNight::class.java, 80f)
    }
}

class HasteRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0xff0000)

    override fun affect(hero: Hero) {
        Buff.prolong(hero, Haste::class.java, 40f)
    }

    class Haste : FlavourBuff()
}

class TreasureRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0xfdd14d)

    override fun affect(hero: Hero) {
        Gold().random().apply {
            quantity = (quantity * Random.Float(2f, 3f)).toInt()
        }.doPickUp(hero)
    }
}

class BloodRune : Rune() {
    override fun glowing(): ItemSprite.Glowing = ItemSprite.Glowing(0xd90355)

    override fun affect(hero: Hero) {
        Buff.prolong(hero, BloodSuck::class.java, 80f)
        StaminaOverload(min(hero.HT / 2, 50)).attachTo(hero)
    }
}

