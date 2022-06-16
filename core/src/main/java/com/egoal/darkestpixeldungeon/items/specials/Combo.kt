package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndActionList
import com.watabou.utils.Bundle
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class Combo : Special() {
    init {
        image = ItemSpriteSheet.NULLWARN
    }

    private var comboTime = 0
    private var count = 0
    private var adrenaline = 0
    private var focusId = 0
    private var focusCount = 0

    val AttackSpeedFactor get() = 0.2f + .8f * .8f.pow(focusCount)

    override fun tick() {
        if (comboTime > 0) --comboTime
        else {
            adrenaline = max(0, adrenaline - 1)
        }

        //todo: update image?
        updateQuickslot()
    }

    fun hit(target: Char) {
        comboTime = 4
        adrenaline = min(adrenaline + 1, 15)
        ++count

        if (count >= 5) {
            Badges.validateMasteryCombo(count)
            GLog.p(M.L(this, "combo", count))
        }

        if (target.id() != focusId) {
            focusId = target.id()
            focusCount = 1
        } else ++focusCount
    }

    override fun use(hero: Hero) {

    }

    override fun status(): String? = if (adrenaline > 0) "$adrenaline" else null

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(TIME, comboTime)
        bundle.put(COUNT, count)
        bundle.put(ADRENALINE, adrenaline)
        bundle.put(FOCUS_ID, focusId)
        bundle.put(FOCUS_COUNT, focusCount)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        comboTime = bundle.getInt(TIME)
        count = bundle.getInt(COUNT)
        adrenaline = bundle.getInt(ADRENALINE)
        focusId = bundle.getInt(FOCUS_ID)
        focusCount = bundle.getInt(FOCUS_COUNT)
    }

    companion object {
        private const val COUNT = "count"
        private const val TIME = "combotime"
        private const val ADRENALINE = "adrenaline"
        private const val FOCUS_ID = "focusid"
        private const val FOCUS_COUNT = "focus-count"
    }

    abstract inner class ComboAction(val cost: Int) : WndActionList.Action() {
        override fun Name(): String = M.L(this, "name")
        override fun Info(): String = M.L(this, "info")
        override fun Disabled(): Boolean = cost > adrenaline

        override fun Execute() { Execute(Dungeon.hero) }

        protected abstract fun Execute(hero: Hero)
    }
}