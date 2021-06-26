package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.Buff
import com.egoal.darkestpixeldungeon.actors.buffs.FlavourBuff
import com.egoal.darkestpixeldungeon.actors.buffs.LifeLink
import com.egoal.darkestpixeldungeon.actors.buffs.MustDodge
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis
import com.egoal.darkestpixeldungeon.actors.buffs.Roots
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroLines
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.BlastParticle
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle
import com.egoal.darkestpixeldungeon.effects.particles.SmokeParticle
import com.egoal.darkestpixeldungeon.items.artifacts.Artifact
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave
import com.egoal.darkestpixeldungeon.levels.Level
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.scenes.CellSelector
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
import com.watabou.utils.PathFinder
import com.watabou.utils.Random

import java.util.ArrayList
import kotlin.math.max

/**
 * Created by 93942 on 7/29/2018.
 */

class Astrolabe : Special() {
    private var cooldown = 0
    private var blockNextNegative = false
    private var nextNegativeIsImprison = false

    internal var cachedInvoker_1: Invoker? = null
    internal var cachedInvoker_2: Invoker? = null

    init {
        image = ItemSpriteSheet.ASTROLABE
    }

    override fun use(hero: Hero) {
        GameScene.show(WndInvoke())
    }

    override fun tick() {
        if (cooldown > 0) cooldown -= 1
        updateQuickslot()
    }

    private fun updateSprite() {
        var magic = 0
        if (cachedInvoker_1 != null) ++magic
        if (cachedInvoker_2 != null) ++magic
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

        cooldown = NORMAL_COOLDOWN
        Sample.INSTANCE.play(Assets.SND_ASTROLABE)

        val invokePositive = Random.Float() < if (cursed) .5f else .75f

        if (!invokePositive && blockNextNegative) {
            blockNextNegative = false

            curUser.sprite.showStatus(0x420000, Messages.get(Invoker::class.java, "extremely_lucky_block"))
            return
        }

        val ivk: Invoker = if (!invokePositive && nextNegativeIsImprison) {
            imprison()
        } else if (invokePositive) randomPositiveInvoke()
        else randomNegativeInvoke()

        curUser.sprite.showStatus(ivk.color(), ivk.status())
        CellEmitter.get(curUser.pos).start(ShaftParticle.FACTORY, 0.2f, 3)

        if (ivk.positive) {
            // positive invoker will cached
            if (cachedInvoker_1 == null)
                cachedInvoker_1 = ivk
            else if (cachedInvoker_2 == null)
                cachedInvoker_2 = ivk
            else {
                // replace
                cachedInvoker_1 = cachedInvoker_2
                cachedInvoker_2 = ivk
            }
        } else {
            if (Random.Int(4) == 0) curUser.sayShort(HeroLines.DAMN)
            ivk.invoke(curUser, this)
        }
    }

    //fixme: bad parameter, index should be 1 or 2, bad logical
    private operator fun invoke(index: Int) {
        if (index == 1 && cachedInvoker_1 != null) {
            cachedInvoker_1!!.invoke(curUser, this)

            cachedInvoker_1 = cachedInvoker_2
            cachedInvoker_2 = null
        } else if (index == 2 && cachedInvoker_2 != null) {
            cachedInvoker_2!!.invoke(curUser, this)
            cachedInvoker_2 = null
        }

        updateQuickslot()
    }

    private fun randomPositiveInvoke(): Invoker = positiveInvokers[Random.chances(positiveProbs)].newInstance() as Invoker

    private fun randomNegativeInvoke(): Invoker = negativeInvokers[Random.chances(negativeProbs)].newInstance() as Invoker

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(COOLDOWN, cooldown)
        bundle.put(BLOCK_NEXT_NEGATIVE, blockNextNegative)
        bundle.put(NEXT_IS_IMPRISON, nextNegativeIsImprison)

        if (cachedInvoker_1 != null)
            bundle.put(CACHED_INVOKER_1, cachedInvoker_1!!.javaClass)
        if (cachedInvoker_2 != null)
            bundle.put(CACHED_INVOKER_2, cachedInvoker_2!!.javaClass)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        blockNextNegative = bundle.getBoolean(BLOCK_NEXT_NEGATIVE)
        nextNegativeIsImprison = bundle.getBoolean(NEXT_IS_IMPRISON)

        try {
            var c: Class<*>? = bundle.getClass(CACHED_INVOKER_1)
            if (c != null)
                cachedInvoker_1 = c.newInstance() as Invoker

            c = bundle.getClass(CACHED_INVOKER_2)
            if (c != null)
                cachedInvoker_2 = c.newInstance() as Invoker

        } catch (e: Exception) {
            DarkestPixelDungeon.reportException(e)
        }

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

            if (cachedInvoker_1 != null)
                pos = addInvoker(pos, WIDTH.toInt(), cachedInvoker_1!!.status(),
                        cachedInvoker_1!!.desc(), cachedInvoker_1!!.color(), index++)

            if (cachedInvoker_2 != null)
                pos = addInvoker(pos, WIDTH.toInt(), cachedInvoker_2!!.status(),
                        cachedInvoker_2!!.desc(), cachedInvoker_2!!.color(), index++)

            resize(WIDTH.toInt(), pos.toInt())
        }

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

        //
        private const val TIME_TO_INVOKE = 1f
        private const val NORMAL_COOLDOWN = 20

        private val positiveInvokers = arrayOf(
                foresight::class.java, purgation::class.java, life_link::class.java, extremely_lucky::class.java,
                pardon::class.java, faith::class.java, overload::class.java, guide::class.java,
                prophesy::class.java, sun_strike::class.java)
        private val positiveProbs = floatArrayOf(10f, 10f, 10f, 5f,
                10f, 5f, 10f, 10f,
                10f, 5f)
        private val negativeInvokers = arrayOf(punish::class.java, vain::class.java, feedback::class.java, imprison::class.java)
        private val negativeProbs = floatArrayOf(10f, 10f, 10f, 10f)

        private const val COOLDOWN = "cooldown"
        private const val BLOCK_NEXT_NEGATIVE = "block_next_negative"
        private const val NEXT_IS_IMPRISON = "next_negative_is_imprison"

        private const val CACHED_INVOKER_1 = "cached_invoker_1"
        private const val CACHED_INVOKER_2 = "cached_invoker_2"
    }

    //////////////////////////////////////////////////////////////////////////////
    //* invokers
    open class Invoker {
        protected var name_ = "invoker"
        protected var needTarget_ = false
        protected lateinit var user_: Hero
        protected lateinit var a_: Astrolabe
        var positive = true

        private var caster: CellSelector.Listener = object : CellSelector.Listener {
            override fun onSelect(cell: Int?) {
                if (cell != null) {
                    val shot = Ballistica(curUser.pos, cell, Ballistica.MAGIC_BOLT)
                    val c = Actor.findChar(shot.collisionPos)

                    invoke_on_target(user_, a_, c)

                    user_.spend(TIME_TO_INVOKE)
                    user_.busy()
                    user_.sprite.operate(user_.pos)
                }
            }

            override fun prompt(): String = M.L(Invoker::class.java, "prompt")
        }

        fun status(): String = M.L(Invoker::class.java, name_)

        fun desc(): String = M.L(Invoker::class.java, name_ + "_desc")

        fun color(): Int = if (positive) 0xCC5252 else 0x000026

        open operator fun invoke(user: Hero, a: Astrolabe) {
            user_ = user
            a_ = a
            if (needTarget_) {
                GameScene.selectCell(caster)
            } else {
                invoke_directly(user, a)

                user_.spend(TIME_TO_INVOKE)
                user_.busy()
                user_.sprite.operate(user_.pos)
            }
        }

        // impl
        protected open fun invoke_directly(user: Hero, a: Astrolabe) {}

        protected open fun invoke_on_target(user: Hero, a: Astrolabe, c: Char?) {}

        protected fun check_is_other(c: Char?): Boolean {
            if (c == null || c === user_) {
                GLog.w(M.L(Invoker::class.java, "not_select_target"))
                return false
            }
            return true
        }
    }

    // positive
    class foresight : Invoker() {
        init {
            name_ = "foresight"
        }

        override fun invoke_directly(user: Hero, a: Astrolabe) {
            Buff.prolong(user, MustDodge::class.java, 3f)
        }
    }

    class purgation : Invoker() {
        init {
            name_ = "purgation"
            needTarget_ = true
        }

        override fun invoke_on_target(user: Hero, a: Astrolabe, c: Char?) {
            if (check_is_other(c)) {
                val dmg = ((c!!.HT - c.HP) * .6f).toInt() + 1
                //todo: add effect
                c.takeDamage(Damage(dmg, user, c).addFeature(Damage.Feature.PURE or Damage.Feature.ACCURATE))
            }
        }
    }

    class life_link : Invoker() {
        init {
            name_ = "life_link"
            needTarget_ = true
        }

        override fun invoke_on_target(user: Hero, a: Astrolabe, c: Char?) {
            if (check_is_other(c)) {
                Buff.prolong(user, LifeLink::class.java, 3f).linker = c!!.id()
            }
        }
    }

    class extremely_lucky : Invoker() {
        init {
            name_ = "extremely_lucky"
        }

        override fun invoke_directly(user: Hero, a: Astrolabe) {
            // recover hp
            val heal = (user.HT - user.HP) / 10 + 1
            user.recoverHP(heal, a_)

            a.blockNextNegative = true
        }
    }

    class pardon : Invoker() {
        init {
            name_ = "pardon"
            needTarget_ = true
        }

        override fun invoke_on_target(user: Hero, a: Astrolabe, c: Char?) {
            if (check_is_other(c)) {
                val dhp = c!!.HP / 4 + 1
                c.recoverHP(dhp, a_)
                Buff.prolong(c, Vulnerable::class.java, Vulnerable.DURATION).ratio = 2f
            }
        }
    }

    class faith : Invoker() {
        init {
            name_ = "faith"
        }

        override fun invoke_directly(user: Hero, a: Astrolabe) {
            user.recoverSanity(Random.Int(1, 4))
            a.cooldown -= NORMAL_COOLDOWN / 2
        }
    }

    class overload : Invoker() {
        init {
            name_ = "overload"
            needTarget_ = true
        }

        override fun invoke_on_target(user: Hero, a: Astrolabe, c: Char?) {
            if (check_is_other(c)) {
                var cost = user.HT / 10
                if (cost >= user.HP) cost = user.HP - 1
                val dmg = cost * 2

                c!!.takeDamage(Damage(dmg, user, c).type(Damage.Type.MAGICAL))
                user.takeDamage(Damage(cost, a, c).addFeature(Damage.Feature.PURE or Damage.Feature.ACCURATE))
            }
        }
    }

    class guide : Invoker() {
        init {
            name_ = "guide"
            needTarget_ = true
        }

        override fun invoke_on_target(user: Hero, a: Astrolabe, c: Char?) {
            if (check_is_other(c)) {
                val shot = Ballistica(curUser.pos, c!!.pos, Ballistica.MAGIC_BOLT)
                if (shot.path.size > shot.dist + 1)
                    WandOfBlastWave.throwChar(c, Ballistica(c.pos, shot.path[shot.dist + 1], Ballistica.MAGIC_BOLT), 3)
            }
        }
    }

    class prophesy : Invoker() {
        init {
            name_ = "prophesy"
        }

        override fun invoke_directly(user: Hero, a: Astrolabe) {
            for (i in PathFinder.NEIGHBOURS8) {
                val ch = Actor.findChar(user.pos + i)
                if (ch != null) {
                    Buff.prolong(ch, Paralysis::class.java, 3f)
                    ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12)
                }
            }
        }
    }

    class sun_strike : Invoker(), CellSelector.Listener {
        init {
            name_ = "sun_strike"
        }

        override fun invoke(user: Hero, a: Astrolabe) {
            user_ = user
            a_ = a
            GameScene.selectCell(this)
        }

        override fun onSelect(cell: Int?) {
            if (cell != null) {
                if (Dungeon.level.visited[cell] || Dungeon.level.mapped[cell]) {
                    Buff.prolong(user_, buff::class.java, 2f).targetpos = cell

                    user_.spend(TIME_TO_CHANT)
                    user_.busy()
                    user_.sprite.operate(user_!!.pos)
                } else
                    GLog.w(M.L(Invoker::class.java, "not_select_target"))
            }
        }

        override fun prompt(): String = M.L(Invoker::class.java, "prompt")

        class buff : FlavourBuff() {
            var targetpos = 0

            override fun act(): Boolean {
                // cast! like bomb...
                Sample.INSTANCE.play(Assets.SND_BLAST)
                if (Dungeon.visible[targetpos]) {
                    CellEmitter.center(targetpos).burst(BlastParticle.FACTORY, 50)
                }

                var terrainAffected = false
                val enemies = ArrayList<Char>()
                for (n in PathFinder.NEIGHBOURS9) {
                    val c = targetpos + n
                    if (c >= 0 && c < Dungeon.level.length()) {
                        if (Dungeon.visible[c]) {
                            CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4)
                        }

                        if (Level.flamable[c]) {
                            Dungeon.level.destroy(c)
                            GameScene.updateMap(c)
                            terrainAffected = true
                        }

                        //destroys items / triggers bombs caught in the blast.
                        val heap = Dungeon.level.heaps.get(c)
                        heap?.explode()

                        val ch = Actor.findChar(c)
                        if (ch != null && ch !== Dungeon.hero) {
                            enemies.add(ch)
                        }
                    }
                }

                if (enemies.isNotEmpty()) {
                    var totalDamage = 0
                    for (ch in enemies) if (ch.HT > totalDamage) totalDamage = ch.HT
                    totalDamage = max(50, totalDamage)

                    val dmg = totalDamage / enemies.size
                    for (ch in enemies) {
                        val d = Damage(Random.IntRange(dmg * 7 / 10, dmg * 12 / 10), curUser, ch).addFeature(Damage.Feature.DEATH)
                        //^ cannot be pure, which will kill boss directly.
                        if (ch.pos == targetpos) d.value += d.value / 4
                        ch.defendDamage(d)
                        ch.takeDamage(d)
                    }
                }

                if (terrainAffected)
                    Dungeon.observe()

                return super.act()
            }

            override fun storeInBundle(bundle: Bundle) {
                super.storeInBundle(bundle)
                bundle.put(TARGET, targetpos)

            }

            override fun restoreFromBundle(bundle: Bundle) {
                super.restoreFromBundle(bundle)
                targetpos = bundle.getInt(TARGET)
            }

            companion object {
                private const val TARGET = "targetpos"
            }
        }

        companion object {
            private const val TIME_TO_CHANT = 3f
        }
    }

    // negative
    class punish : Invoker() {
        init {
            name_ = "punish"
            positive = false
        }

        override fun invoke_directly(user: Hero, a: Astrolabe) {
            user.takeDamage(Damage((user.HP * .25f).toInt(), this, user).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.ACCURATE))
        }
    }

    class vain : Invoker() {
        init {
            name_ = "vain"
            positive = false
        }

        override fun invoke_directly(user: Hero, a: Astrolabe) {
            Buff.prolong(user, Vertigo::class.java, 5f)
            Buff.prolong(user, Weakness::class.java, 5f)
        }
    }

    class feedback : Invoker() {
        init {
            name_ = "feedback"
            positive = false
        }

        override fun invoke_directly(user: Hero, a: Astrolabe) {
            user.takeDamage(Damage(Random.Int(1, 10), this, user).type(Damage
                    .Type.MENTAL)
                    .addFeature(Damage.Feature.ACCURATE))
        }
    }

    class imprison : Invoker() {
        init {
            name_ = "imprison"
            positive = false
        }

        override fun invoke_directly(user: Hero, a: Astrolabe) {
            Buff.prolong(user, Roots::class.java, 3f)
            a.cooldown += NORMAL_COOLDOWN
        }
    }
}
