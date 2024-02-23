package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Stasis
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.watabou.noosa.audio.Sample

class ReagentOfPetrifaction : Reagent(true) {
    init {
        image = ItemSpriteSheet.REAGENT_PETRIFACTION
    }

    override fun drink(hero: Hero) {
        super.drink(hero)

        GameScene.flash(0xffffff)
        Sample.INSTANCE.play(Assets.SND_TELEPORT)

        GLog.w(M.L(this, "onstasis"))

        Buff.affect(hero, Stasis::class.java).setDuration(10f)
    }
}