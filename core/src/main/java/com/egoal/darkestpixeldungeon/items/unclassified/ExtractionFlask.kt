package com.egoal.darkestpixeldungeon.items.unclassified

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Chrome
import com.egoal.darkestpixeldungeon.Dungeon
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.effects.particles.PurpleParticle
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.KGenerator
import com.egoal.darkestpixeldungeon.items.potions.Potion
import com.egoal.darkestpixeldungeon.items.potions.PotionOfToxicGas
import com.egoal.darkestpixeldungeon.items.weapon.Weapon
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Unstable
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.plants.Plant
import com.egoal.darkestpixeldungeon.plants.Sorrowmoss
import com.egoal.darkestpixeldungeon.scenes.GameScene
import com.egoal.darkestpixeldungeon.scenes.PixelScene
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.ItemSlot
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.ui.Window
import com.egoal.darkestpixeldungeon.utils.GLog
import com.egoal.darkestpixeldungeon.windows.IconTitle
import com.egoal.darkestpixeldungeon.windows.WndBag
import com.egoal.darkestpixeldungeon.windows.WndMessage
import com.watabou.noosa.NinePatch
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.ui.Component
import com.watabou.utils.Bundle
import com.watabou.utils.Random
import java.util.ArrayList
import kotlin.math.exp

class ExtractionFlask : Item() {
    init {
        image = ItemSpriteSheet.EXTRACTION_FLASK

        defaultAction = AC_REFINE
        unique = true
    }

    private var reinforced = false // once reinforced, can strengthen potion
    private var refined = 0 // refined counts

    fun reinforce() {
        reinforced = true
        GLog.p(Messages.get(this, "upgrade"))
    }

    override fun actions(hero: Hero?): ArrayList<String> {
        val actions = super.actions(hero)
        actions.add(AC_REFINE)
        if (reinforced) actions.add(AC_STRENGTHEN)
        return actions
    }

    override fun execute(hero: Hero, action: String) {
        super.execute(hero, action)

        if (action == AC_REFINE)
            GameScene.show(WndCraft(this, MODE_REFINE))
        else if (action == AC_STRENGTHEN)
            GameScene.show(WndCraft(this, MODE_STRENGTHEN))
    }

    override fun desc(): String {
        var desc = Messages.get(this, "desc", refined)

        desc += if (!cursed)
            "\n\n" + Messages.get(this, "desc_hint")
        else
            "\n\n" + Messages.get(this, "desc_cursed")

        return desc
    }

    override fun isUpgradable(): Boolean = false

    fun verifyRefine(s1: Plant.Seed, s2: Plant.Seed): String? {
        val vial = curUser.belongings.getItem(DewVial::class.java)
        return if (vial == null || vial.Volume < minDewRequired())
            Messages.get(this, "no_water", minDewRequired())
        else
            null
    }

    fun refine(s1: Plant.Seed, s2: Plant.Seed) {
        // cast items
        s1.detach(curUser.belongings.backpack)
        s2.detach(curUser.belongings.backpack)
        Dungeon.hero.belongings.getItem(DewVial::class.java)!!.Volume -= minDewRequired()

        // more likely to be toxic gas
        val potion: Potion? = when {
            s1 is Sorrowmoss.Seed || s2 is Sorrowmoss.Seed -> PotionOfToxicGas()
            Random.Int(15) == 0 -> {
                GLog.w(Messages.get(this, "refine_failed"))
                null
            }
            Random.Int(6) == 0 -> PotionOfToxicGas()
            else -> KGenerator.POTION.generate() as Potion
        }
        ++refined

        potion?.let {
            //^ refined okay, do inscribe
            GLog.p(Messages.get(this, "refine", potion.name()))
            if (!potion.doPickUp(curUser))
                Dungeon.level.drop(potion, curUser.pos).sprite.drop()

            curUser.belongings.weapon?.let {
                if (it is Weapon) {
                    if (it.STRReq() < curUser.STR() && !it.cursed) {
                        // 0.2-> 0.5
                        val x = exp(refined.toFloat() / 4f)
                        val p = x / (x + 1f) * .5f
                        if (Random.Float() < p) {
                            when (Random.Int(3)) {
                                0 -> it.enchant(Venomous())
                                1 -> it.enchant(Unstable())
                                else -> it.enchant()
                            }

                            GLog.w(Messages.get(this, "inscribed"))
                        }
                    } else
                        GLog.w(Messages.get(this, "cannot_inscribe"))
                }
            }
        }

        // spend time
        with(curUser) {
            sprite.operate(pos)
            sprite.centerEmitter().start(PurpleParticle.BURST, 0.05f, 10)
            spend(TIME_TO_REFINE)
            busy()
        }
    }

    fun verifyStrengthen(potion: Potion, seed: Plant.Seed): String? {
        return when {
            !potion.isIdentified -> Messages.get(this, "not_identified")
            potion.reinforced -> Messages.get(this, "reinforced")
            !potion.canBeReinforced() -> Messages.get(this, "cannot_reinforce")
            else -> null
        }
    }

    fun strengthen(potion: Potion, seed: Plant.Seed) {
        // consume items
        seed.detach(Dungeon.hero.belongings.backpack)
        val p = potion.detach(Dungeon.hero.belongings.backpack) as Potion

        // reinforce
        p.reinforce()
        if (!p.doPickUp(Dungeon.hero))
            Dungeon.level.drop(p, Dungeon.hero.pos).sprite.drop()

        // spend time

        with(curUser) {
            sprite.operate(pos)
            sprite.centerEmitter().start(Speck.factory(Speck.FORGE), 0.05f, 10)
            spend(TIME_TO_EXTRACT)
            busy()
        }
    }

    private fun minDewRequired() = if (reinforced) 3 else 4

    override fun storeInBundle(bundle: Bundle) {
        super.storeInBundle(bundle)
        bundle.put(REINFORCED, reinforced)
        bundle.put(REFINED, refined)
    }

    override fun restoreFromBundle(bundle: Bundle) {
        super.restoreFromBundle(bundle)
        reinforced = bundle.getBoolean(REINFORCED)
        refined = bundle.getInt(REFINED)
    }

    companion object {
        private const val AC_REFINE = "refine"
        private const val AC_STRENGTHEN = "strengthen"

        private const val TIME_TO_REFINE = 2f
        private const val TIME_TO_EXTRACT = 2f

        private const val REINFORCED = "reinforced"
        private const val REFINED = "refined"

        // interact
        private const val MODE_REFINE = 0
        private const val MODE_STRENGTHEN = 1

        private const val WND_WIDTH = 110f
        private const val WND_BTN_SIZE = 32f
        private const val WND_GAP = 2f
        private const val WND_BTN_GAP = 20f

        private class WndCraft(private val flask: ExtractionFlask, private val mode: Int) : Window() {

            private lateinit var btnItem_1: ItemButton
            private lateinit var btnItem_2: ItemButton
            private var btnPressed: ItemButton? = null
            private var btnCraft: RedButton

            init {
                val title = IconTitle(ItemSprite(flask.image(), null), Messages.get(this, "prompt")).apply {
                    setRect(0f, 0f, WND_WIDTH, 0f)
                }
                add(title)

                // first one be seed or potion
                btnItem_1 = object : ItemButton() {
                    override fun onClick() {
                        btnPressed = btnItem_1

                        val bagmode = if (mode == MODE_REFINE) WndBag.Mode.SEED else WndBag.Mode.POTION
                        val prompt = Messages.get(WndCraft::class.java, if (mode == MODE_REFINE) "select_seed" else "select_potion")
                        GameScene.selectItem(itemSelector, bagmode, prompt)
                    }
                }.apply {
                    setRect((WND_WIDTH - WND_BTN_GAP) / 2 - WND_BTN_SIZE, title.bottom() + WND_GAP,
                            WND_BTN_SIZE, WND_BTN_SIZE)
                }
                add(btnItem_1)

                // second one is just seed
                btnItem_2 = object : ItemButton() {
                    override fun onClick() {
                        btnPressed = btnItem_2

                        GameScene.selectItem(itemSelector, WndBag.Mode.SEED, Messages.get(WndCraft::class.java, "select_seed"))
                    }
                }.apply {
                    setRect(btnItem_1.right() + WND_BTN_GAP, btnItem_1.top(), WND_BTN_SIZE, WND_BTN_SIZE)
                }
                add(btnItem_2)

                btnCraft = object : RedButton(Messages.get(this, "done")) {
                    override fun onClick() {
                        if (mode == MODE_REFINE)
                            flask.refine(btnItem_1.item as Plant.Seed, btnItem_2.item as Plant.Seed)
                        else
                            flask.strengthen(btnItem_1.item as Potion, btnItem_2.item as Plant.Seed)

                        // kill items
                        btnItem_1.item(null)
                        btnItem_2.item(null)

                        hide()
                    }
                }.apply {
                    enable(false)
                    setRect(0f, btnItem_1.bottom() + WND_GAP, WND_WIDTH.toFloat(), 20f)
                }
                add(btnCraft)

                resize(WND_WIDTH.toInt(), btnCraft.bottom().toInt())
            }

            private val itemSelector = WndBag.Listener { item ->
                if (item != null) {
                    // give back
                    btnPressed!!.item?.let {
                        if (!it.collect()) Dungeon.level.drop(it, Dungeon.hero.pos)
                    }

                    // take from backpack
                    btnPressed!!.item(item.detach(Dungeon.hero.belongings.backpack))

                    verifyItems()
                }
            }

            private fun verifyItems() {
                if (btnItem_1.item != null && btnItem_2.item != null) {
                    val result = if (mode == MODE_REFINE)
                        flask.verifyRefine(btnItem_1.item as Plant.Seed, btnItem_2.item as Plant.Seed)
                    else // strengthen 
                        flask.verifyStrengthen(btnItem_1.item as Potion, btnItem_2.item as Plant.Seed)

                    if (result == null)
                        btnCraft.enable(true)
                    else {
                        GameScene.show(WndMessage(result))
                        btnCraft.enable(false)
                    }
                }
            }
        }

        // item button
        private abstract class ItemButton : Component() {
            private lateinit var bg: NinePatch
            private lateinit var slot: ItemSlot
            var item: Item? = null

            override fun createChildren() {
                super.createChildren()

                bg = Chrome.get(Chrome.Type.BUTTON)
                add(bg)

                slot = object : ItemSlot() {
                    override fun onTouchDown() {
                        bg.brightness(1.2f)
                        Sample.INSTANCE.play(Assets.SND_CLICK)
                    }

                    override fun onTouchUp() {
                        bg.resetColor()
                    }

                    override fun onClick() {
                        this@ItemButton.onClick()
                    }
                }.apply {
                    enable(true)
                }
                add(slot)
            }

            protected abstract fun onClick()

            override fun layout() {
                super.layout()

                bg.x = x
                bg.y = y
                bg.size(width, height)

                slot.setRect(x + 2, y + 2, width - 4, height - 4)
            }

            fun item(item: Item?) {
                this.item = item
                slot.item(item)
            }
        }
    }
}