package com.egoal.darkestpixeldungeon.items.weapon.missiles

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.SmokeOfBlindness
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Random

class SmokeSparks : MissileWeapon() {
    init {
        image = ItemSpriteSheet.SMOKE_SPARKS
    }

    override fun min(lvl: Int) = 1

    override fun max(lvl: Int) = 4

    override fun STRReq(lvl: Int): Int = 10

    override fun onThrow(cell: Int) {
        Sample.INSTANCE.play(Assets.SND_BLAST)
        GameScene.add(Blob.seed(cell, 80, SmokeOfBlindness::class.java))
    }

    override fun proc(dmg: Damage): Damage {
        Buff.affect(dmg.to as Char, Blindness::class.java, 3f)
        dmg.addFeature(Damage.Feature.ACCURATE) // cannot miss
        return super.proc(dmg)
    }

    override fun random(): Item {
        quantity = Random.Int(3, 5)
        return this
    }

    override fun price(): Int = 5 * quantity
}