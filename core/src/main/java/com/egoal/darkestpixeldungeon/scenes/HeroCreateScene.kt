package com.egoal.darkestpixeldungeon.scenes

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.hero.Hero
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.items.Item
import com.egoal.darkestpixeldungeon.items.food.Humanity
import com.egoal.darkestpixeldungeon.items.food.OrchidRoot
import com.egoal.darkestpixeldungeon.items.potions.ReagentOfHealing
import com.egoal.darkestpixeldungeon.items.unclassified.*
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.sprites.HeroSprite
import com.egoal.darkestpixeldungeon.ui.Archs
import com.egoal.darkestpixeldungeon.ui.InputButton
import com.egoal.darkestpixeldungeon.ui.RedButton
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.egoal.darkestpixeldungeon.windows.WndTabbed
import com.watabou.gltextures.TextureCache
import com.watabou.noosa.*
import com.watabou.noosa.ui.Button
import com.watabou.utils.Random
import kotlin.Nothing
import kotlin.math.max

class HeroCreateScene : PixelScene() {
    override fun create() {
        super.create()

        uiCamera.visible = false

        val w = Camera.main.width
        val h = Camera.main.height

        val archs = Archs().apply { setSize(w.toFloat(), h.toFloat()) }
        add(archs)

//        val btnExit = ExitButton()
//        btnExit.setPos(Camera.main.width - btnExit.width(), 0f)
//        add(btnExit)

        val wnd = object : WndCreateHero() {
            override fun onBackPressed() {
                this@HeroCreateScene.onBackPressed()
            }

            override fun startNewGame() {
                UserName = btnName.inputText
                CurrentClass = btnClass.heroClass
                BornPrize = btnPrize.prize()

                DarkestPixelDungeon.lastHeroName(UserName)

                Dungeon.hero = null
                InterlevelScene.mode = InterlevelScene.Mode.DESCEND
                Generator.reset()

                if (DarkestPixelDungeon.intro()) {
                    DarkestPixelDungeon.intro(false)
                    Game.switchScene(IntroScene::class.java)
                } else Game.switchScene(InterlevelScene::class.java)
            }
        }
        add(wnd)

        fadeIn()
    }

    companion object {
        private const val WND_WIDTH = 116f
        private const val WND_MARGIN = 2f
        private const val WND_TAB_WIDTH = 38f

        private const val BTN_HEIGHT = 12f
        private const val BTN_WIDTH = 48f

        private const val START_BTN_WIDTH = 70f

        private const val LBL_WDITH = 20f

        private const val PORTRAIT_WIDTH = 36f //todo:

        // state
        var UserName: String = DarkestPixelDungeon.lastHeroName()
        var CurrentClass = HeroClass.WARRIOR
        var BornPrize = Prize.NOTHING
    }

    override fun onBackPressed() {
        DarkestPixelDungeon.switchNoFade(SlotSelectScene::class.java)
    }

    private open class WndCreateHero : WndTabbed() {
        // settings
        val btnName: InputButton
        val btnClass = ClassButton()
        val btnPrize = PrizeButton()

        // infos
        private val tabDesc: DescTab
        private val tabPerk: PerkTab
        private var tabMastery: MasteryTab? = null
        private val portrait: Image

        fun heroClass() = btnClass.heroClass
        fun bornPrize(): Prize = btnPrize.prize()

        init {
            var pos = WND_MARGIN
            // settings
            val addLabel = { y: Float, text: String ->
                val lbl = renderText(text, 8)
                lbl.x = WND_MARGIN
                lbl.y = y + (BTN_HEIGHT - lbl.height()) / 2f
                add(lbl)

                lbl.y + lbl.height() + WND_MARGIN
            }

            btnName = object : InputButton(DarkestPixelDungeon.lastHeroName(), 8) {
                override fun isValid(text: String): Boolean = text.length in 0..20
            }
            btnName.setRect(LBL_WDITH + WND_MARGIN * 2, pos, BTN_WIDTH, BTN_HEIGHT)
            add(btnName)
            pos = addLabel(pos, M.L(HeroCreateScene::class.java, "name"))

            btnClass.setRect(LBL_WDITH + WND_MARGIN * 2, pos, BTN_WIDTH, BTN_HEIGHT)
            add(btnClass)
            pos = addLabel(pos, M.L(HeroCreateScene::class.java, "class"))

            btnPrize.setRect(LBL_WDITH + WND_MARGIN * 2, pos, BTN_WIDTH, BTN_HEIGHT)
            add(btnPrize)
            pos = addLabel(pos, M.L(HeroCreateScene::class.java, "prize"))

            val portraitBg = Chrome.get(Chrome.Type.TOAST_DARKER)
            portraitBg.x = WND_WIDTH - WND_MARGIN - PORTRAIT_WIDTH
            portraitBg.y = (BTN_HEIGHT * 3 + WND_MARGIN * 4 - PORTRAIT_WIDTH) / 2f
            portraitBg.size(PORTRAIT_WIDTH, PORTRAIT_WIDTH)
            PixelScene.align(portraitBg)
            add(portraitBg)

            portrait = HeroSprite.Portrait(heroClass(), 0)
            portrait.x = portraitBg.x + (portraitBg.width - portrait.width) / 2f
            portrait.y = portraitBg.y + (portraitBg.height - portrait.height) / 2f
            PixelScene.align(portrait)
            add(portrait)

            val startGame = object : RedButton(M.L(HeroCreateScene::class.java, "new_game"), 8) {
                override fun onClick() {
                    startNewGame()
                }
            }
            startGame.setRect((WND_WIDTH - START_BTN_WIDTH) / 2f, pos + WND_MARGIN * 2f, START_BTN_WIDTH, 16f)
            add(startGame)

            pos = startGame.bottom() + WND_MARGIN

            // split
            val cb = ColorBlock(WND_WIDTH - WND_MARGIN * 2, 1f, 0xff222222.toInt())
            cb.x = WND_MARGIN
            cb.y = pos + WND_MARGIN
            add(cb)

            pos = cb.y + cb.height() + WND_MARGIN * 2f

            // tabs
            tabDesc = DescTab(pos, heroClass())
            add(tabDesc)
            var tab = RankingTab(M.L(HeroCreateScene::class.java, "tab_desc"), tabDesc)
            tab.setSize(WND_TAB_WIDTH, tabHeight().toFloat())
            add(tab)

            tabPerk = PerkTab(pos, heroClass())
            add(tabPerk)
            tab = RankingTab(M.L(HeroCreateScene::class.java, "tab_perk"), tabPerk)
            tab.setSize(WND_TAB_WIDTH, tabHeight().toFloat())
            add(tab)

            var h = max(tabDesc.y, tabPerk.y)

            if (Badges.isUnlocked(heroClass().masteryBadge())) {
                tabMastery = MasteryTab(pos, heroClass())
                add(tabMastery)

                tab = RankingTab(M.L(HeroCreateScene::class.java, "tab_mastery"), tabMastery)
                tab.setSize(WND_TAB_WIDTH, tabHeight().toFloat())
                add(tab)

                h = max(h, tabMastery!!.y)
            }

            resize(WND_WIDTH.toInt(), (h + WND_MARGIN).toInt())

            layoutTabs()
            select(0)
        }

        override fun onBackPressed() {} // do nothing

        fun onHeroClassChanged() {
            portrait.copy(HeroSprite.Portrait(heroClass(), 0))

            tabDesc.refresh(heroClass())
            tabPerk.refresh(heroClass())
            tabMastery?.refresh(heroClass())

            var h = max(tabDesc.y, tabPerk.y)
            if (tabMastery != null) {
                tabMastery!!.refresh(heroClass())
                h = max(h, tabMastery!!.y)
            }

            resize(WND_WIDTH.toInt(), (h + WND_MARGIN).toInt())
        }

        open fun startNewGame() {}

        // settings
        inner class ClassButton(var heroClass: HeroClass = HeroClass.WARRIOR) : RedButton(heroClass.title(), 8) {

            override fun onClick() {
                val title = M.L(HeroCreateScene::class.java, "select_class")
                val message = ""
                val classes = HeroClass.values()

                Game.scene().add(object : WndOptions(title, message, *classes.map { it.title() }.toTypedArray()) {
                    override fun onSelect(index: Int) {
                        if (heroClass != classes[index]) {
                            heroClass = classes[index]
                            text(heroClass.title())

                            onHeroClassChanged()
                        }
                    }
                })
            }
        }

        class PrizeButton(private var index: Int = 0,
                          private val prizes: Array<Prize> = Prize.values()) : RedButton(prizes[index].title(), 8) {
            fun prize(): Prize = prizes[index]

            override fun onClick() {
                val title = M.L(HeroCreateScene::class.java, "select_prize")
                val message = ""

                Game.scene().add(object : WndOptions(title, message, *prizes.map { it.title() }.toTypedArray()) {
                    override fun onSelect(index: Int) {
                        this@PrizeButton.index = index
                        text(prize().title())
                    }
                })
            }
        }

        // info tabs
        private inner class RankingTab(label: String, private val page: Group?) : WndTabbed.LabeledTab(label) {
            override fun select(value: Boolean) {
                super.select(value)
                if (page != null) {
                    page.active = selected
                    page.visible = page.active
                }
            }
        }

        private class DescTab(private val initY: Float, heroClass: HeroClass) : Group() {
            var y = 0f
            private val text = renderMultiline(6)

            init {
                refresh(heroClass)
            }

            fun refresh(heroClass: HeroClass) {
                y = initY

                val message = heroClass.desc()

                text.text(message, (WND_WIDTH - WND_MARGIN * 2).toInt())
                text.setPos(WND_MARGIN, y)
                add(text)

                y = text.bottom() + WND_MARGIN
            }
        }

        private class PerkTab(private val initY: Float, heroClass: HeroClass) : Group() {
            var y = 0f

            init {
                refresh(heroClass)
            }

            fun refresh(heroClass: HeroClass) {
                clear()
                y = initY

                var dotWidth = 0f

                val items = heroClass.perks()
                var pos = y

                for (i in items.indices) {
                    if (i > 0) {
                        pos += 4f
                    }

                    val dot = createText("-", 6f)
                    dot.x = WND_MARGIN
                    dot.y = pos
                    if (dotWidth == 0f) {
                        dot.measure()
                        dotWidth = dot.width()
                    }
                    add(dot)

                    val item = renderMultiline(items[i], 6)
                    item.maxWidth((WND_WIDTH - WND_MARGIN * 2 - dotWidth).toInt())
                    item.setPos(dot.x + dotWidth, pos)
                    add(item)

                    pos += item.height()
                }

                val cb = ColorBlock(WND_WIDTH - WND_MARGIN * 2, 1f, 0xff222222.toInt())
                cb.x = WND_MARGIN
                cb.y = pos + WND_MARGIN
                add(cb)
                pos = cb.y + cb.height()

                // align initial perks
                // todo: clean this.
                val description = PixelScene.renderMultiline(6).apply {
                    maxWidth((WND_WIDTH - WND_MARGIN * 2f).toInt())
                }
                add(description)

                for (pr in heroClass.initialPerks().withIndex()) {
                    val x = WND_MARGIN + pr.index % 4 * 23f
                    val y = pos + pr.index / 4 * 23f

                    val perk = pr.value

                    val pb = object : Button() {

                        private lateinit var icon: Image

                        override fun createChildren() {
                            super.createChildren()
                            val icons = TextureCache.get(Assets.PERKS)
                            icon = Image(icons)
                            icon.frame(TextureFilm(icons, 16, 16).get(perk.image()))
                            add(icon)
                        }

                        override fun layout() {
                            super.layout()

                            icon.x = x + (width - icon.width) / 2f
                            icon.y = y + (height - icon.height) / 2f
                        }

                        override fun onClick() {
                            description.text(perk.description())
                        }
                    }

                    pb.setRect(x, y, 22f, 22f)
                    add(pb)
                }
                pos += WND_MARGIN + 23f

                description.setPos(WND_MARGIN, pos + WND_MARGIN)
                pos += WND_MARGIN + 30f

                y = pos + WND_MARGIN
            }
        }

        private class MasteryTab(private val initY: Float, heroClass: HeroClass) : Group() {
            var y = 0f
            private val text = renderMultiline(6)

            init {
                refresh(heroClass)
            }

            fun refresh(heroClass: HeroClass) {
                y = initY

                val message = when (heroClass) {
                    HeroClass.WARRIOR -> HeroSubClass.GLADIATOR.desc() + "\n\n" + HeroSubClass.BERSERKER.desc()
                    HeroClass.MAGE -> HeroSubClass.BATTLEMAGE.desc() + "\n\n" + HeroSubClass.WARLOCK.desc()
                    HeroClass.ROGUE -> HeroSubClass.FREERUNNER.desc() + "\n\n" + HeroSubClass.ASSASSIN.desc()
                    HeroClass.HUNTRESS -> HeroSubClass.SNIPER.desc() + "\n\n" + HeroSubClass.WARDEN.desc()
                    HeroClass.SORCERESS -> HeroSubClass.STARGAZER.desc() + "\n\n" + HeroSubClass.WITCH.desc()
                    HeroClass.EXILE -> HeroSubClass.LANCER.desc() + "\n\n" + HeroSubClass.WINEBIBBER.desc()
                }

                text.text(message, (WND_WIDTH - WND_MARGIN * 2).toInt())
                text.setPos(WND_MARGIN, y)
                add(text)

                y = text.bottom() + WND_MARGIN
            }
        }
    }
}