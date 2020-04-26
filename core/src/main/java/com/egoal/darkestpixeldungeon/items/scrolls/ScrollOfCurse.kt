package com.egoal.darkestpixeldungeon.items.scrolls

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.watabou.noosa.audio.Sample

import java.security.cert.TrustAnchor

/**
 * Created by 93942 on 10/15/2018.
 */

class ScrollOfCurse : InventoryScroll() {
    init {
        initials = 13
        mode = WndBag.Mode.EQUIPMENT

        bones = true
    }

    override fun onItemSelected(item: Item) {
        CellEmitter.get(Item.curUser.pos).burst(ShadowParticle.CURSE, 5)
        Sample.INSTANCE.play(Assets.SND_CURSED)

        item.cursed = true
        item.cursedKnown = true
        updateQuickslot()

        GLog.w(Messages.get(this, "cursed", item.name()))
    }
}
