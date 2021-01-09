package com.egoal.darkestpixeldungeon.items.potions

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.blobs.Blob
import com.egoal.darkestpixeldungeon.actors.blobs.WhiteFog
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.watabou.noosa.audio.Sample
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

class PotionOfMagicalFog : Potion() {
    init {
        initials = 13
    }

    override fun canBeReinforced(): Boolean = !reinforced

    override fun shatter(cell: Int) {
        if (Dungeon.visible[cell]) {
            setKnown()

            splash(cell)
            Sample.INSTANCE.play(Assets.SND_SHATTER)
        }

        if (reinforced) {
            PathFinder.NEIGHBOURS9.forEach {
                Actor.findChar(it + cell)?.let { ch ->
                    val len = Random.Float(10f, 15f)
                    Buff.prolong(ch, Vulnerable::class.java, len).apply {
                        dmgType = Damage.Type.MAGICAL
                        ratio = 1.35f
                    }
                }
            }
        }
        GameScene.add(Blob.seed(cell, 500, WhiteFog::class.java))
    }

    override fun price(): Int {
        return if (isKnown) (30f * quantity.toFloat() * if (reinforced) 1.5f else 1f).toInt() else super.price()
    }
}