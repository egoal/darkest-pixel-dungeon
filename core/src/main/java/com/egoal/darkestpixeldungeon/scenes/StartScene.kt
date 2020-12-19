package com.egoal.darkestpixeldungeon.scenes

import android.util.Log
import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass
import com.egoal.darkestpixeldungeon.effects.BannerSprites
import com.egoal.darkestpixeldungeon.effects.Speck
import com.egoal.darkestpixeldungeon.items.Generator
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.*
import com.egoal.darkestpixeldungeon.windows.InputDialog
import com.egoal.darkestpixeldungeon.windows.WndClass
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.*
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.particles.BitmaskEmitter
import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.ui.Button
import com.watabou.utils.Callback
import kotlin.math.min
import kotlin.math.sin

class StartScene : PixelScene() {
    private lateinit var btnLoadGame: GameButton
    private lateinit var btnNewGame: GameButton
    private val shields = hashMapOf<HeroClass, ClassShield>()

    private lateinit var tip: RenderedText
    private lateinit var slider: ClassSlideBar
    private lateinit var unlock: Group
    private var unlockText: RenderedTextMultiline? = null

    private var buttonX: Float = 0f
    private var buttonY: Float = 0f

    override fun create() {
        super.create()

        Badges.loadGlobal()

        uiCamera.visible = false

        val w = Camera.main.width
        val h = Camera.main.height

        val width = WIDTH_P
        val height = HEIGHT_P

        val left = (w - width) / 2f
        val top = (h - height) / 2f
        val bottom = h - top

        val archs = Archs().apply { setSize(w.toFloat(), h.toFloat()) }
        add(archs)

        val title = BannerSprites.get(BannerSprites.Type.SELECT_YOUR_HERO)
        title.x = (w - title.width()) / 2f
        title.y = top
        align(title)
        add(title)

        buttonX = left
        buttonY = bottom - BUTTON_HEIGHT

        btnNewGame = object : GameButton(M.L(this, "new")) {
            override fun onClick() {
                if (GamesInProgress.check(CurrentClass) != null) {
                    this@StartScene.add(object : WndOptions(
                            M.L(StartScene::class.java, "really"),
                            M.L(StartScene::class.java, "warning"),
                            M.L(StartScene::class.java, "yes"),
                            M.L(StartScene::class.java, "no")) {
                        override fun onSelect(index: Int) {
                            if (index == 0) startNewGame()
                        }
                    })
                } else
                    startNewGame()
            }
        }
        add(btnNewGame)

        btnLoadGame = object : GameButton(M.L(this, "load")) {
            override fun onClick() {
                InterlevelScene.mode = if (GamesInProgress.check(CurrentClass)!!.isBackup)
                    InterlevelScene.Mode.REFLUX else InterlevelScene.Mode.CONTINUE

                Game.switchScene(InterlevelScene::class.java)
            }

//            override fun onLongClick(): Boolean {
//                InterlevelScene.mode = InterlevelScene.Mode.REFLUX
//                Game.switchScene(InterlevelScene::class.java)
//                return false
//            }
        }
        add(btnLoadGame)

        slider = ClassSlideBar().apply { centered(w / 2f, buttonY - 20f) }
        add(slider)

        val centralHeight = (buttonY - 20f) - 10f - title.y - title.height()
        val shieldW = width / 4
        val shieldH = min(centralHeight, shieldW)
        val shieldTop = title.y + title.height + (centralHeight - shieldH) / 2f
        val shieldLeft = left + (width - shieldW) / 2f
        for (cl in enumValues<HeroClass>()) {
            val shield = object : ClassShield(cl) {
                override fun onTouchDown() {
                    Sample.INSTANCE.play(Assets.SND_CLICK, 1f, 1f, 1.2f)
                    // this@StartScene.add(WndClass(heroClass))
                    InputDialog.GetString("name your hero", "unnamed"){ Log.d("dpd", "name set: $it") }
                }
            }
            shield.setRect(shieldLeft, shieldTop, shieldW, shieldH)
            shield.visible = false
            add(shield)
            shields[cl] = shield
        }

        tip = renderText(M.L(this, "click_for_info"), 5)
        tip.x = left + (width - tip.width()) / 2f
        tip.y = (buttonY - 20f) - tip.height() - 10f
        align(tip)
        add(tip)

        unlock = Group()
        add(unlock)
        if (!isHuntressUnlocked() || !IsSorceressUnlocked() || !IsExileUnlocked()) {
            unlockText = renderMultiline(9).apply {
                maxWidth(width.toInt())
                hardlight(0xffff00)
            }
            unlock.add(unlockText)
        }

        val btnExit = ExitButton()
        btnExit.setPos(Camera.main.width - btnExit.width(), 0f)
        add(btnExit)

        updateClass(HeroClass.values()[DarkestPixelDungeon.lastClass()])
        fadeIn()

        Badges.loadingListener = Callback {
            if (Game.scene() === this@StartScene)
                DarkestPixelDungeon.switchNoFade(StartScene::class.java)
        }
    }

    override fun update() {
        super.update()

        // todo: refactor this
        tip.alpha(sin(Game.timeTotal * 2f) * .4f + .6f)
    }

    override fun destroy() {
        Badges.saveGlobal()
        Badges.loadingListener = null

        super.destroy()
    }

    override fun onBackPressed() {
        DarkestPixelDungeon.switchNoFade(TitleScene::class.java)
    }

    private fun startNewGame() {
        Dungeon.hero = null
        InterlevelScene.mode = InterlevelScene.Mode.DESCEND
        Generator.reset()

        if (DarkestPixelDungeon.intro()) {
            DarkestPixelDungeon.intro(false)
            Game.switchScene(IntroScene::class.java)
        } else Game.switchScene(InterlevelScene::class.java)
    }

    private fun updateClass(cl: HeroClass) {
        shields[CurrentClass]!!.visible = false
        slider.highlightDot(CurrentClass.ordinal, false)

        CurrentClass = cl

        shields[CurrentClass]!!.visible = true
        shields[CurrentClass]!!.showSpeckEffects()
        slider.highlightDot(CurrentClass.ordinal, true)

        if (!IsLocked(CurrentClass)) {
            unlock.visible = false

            val info = GamesInProgress.check(CurrentClass)
            if (info != null) {
                btnLoadGame.visible = true
                val str = if (info.isBackup) M.L(this, "back_up") else M.L(this, "depth_level", info.depth, info.level)
                btnLoadGame.secondary(str, info.isChallenged)

                btnNewGame.visible = true
                btnNewGame.secondary(M.L(this, "erase"), false)

                val w = (Camera.main.width - GAP) / 2 - buttonX
                btnLoadGame.setRect(buttonX, buttonY, w, BUTTON_HEIGHT)
                btnNewGame.setRect(btnLoadGame.right() + GAP, buttonY, w, BUTTON_HEIGHT)
            } else {
                btnLoadGame.visible = false

                btnNewGame.visible = true
                btnNewGame.secondary(null, false)
                btnNewGame.setRect(buttonX, buttonY, Camera.main.width - buttonX * 2, BUTTON_HEIGHT)
            }
        } else {
            val text = when (CurrentClass) {
                HeroClass.HUNTRESS -> M.L(this, "unlock_huntress")
                HeroClass.SORCERESS -> M.L(this, "unlock_sorceress")
                HeroClass.EXILE -> M.L(this, "unlock_exile")
                else -> ""
            }
            // unlock
            val height = HEIGHT_P
            val bottom = Camera.main.height - (Camera.main.height - height) / 2
            unlockText!!.text(text)
            unlockText!!.setPos(Camera.main.width / 2f - unlockText!!.width() / 2f,
                    (bottom - BUTTON_HEIGHT) + (BUTTON_HEIGHT - unlockText!!.height()) / 2f)
            align(unlockText)

            unlock.visible = true
            btnLoadGame.visible = false
            btnNewGame.visible = false
        }
    }

    companion object {
        private const val BUTTON_HEIGHT = 24f
        private const val GAP = 2f

        private const val WIDTH_P = 116f
        private const val HEIGHT_P = 220f

        var CurrentClass = HeroClass.WARRIOR

        //todo:
        private fun isHuntressUnlocked(): Boolean = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_2)

        private fun IsSorceressUnlocked(): Boolean = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_3)

        private fun IsExileUnlocked(): Boolean = Badges.isUnlocked(Badges.Badge.BOSS_SLAIN_4)

        private fun IsLocked(cls: HeroClass): Boolean = (cls == HeroClass.HUNTRESS && !isHuntressUnlocked()) ||
                (cls == HeroClass.SORCERESS && !IsSorceressUnlocked()) ||
                (cls == HeroClass.EXILE && !IsExileUnlocked())

        // GameButton
        private const val SECONDARY_COLOR_N = 0xCACFC2
        private const val SECONDARY_COLOR_H = 0xFFFF88

        // ClassShield
        private const val MIN_BRIGHTNESS = 0.6f

        private const val BASIC_HIGHLIGHTED = 0xCACFC2
        private const val MASTERY_HIGHLIGHTED = 0xFFFF88

        private const val WIDTH = 24
        private const val HEIGHT = 32
        private const val SCALE = 2.5f
    }

    private inner class ClassSlideBar : Group() {
        val btnLeft: IconButton
        val btnRight: IconButton
        val dots: Array<Image>

        init {
            btnLeft = object : IconButton(Image().apply {
                flipHorizontal = true
                copy(Icons.get(Icons.ARROW_RIGHT))
            }) {
                override fun onClick() {
                    super.onClick()
                    val values = enumValues<HeroClass>()
                    val pre = if (CurrentClass.ordinal == 0) values.size - 1 else CurrentClass.ordinal - 1

                    updateClass(values[pre])
                }
            }
            add(btnLeft)

            btnRight = object : IconButton(Icons.get(Icons.ARROW_RIGHT)) {
                override fun onClick() {
                    super.onClick()
                    val values = enumValues<HeroClass>()
                    val next = (CurrentClass.ordinal + 1) % values.size

                    updateClass(values[next])
                }
            }
            add(btnRight)

            dots = Array(enumValues<HeroClass>().size) { Icons.get(Icons.DOT_OFF) }
            for (dot in dots) add(dot)
        }

        fun centered(x: Float, y: Float) {
            val DOT_GAP = 3f
            val DOT_SIZE = dots[0].width()
            val DOTS_Width = (dots.size - 1) * DOT_GAP + dots.size * DOT_SIZE

            val GAP = 10f
            val ARROW_HEIGHT = 20f
            val ARROW_WIDTH = 20f

            val dotLeft = x - DOTS_Width / 2f

            for (pr in dots.withIndex()) {
                pr.value.x = dotLeft + pr.index * (DOT_GAP + DOT_SIZE)
                pr.value.y = y
            }

            btnLeft.setRect(dotLeft - GAP - ARROW_WIDTH, y - ARROW_HEIGHT / 2, ARROW_WIDTH, ARROW_HEIGHT)
            btnRight.setRect(x + DOTS_Width / 2f + GAP, btnLeft.top(), ARROW_WIDTH, ARROW_HEIGHT)
        }

        fun highlightDot(idx: Int, on: Boolean) {
            if (on) dots[idx].copy(Icons.get(Icons.DOT_ON))
            else dots[idx].copy(Icons.get(Icons.DOT_OFF))
        }
    }

    private open class ClassShield(val heroClass: HeroClass) : Button() {
        private lateinit var avatar: Image
        private lateinit var emitter: Emitter
        private var brightness: Float = 0f
        private lateinit var name: RenderedText

        init {
            avatar.frame(heroClass.ordinal * WIDTH, 0, WIDTH, HEIGHT)
            avatar.scale.set(SCALE)

            brightness = if (IsLocked(heroClass)) MIN_BRIGHTNESS else 1f
            updateBrightness()

            name.text(heroClass.title().toUpperCase())
            name.hardlight(if (Badges.isUnlocked(heroClass.masteryBadge()))
                MASTERY_HIGHLIGHTED else BASIC_HIGHLIGHTED)
        }

        override fun createChildren() {
            super.createChildren()

            avatar = Image(Assets.DPD_AVATARS)
            add(avatar)

            emitter = BitmaskEmitter(avatar)
            add(emitter)

            name = renderText(9)
            add(name)
        }

        override fun layout() {
            super.layout()

            avatar.x = x + (width - avatar.width()) / 2f
            avatar.y = y + (height - avatar.height() - name.height()) / 2f
            align(avatar)

            name.x = x + (width - name.width()) / 2f
            name.y = avatar.y + avatar.height() + SCALE
            align(name)
        }

        fun showSpeckEffects() {
            emitter.revive()
            emitter.start(Speck.factory(Speck.LIGHT), 0.05f, 7)
        }

        private fun updateBrightness() {
            avatar.am = brightness
            avatar.rm = avatar.am
            avatar.bm = avatar.rm
            avatar.gm = avatar.bm
        }
    }

    private open class GameButton(primary: String) : RedButton(primary) {
        private lateinit var secondary: RenderedText

        init {
            this.secondary.text(null)
        }

        override fun createChildren() {
            super.createChildren()

            secondary = renderText(6)
            add(secondary)
        }

        override fun layout() {
            super.layout()

            if (secondary.text().isNotEmpty()) {
                text.y = y + (height - text.height() - secondary.baseLine()) / 2

                secondary.x = x + (width - secondary.width()) / 2
                secondary.y = text.y + text.height()
            } else {
                text.y = y + (height - text.baseLine()) / 2
            }
            align(text)
            align(secondary)
        }

        fun secondary(text: String?, highlighted: Boolean) {
            secondary.text(text)

            secondary.hardlight(if (highlighted) SECONDARY_COLOR_H else SECONDARY_COLOR_N)
        }
    }
}