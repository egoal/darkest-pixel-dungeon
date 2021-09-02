package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.KRandom
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.IconTitle
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import kotlin.math.pow

/**
 * Created by 93942 on 7/29/2018.
 */

class Astrolabe : Special() {
    private var cooldown = 0
    var blockNextNegative = false

    private var cachedInvoker_1: Int = -1
    private var cachedInvoker_2: Int = -1
    private var upgraded = 0
    private var upgrade_interval = 3

    private lateinit var positiveInvokers: List<Invoker>
    private val positiveProbs = floatArrayOf(10f, 10f, 10f, 10f,
            10f, 5f, 10f, 10f,
            10f, 5f, 0f) //! note 0f here: blessed_grant
    private val BLESSED_GRANT_IDX = positiveProbs.size - 1

    private val negativeInvokers = listOf(punish(), vain(), feedback(), imprison())

    init {
        image = ItemSpriteSheet.ASTROLABE
    }

    private fun resetInvokers() {
        positiveInvokers = listOf(foresight(), purgation(), life_link(), extremely_lucky(),
                pardon(), faith(), overload(), guide(),
                prophesy(), sun_strike(), blessed_grant())
    }

    override fun use(hero: Hero) {
        if (!::positiveInvokers.isInitialized) resetInvokers()
        GameScene.show(WndInvoke())
    }

    override fun tick() {
        if (cooldown > 0) cooldown -= 1
        updateQuickslot()
    }

    override fun status(): String? = if (cooldown > 0) "$cooldown" else null

    private fun updateSprite() {
        var magic = 0
        if (cachedInvoker_1 >= 0) ++magic
        if (cachedInvoker_2 >= 0) ++magic
        image = when (magic) {
            1 -> ItemSpriteSheet.ASTROLABE_1
            2 -> ItemSpriteSheet.ASTROLABE_2
            else -> ItemSpriteSheet.ASTROLABE
        }
    }

    // invoke logic
    private fun invokeMagic() {
        if (cooldown > 0) {
            GLog.i(M.L(this, "cooldown", cooldown))
            return
        }

        Sample.INSTANCE.play(Assets.SND_ASTROLABE)

        val invokePositive = Random.Float() < if (cursed) .5f else .75f

        if (!invokePositive && blockNextNegative) {
            blockNextNegative = false
            curUser.sprite.showStatus(0x420000, Messages.get(extremely_lucky::class.java, "block"))

            cooldown = 20
            return
        }

        if (invokePositive) {
            val idx = randomPositiveIndex()
            if (cachedInvoker_1 < 0) cachedInvoker_1 = idx
            else if (cachedInvoker_2 < 0) cachedInvoker_2 = idx
            else {
                // replace
                cachedInvoker_1 = cachedInvoker_2
                cachedInvoker_2 = idx
            }

            val ivk = positiveInvokers[idx]
            curUser.sprite.showStatus(ivk.color(), ivk.status())
            CellEmitter.get(curUser.pos).start(ShaftParticle.FACTORY, .2f, 3)

            cooldown = ivk.InvokeCD
        } else {
            val ivk = negativeInvokers[randomNegativeIndex()]
            curUser.sprite.showStatus(ivk.color(), ivk.status())
            CellEmitter.get(curUser.pos).start(ShaftParticle.FACTORY, .2f, 3)

            if (Random.Int(4) == 0) curUser.sayShort(HeroLines.DAMN)
            ivk.invoke(curUser, this)

            cooldown = ivk.InvokeCD
        }

        if (upgrade_interval > 0) upgrade_interval--
    }

    //fixme: bad parameter, index should be 1 or 2, bad logical
    private operator fun invoke(index: Int) {
        if (index == 1) {
            positiveInvokers[cachedInvoker_1].invoke(curUser, this)
            cachedInvoker_1 = cachedInvoker_2
            cachedInvoker_2 = -1
        } else if (index == 2) {
            positiveInvokers[cachedInvoker_2].invoke(curUser, this)
            cachedInvoker_2 = -1
        }

        updateQuickslot()
    }

    private fun randomPositiveIndex(): Int {
        // blessed grant.
        if (upgrade_interval<=0 && upgraded < MAX_UPGRADE_TIMES &&
                cachedInvoker_1 != BLESSED_GRANT_IDX && cachedInvoker_2 != BLESSED_GRANT_IDX) {
            val p = 0.4f * 0.85f.pow(upgraded) // about 85 calls in total
            if (Random.Float() < p) return BLESSED_GRANT_IDX
        }

        var idx = -1
        do {
            idx = Random.chances(positiveProbs)
        } while (idx == cachedInvoker_1 || idx == cachedInvoker_2)
        return idx
    }

    private fun randomNegativeIndex(): Int = Random.Int(negativeInvokers.size)

    fun doUpgrade() {
        val cans = KRandom.nOf(positiveInvokers.filter { it.IsUpgradable }, 5)

        if (cans.isEmpty()) GLog.w(M.L(Astrolabe::class.java, "cannot_upgrade"))
        else
            WndOptions.Show(ItemSprite(this), name(), M.L(Astrolabe::class.java, "select_upgrade"), *cans.map { it.status() }.toTypedArray()) {
                if (it >= 0) {
                    cans[it].upgrade()
                    upgraded++
                    GLog.w(M.L(Astrolabe::class.java, "upgraded"))
                    upgrade_interval = 3
                }
            }
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(COOLDOWN, cooldown)
        bundle.put(BLOCK_NEXT_NEGATIVE, blockNextNegative)

        bundle.put(CACHED_INVOKER_1, cachedInvoker_1)
        bundle.put(CACHED_INVOKER_2, cachedInvoker_2)

        bundle.put(UPGRADED, upgraded)
        bundle.put(UPGRADE_INTERVAL, upgrade_interval)

        if (!::positiveInvokers.isInitialized) resetInvokers()
        bundle.put(INVOKERS, positiveInvokers)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        blockNextNegative = bundle.getBoolean(BLOCK_NEXT_NEGATIVE)

        cachedInvoker_1 = bundle.getInt(CACHED_INVOKER_1)
        cachedInvoker_2 = bundle.getInt(CACHED_INVOKER_2)

        upgraded = bundle.getInt(UPGRADED)
        upgrade_interval = bundle.getInt(UPGRADE_INTERVAL)

        check(!::positiveInvokers.isInitialized)
        positiveInvokers = bundle.getCollection(INVOKERS).map { it as Invoker }.toList()

        updateSprite()
    }

    override fun price(): Int = 0

    // todo: refactor this
    inner class WndInvoke : Window() {
        init {
            val ic = IconTitle(ItemSprite(image(), null), M.L(Astrolabe::class.java, "select_invoker"))
            ic.setRect(0f, 0f, WIDTH, 0f)
            add(ic)

            var pos = ic.bottom() + MARGIN

            var index = 0
            pos = addInvoker(pos, WIDTH.toInt(), Messages.get(Astrolabe::class.java, "ac_invoke"), "", 0xFFFFFF, index++)

            if (cachedInvoker_1 >= 0) pos = addInvoker(pos, positiveInvokers[cachedInvoker_1], index++)

            if (cachedInvoker_2 >= 0) pos = addInvoker(pos, positiveInvokers[cachedInvoker_2], index++)

            resize(WIDTH.toInt(), pos.toInt())
        }

        private fun addInvoker(pos: Float, invoker: Invoker, idx: Int): Float =
                addInvoker(pos, WIDTH.toInt(), invoker.status(), invoker.desc(), invoker.color(), idx)

        private fun addInvoker(pos: Float, width: Int, name: String, help: String, color: Int, idx: Int): Float {
            val btn = object : RedButton(name) {
                override fun onClick() {
                    hide()
                    onSelect(idx)
                }
            }
            btn.textColor(color)

            if (help.isNotEmpty()) {
                val btnHelp = object : RedButton("?") {
                    override fun onClick() {
                        GameScene.show(WndOptions(name, help))
                    }
                }
                btnHelp.textColor(color)

                btn.setRect(MARGIN, pos, (width - MARGIN * 3 - WIDTH_HELP_BUTTON), BTN_HEIGHT)
                add(btn)
                btnHelp.setRect((width - MARGIN - WIDTH_HELP_BUTTON), pos, WIDTH_HELP_BUTTON, BTN_HEIGHT)
                add(btnHelp)
            } else {
                btn.setRect(MARGIN, pos, width - MARGIN * 2f, BTN_HEIGHT)
                add(btn)
            }

            return pos + BTN_HEIGHT + MARGIN
        }

        private fun onSelect(index: Int) {
            when (index) {
                1 -> invoke(1)
                2 -> invoke(2)
                else -> invokeMagic()
            }

            updateSprite()
            updateQuickslot()
        }
    }

    companion object {
        // window
        private const val WIDTH = 120f
        private const val MARGIN = 2f
        private const val BTN_HEIGHT = 20f

        private const val WIDTH_HELP_BUTTON = 15f

        private const val MAX_UPGRADE_TIMES = 10 //

        private const val COOLDOWN = "cooldown"
        private const val BLOCK_NEXT_NEGATIVE = "block_next_negative"

        private const val CACHED_INVOKER_1 = "cached_invoker_1"
        private const val CACHED_INVOKER_2 = "cached_invoker_2"

        private const val UPGRADED = "upgraded"
        private const val UPGRADE_INTERVAL = "upgrade-interval"
        private const val INVOKERS = "invokers"
    }
}
