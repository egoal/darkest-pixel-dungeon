package com.egoal.darkestpixeldungeon.items.specials

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.Actor
import com.egoal.darkestpixeldungeon.actors.Char
import com.egoal.darkestpixeldungeon.actors.Damage
import com.egoal.darkestpixeldungeon.actors.buffs.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.mobs.Mob
import com.egoal.darkestpixeldungeon.effects.CellEmitter
import com.egoal.darkestpixeldungeon.effects.MagicMissile
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.mechanics.Ballistica
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.scenes.CellSelector
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.IconTitle
import com.egoal.darkestpixeldungeon.windows.WndTitledMessage
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.lang.RuntimeException
import java.util.ArrayList
import kotlin.math.max
import kotlin.math.min

class UrnOfShadow : Special() {
    init {
        image = ItemSpriteSheet.URN_OF_SHADOW
    }

    var volume: Int = 0

    private val isFull: Boolean get() = volume == MAX_VOLUME

    override val isIdentified: Boolean
        get() = true

    override val isUpgradable: Boolean
        get() = false

    override fun upgrade(): Item {
        GLog.p(M.L(this, "Levelup"))
        return super.upgrade()
    }

    override fun use(hero: Hero) {
        GameScene.show(WndUrnOfShadow())
    }

    fun collectSoul(mob: Mob) {
        curUser = Dungeon.hero

        if (Dungeon.level.distance(curUser.pos, mob.pos) > COLLECT_RANGE) return

        if (mob.camp != Char.Camp.ENEMY) return

        val properties = mob.properties()

        val cnt = when {
            properties.contains(Char.Property.UNDEAD) || properties.contains(Char.Property.PHANTOM) -> 0
            properties.contains(Char.Property.MINIBOSS) || properties.contains(Char.Property.BOSS) -> 5
            mob.maxLvl >= (curUser.lvl + 3) -> 2
            else -> 1
        }

        if (cnt > 0) {
            volume = min(MAX_VOLUME, volume + cnt)
            updateQuickslot()

            GLog.i(M.L(this, "collected", mob.name))
            CellEmitter.get(curUser.pos).burst(ShadowParticle.CURSE, 5)
            Sample.INSTANCE.play(Assets.SND_BURNING)

            if (isFull) GLog.w(M.L(this, "full"))
        }
    }

    fun consume(value: Int) {
        volume -= value
        updateQuickslot()
    }

    override fun status(): String = "$volume"

    override fun desc(): String = super.desc() + "\n\n" + M.L(this, "desc_hint")

    private fun spells(): List<String> = listOf(CAST_SOUL_SIPHON, CAST_SOUL_BURN, CAST_SOUL_MARK, CAST_DEMENTAGE)

    private fun cost(spell: String): Int {
        return when (spell) {
            CAST_SOUL_SIPHON -> 2
            CAST_SOUL_BURN -> 3
            CAST_SOUL_MARK -> 5
            CAST_DEMENTAGE -> 10
            else -> throw RuntimeException("undefined spell.")
        }
    }

    private fun cast(spell: String) {
        when (spell) {
            CAST_SOUL_SIPHON -> castSoulSiphon()
            CAST_SOUL_BURN -> castSoulBurn()
            CAST_SOUL_MARK -> castSoulMark()
            CAST_DEMENTAGE -> castDementage()
            else -> throw RuntimeException("undefined spell.")
        }
    }

    private fun castSoulSiphon() {
        caster.onChar = { ch: Char ->
            if (ch === curUser) GLog.w(M.L(UrnOfShadow::class.java, "not_yourself"))
            else {
                consume(2)
                Item.curUser.sprite.zap(ch.pos)
                Item.curUser.spend(1f)
                Item.curUser.busy()

                MagicMissile.shadow(Item.curUser.sprite.parent, Item.curUser.pos, ch.pos) {
                    if (ch.buff(Dementage::class.java) != null) {
                        // recover
                        val value = Random.IntRange(10, ch.HT / 3)
                        val dhp = min(value, ch.HT - ch.HP)
                        if (dhp > 0) {
                            Item.curUser.HP = max(1, Item.curUser.HP - dhp / 2)
                            ch.HP += dhp
                            ch.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 2)
                        }
                    } else {
                        Buff.prolong(ch, Senile::class.java, Senile.DURATION).ratio = 0.2f + 0.05f * (level())
                    }
                    Item.curUser.next()
                }
                Sample.INSTANCE.play(Assets.SND_ZAP)
            }
        }

        GameScene.selectCell(caster)
    }

    private fun castSoulBurn() {
        caster.onChar = { ch: Char ->
            if (ch === Item.curUser) GLog.w(M.L(UrnOfShadow::class.java, "not_yourself"))
            else {
                consume(3)
                Item.curUser.sprite.zap(ch.pos)
                Item.curUser.spend(1f)
                Item.curUser.busy()

                MagicMissile.shadow(Item.curUser.sprite.parent, Item.curUser.pos, ch.pos) {
                    val value = 5 + Random.NormalIntRange(Dungeon.depth, Dungeon.depth * 2) + level() * 5
                    val dmg = Damage(value, Item.curUser, ch).type(Damage.Type.MAGICAL).addFeature(Damage.Feature.ACCURATE)
                    ch.takeDamage(dmg)
                    Buff.affect(ch, SoulBurning::class.java).reignite(ch)
                    Item.curUser.next()
                }
                Sample.INSTANCE.play(Assets.SND_ZAP)
            }
        }

        GameScene.selectCell(caster)
    }

    private fun castSoulMark() {
        caster.onChar = { ch: Char ->
            if (ch === Item.curUser) GLog.w(M.L(UrnOfShadow::class.java, "not_yourself"))
            else {
                consume(5)
                Item.curUser.sprite.zap(ch.pos)
                Item.curUser.spend(1f)
                Item.curUser.busy()

                MagicMissile.shadow(Item.curUser.sprite.parent, Item.curUser.pos, ch.pos) {
                    Buff.prolong(ch, SoulMark::class.java, SoulMark.DURATION * Math.pow(1.1, level().toDouble()).toFloat()).level = 1 + level()
                    Item.curUser.next()
                }
                Sample.INSTANCE.play(Assets.SND_ZAP)
            }
        }

        GameScene.selectCell(caster)
    }

    private fun castDementage() {
        caster.onChar = { ch: Char ->
            if (ch === Item.curUser) GLog.w(M.L(UrnOfShadow::class.java, "not_yourself"))
            else if (ch.buff(Corruption::class.java) != null) GLog.w(M.L(UrnOfShadow::class.java, "already_dementage"))
            else if (ch.properties().contains(Char.Property.BOSS) || ch.properties().contains(Char.Property.MINIBOSS))
                GLog.w(M.L(UrnOfShadow::class.java, "boss"))
            else if (ch is Mob && (ch.camp != Char.Camp.ENEMY || ch.properties().contains(Char.Property.UNDEAD) ||
                            ch.immunizedBuffs().contains(Dementage::class.java)))
                GLog.w(M.L(UrnOfShadow::class.java, "no_soul"))
            else {
                consume(10)
                Item.curUser.sprite.zap(ch.pos)
                Item.curUser.spend(1f)
                Item.curUser.busy()

                MagicMissile.shadow(Item.curUser.sprite.parent, Item.curUser.pos, ch.pos) {
                    Buff.append(ch, Dementage::class.java)
                    ch.HT += (ch.HT * 0.1f * level()).toInt()
                    ch.HP = ch.HT
                    GLog.i(M.L(UrnOfShadow::class.java, "sucess_dementage", ch.name))
                    Item.curUser.next()
                }
                Sample.INSTANCE.play(Assets.SND_ZAP)
            }
        }

        GameScene.selectCell(caster)
    }

    private val caster = object : CellSelector.Listener {
        var onChar: ((Char) -> Unit)? = null

        override fun onSelect(cell: Int?) {
            if (cell != null) {
                val shot = Ballistica(Item.curUser.pos, cell, Ballistica.MAGIC_BOLT)
                val c = Actor.findChar(shot.collisionPos)
                if (c != null) {
                    if (onChar != null) onChar!!(c)
                } else
                    GLog.w(M.L(UrnOfShadow::class.java, "not_select_target"))
            }
        }

        override fun prompt(): String = M.L(UrnOfShadow::class.java, "prompt")
    }

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(VOLUME, volume)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        volume = bundle.getInt(VOLUME)
    }

    companion object {
        private const val MAX_VOLUME = 10
        private const val COLLECT_RANGE = 6f

        private const val VOLUME = "volume"

        ///
        private const val WIN_WIDTH = 80f
        private const val BTN_HEIGHT = 20f
        private const val GAP = 2f

        private const val CAST_SOUL_SIPHON = "soul_siphon"
        private const val CAST_SOUL_BURN = "soul_burn"
        private const val CAST_SOUL_MARK = "soul_mark"
        private const val CAST_DEMENTAGE = "dementage"

        private const val WIDTH_CAST_BUTTON = 60f
        private const val WIDTH_HELP_BUTTON = 15f
    }

    inner class WndUrnOfShadow : Window() {
        init {
            val title = IconTitle().apply {
                icon(ItemSprite(image(), null))
                label(M.T(name()))
                setRect(0f, 0f, WIN_WIDTH, 0f)
            }
            add(title)

            var y = title.bottom()
            for (spell in spells()) y = addCastAndHelpButton(spell, y + GAP)

            resize(WIN_WIDTH.toInt(), (y + GAP).toInt())
        }

        private fun addCastAndHelpButton(spell: String, y: Float): Float {
            val cost = cost(spell)

            val btnCast = object : RedButton(M.L(UrnOfShadow::class.java, spell)) {
                override fun onClick() {
                    hide()
                    this@UrnOfShadow.cast(spell)
                }
            }.apply {
                setRect(0f, y, WIDTH_CAST_BUTTON, BTN_HEIGHT)
                enable(cost <= volume)
            }
            add(btnCast)

            val btnHelp = object : RedButton("?") {
                override fun onClick() {
                    GameScene.show(WndTitledMessage(ItemSprite(image(), null),
                            M.L(UrnOfShadow::class.java, spell),
                            M.L(UrnOfShadow::class.java, spell + "_desc") + M.L(UrnOfShadow::class.java, "cost", cost)))
                }
            }.apply { setRect(WIN_WIDTH - WIDTH_HELP_BUTTON, btnCast.top(), WIDTH_HELP_BUTTON, BTN_HEIGHT) }
            add(btnHelp)

            return btnCast.bottom()
        }
    }
}