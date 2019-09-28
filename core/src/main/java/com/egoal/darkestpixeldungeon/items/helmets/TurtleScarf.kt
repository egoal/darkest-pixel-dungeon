package com.egoal.darkestpixeldungeon.items.helmets

import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.round

class TurtleScarf : Helmet() {
    init {
        image = ItemSpriteSheet.TURTLE_SCARF_BLUE + Random.Int(4)
    }

    override fun desc(): String {
        var desc = super.desc()
        if (isIdentified) {
            desc += "\n\n" + Messages.get(this, "effect-desc")
            if (cursed)
                desc += "\n\n" + Messages.get(this, "cursed-desc")
        }

        return desc
    }

    override fun procTakenDamage(dmg: Damage) {
        if (dmg.type == Damage.Type.MAGICAL)
            dmg.value = round(dmg.value * ratio()).toInt()
    }

    private fun ratio(): Float = if (cursed) 1.25f else 0.8f

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(IMAGE, image)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        image = bundle.getInt(IMAGE)
    }

    companion object {
        private const val IMAGE = "image"
    }
}