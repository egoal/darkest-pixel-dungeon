package com.egoal.darkestpixeldungeon.items.weapon.melee

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.bags.Bag
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRecharging
import com.egoal.darkestpixeldungeon.items.wands.Wand
import com.egoal.darkestpixeldungeon.items.wands.WandOfCorruption
import com.egoal.darkestpixeldungeon.items.wands.WandOfDisintegration
import com.egoal.darkestpixeldungeon.items.wands.WandOfRegrowth
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndItem
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.particles.PixelParticle
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList

class MagesStaff(w: Wand? = null) : MeleeWeapon() {
    private var wand: Wand? = null

    init {
        image = ItemSpriteSheet.MAGES_STAFF

        tier = 1

        defaultAction = AC_ZAP
        usesTargeting = true

        unique = true
        bones = false

        if (w != null) setWand(w)
    }

    private fun setWand(w: Wand) {
        w.apply {
            identify()
            cursed = false
            maxCharges = kotlin.math.min(maxCharges + 1, 10)
            curCharges = w.curCharges
        }
        wand = w
        name = M.L(wand!!, "staff_name")
    }

    override fun max(lvl: Int): Int = 4 * (tier + 1) + lvl * (tier + 1)

    override fun actions(hero: Hero): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_IMBUE)
        if (wand != null && wand!!.curCharges > 0) actions.add(AC_ZAP)
        return actions
    }

    override fun activate(ch: Char) {
        wand?.charge(ch, STAFF_SCALE_FACTOR)
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)
        if (action == AC_IMBUE) {
            Item.curUser = hero
            GameScene.selectItem(wandImbuer, WndBag.Mode.WAND, M.L(this, "prompt"))
        } else if (action == AC_ZAP) {
            if (wand == null) GameScene.show(WndItem(null, this, true))
            else wand!!.execute(hero, AC_ZAP)
        }
    }

    override fun proc(dmg: Damage): Damage {
        // battle mage
        wand?.let {
            if (Dungeon.hero.subClass == HeroSubClass.BATTLEMAGE) {
                if (it.curCharges < it.maxCharges) it.partialCharge += 0.33f
                ScrollOfRecharging.charge(dmg.from as Hero)
                it.onHit(this@MagesStaff, dmg)
            }
        }
        return super.proc(dmg)
    }

    override fun reachFactor(hero: Hero): Int {
        var reach = super.reachFactor(hero)
        if (wand is WandOfDisintegration && hero.subClass == HeroSubClass.BATTLEMAGE) ++reach
        return reach
    }

    override fun collect(container: Bag): Boolean = if (super.collect(container)) {
        if (container.owner != null && wand != null) wand!!.charge(container.owner!!, STAFF_SCALE_FACTOR)
        true
    } else false

    override fun onDetach() {
        wand?.stopCharging()
    }

    fun imbueWand(w: Wand, owner: Char?) {
        w.cursed = false
        wand = null

        // syncs the level
        var targetLevel = kotlin.math.max(level(), w.level())
        //if the staff's level is being overridden by the wand, preserve 1 upgrade
        if (w.level() >= level() && level() > 0) targetLevel++

        val staffLevelDiff = targetLevel - level()
        if (staffLevelDiff > 0) upgrade(staffLevelDiff)
        else if (staffLevelDiff < 0) degrade(-staffLevelDiff) // why would you do this??

        val wandLevelDiff = targetLevel - w.level()
        if (wandLevelDiff > 0) w.upgrade(wandLevelDiff)
        else if (wandLevelDiff < 0) w.degrade(-wandLevelDiff)

        setWand(w)
        if (owner != null) w.charge(owner)

        updateQuickslot()
    }

    fun wandClass(): Class<out Wand>? = wand?.javaClass

    override fun upgrade(): Item {
        super.upgrade()
        wand?.let {
            // val c = it.curCharges
            it.upgrade()
            // additional charge
            it.maxCharges = kotlin.math.min(it.maxCharges + 1, 10)
            it.curCharges = kotlin.math.min(it.curCharges + 1, 10)
            updateQuickslot()
        }
        return this
    }

    override fun degrade(): Item {
        super.degrade()
        wand?.let {
            val c = it.curCharges
            it.degrade()
            it.maxCharges = kotlin.math.min(it.maxCharges + 1, 10)
            it.curCharges = c - 1
            updateQuickslot()
        }

        return this
    }

    override fun status(): String? = wand?.status() ?: super.status()

    override fun info(): String {
        return super.info() +
                if (wand == null) "\n\n" + M.L(this, "no_wand")
                else "\n\n" + M.L(this, "has_wand", M.L(wand!!, "name")) + " " + wand!!.statsDesc()
    }

    override fun emitter(): Emitter? {
        if (wand == null) return null
        return Emitter().apply {
            pos(12.5f, 2.5f)
            fillTarget = false
            pour(StaffParticleFactory, 0.06f)
        }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(WAND, wand)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        (bundle.get(WAND) as Wand?)?.let { setWand(it) }
    }

    override fun price(): Int = 0

    private val wandImbuer = object : WndBag.Listener {
        override fun onSelect(item: Item?) {
            if (item == null) return

            val w = item as Wand
            if (!w.isIdentified) {
                GLog.w(M.L(MagesStaff::class.java, "id_first"))
                return
            }
            if (w.cursed) {
                GLog.w(M.L(MagesStaff::class.java, "cursed"))
                return
            }

            if (wand == null) applyWand(w)
            else {
                val newLevel = if (w.level() >= level()) {
                    if (level() > 0) w.level() + 1 else w.level()
                } else level()

                GameScene.show(object : WndOptions("", M.L(MagesStaff::class.java, "warning", newLevel),
                        M.L(MagesStaff::class.java, "yes"),
                        M.L(MagesStaff::class.java, "no")) {
                    override fun onSelect(index: Int) {
                        if (index == 0) applyWand(w)
                    }
                })
            }
        }

        private fun applyWand(w: Wand) {
            Sample.INSTANCE.play(Assets.SND_BURNING)
            Item.curUser.sprite.emitter().burst(ElmoParticle.FACTORY, 12)
            Item.evoke(Item.curUser)

            Dungeon.quickslot.clearItem(w)

            w.detach(Item.curUser.belongings.backpack)
            Badges.validateTutorial()

            GLog.p(M.L(MagesStaff::class.java, "imbue", w.name()))
            imbueWand(w, Item.curUser)
            updateQuickslot()
        }
    }


    private val StaffParticleFactory = object : Emitter.Factory() {
        override fun emit(emitter: Emitter, index: Int, x: Float, y: Float) {
            var c = emitter.getFirstAvailable(StaffParticle::class.java) as StaffParticle?
            if (c == null) {
                c = StaffParticle()
                emitter.add(c)
            }
            c.reset(x, y)
        }

        //some particles need light mode, others don't
        override fun lightMode(): Boolean = !(wand is WandOfDisintegration ||
                wand is WandOfCorruption || wand is WandOfRegrowth)
    }

    inner class StaffParticle : PixelParticle() {
        private var minSize = 0f
        private var maxSize = 0f
        var sizeJitter = 0f

        fun reset(x: Float, y: Float) {
            revive()

            speed.set(0f)

            this.x = x
            this.y = y

            wand?.staffFx(this)
        }

        fun setSize(minSize: Float, maxSize: Float) {
            this.minSize = minSize
            this.maxSize = maxSize
        }

        fun setLifespan(life: Float) {
            left = life
            lifespan = left
        }

        fun shuffleXY(amt: Float) {
            x += Random.Float(-amt, amt)
            y += Random.Float(-amt, amt)
        }

        fun radiateXY(amt: Float) {
            val hypot = Math.hypot(speed.x.toDouble(), speed.y.toDouble()).toFloat()
            this.x += speed.x / hypot * amt
            this.y += speed.y / hypot * amt
        }

        override fun update() {
            super.update()
            size(minSize + left / lifespan * (maxSize - minSize) + Random.Float(sizeJitter))
        }
    }

    companion object {
        private const val AC_IMBUE = "IMBUE"
        private const val AC_ZAP = "ZAP"

        private const val STAFF_SCALE_FACTOR = 0.75f

        private const val WAND = "wand"
    }

}