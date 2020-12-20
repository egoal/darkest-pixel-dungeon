package com.egoal.darkestpixeldungeon.scenes

import com.egoal.darkestpixeldungeon.*
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.*
import com.egoal.darkestpixeldungeon.windows.WndOptions
import com.watabou.noosa.*
import com.watabou.noosa.ui.Button

class SlotSelectScene : PixelScene() {
    override fun create() {
        super.create()

        Badges.loadGlobal()

        uiCamera.visible = false

        val w = Camera.main.width
        val h = Camera.main.height

        val archs = Archs().apply { setSize(w.toFloat(), h.toFloat()) }
        add(archs)

        val btnExit = ExitButton()
        btnExit.setPos(Camera.main.width - btnExit.width(), 0f)
        add(btnExit)

        // slots
        val progresses = GamesInProgress.reloadAll()
        val totalHeight = progresses.size * SLOT_HEIGHT + (progresses.size - 1) * SLOT_GAP

        var yPrg = (h - totalHeight) / 2f
        for (pr in progresses.withIndex()) {
            val btn = SlotButton()
            btn.set(pr.index)
            btn.setRect((w - SLOT_WIDTH) / 2f, yPrg, SLOT_WIDTH, SLOT_HEIGHT)
            align(btn)
            add(btn)

            yPrg += SLOT_HEIGHT + SLOT_GAP
        }

        GamesInProgress.curSlot = 0

        fadeIn()
    }

    override fun onBackPressed() {
        DarkestPixelDungeon.switchNoFade(TitleScene::class.java)
    }

    companion object {
        private const val SLOT_WIDTH = 120f
        private const val SLOT_HEIGHT = 24f
        private const val SLOT_GAP = 2f
    }

    class SlotButton : Button() {
        private lateinit var bg: NinePatch
        private lateinit var name: RenderedText

        private var hero: Image? = null
        private var depth: BitmapText? = null
        private var stairs: Image? = null
        private var level: BitmapText? = null
        private var classIcon: Image? = null

        private var slot: Int = 0
        private var empty: Boolean = false

        override fun createChildren() {
            super.createChildren()

            bg = Chrome.get(Chrome.Type.TOAST_TR)
            add(bg)

            name = PixelScene.renderText(8)
            add(name)
        }

        fun set(slot: Int) {
            this.slot = slot
            val info = GamesInProgress[slot]
            empty = info == null
            if (empty) {
                name.text(M.L(SlotSelectScene::class.java, "empty_slot"))
                if (hero != null) {
                    remove(hero)
                    hero = null
                    remove(depth)
                    depth = null
                    remove(stairs)
                    stairs = null
                    remove(level)
                    level = null
                    remove(classIcon)
                    classIcon = null
                }
            } else {
                name.text(info!!.name)
                if (hero == null) {
                    hero = Image(info.heroClass.spritesheet(), 0, 15 * info.armorTier, 12, 15)
                    add(hero)

                    stairs = Image(Icons.get(Icons.DEPTH))
                    add(stairs)
                    depth = BitmapText(PixelScene.pixelFont)
                    add(depth)

                    classIcon = Image(Icons.get(info.heroClass))
                    add(classIcon)
                    level = BitmapText(PixelScene.pixelFont)
                    add(level)
                } else {
                    hero!!.copy(Image(info.heroClass.spritesheet(), 0, 15 * info.armorTier, 12, 15))
                    classIcon!!.copy(Image(Icons.get(info.heroClass)))
                }

                depth!!.text(info.depth.toString())
                depth!!.measure()
                level!!.text(info.level.toString())
                level!!.measure()

                if (info.isChallenged) {
                    name.hardlight(Window.TITLE_COLOR)
                } else name.resetColor()
            }

            layout()
        }

        override fun layout() {
            super.layout()

            bg.x = x
            bg.y = y
            bg.size(width, height)

            if (hero != null) {
                hero!!.x = x + 8
                hero!!.y = y + (height - hero!!.height()) / 2f
                align(hero)

                name.x = hero!!.x + hero!!.width() + 6f
                name.y = y + (height - name.height()) / 2f
                align(name)

                classIcon!!.x = x + width - 24 + (16 - classIcon!!.width()) / 2f
                classIcon!!.y = y + (height - classIcon!!.height()) / 2f
                align(classIcon)

                level!!.x = classIcon!!.x + (classIcon!!.width() - level!!.width()) / 2f
                level!!.y = classIcon!!.y + (classIcon!!.height() - level!!.height()) / 2f + 1
                align(level)

                stairs!!.x = x + width - 40 + (16 - stairs!!.width()) / 2f
                stairs!!.y = y + (height - stairs!!.height()) / 2f
                align(stairs)

                depth!!.x = stairs!!.x + (stairs!!.width() - depth!!.width()) / 2f
                depth!!.y = stairs!!.y + (stairs!!.height() - depth!!.height()) / 2f + 1
                align(depth)

            } else {
                name.x = x + (width - name.width()) / 2f
                name.y = y + (height - name.height()) / 2f

                align(name)
            }
        }

        override fun onClick() {
            if (empty) {
                GamesInProgress.curSlot = slot
                DarkestPixelDungeon.switchScene(HeroCreateScene::class.java)
            } else {
                GamesInProgress.curSlot = slot
                Dungeon.nullHero()
                ActionIndicator.action = null
                InterlevelScene.mode = InterlevelScene.Mode.CONTINUE
                DarkestPixelDungeon.switchScene(InterlevelScene::class.java)
            }
        }

        override fun onLongClick(): Boolean {
            val wnd = WndOptions.CreateConfirm(Icons.WARNING.get(),
                    M.L(SlotSelectScene::class.java, "delete_title"),
                    M.L(SlotSelectScene::class.java, "delete_message")) {
                GamesInProgress.delete(slot, true, true)
                set(slot)
            }
            Game.scene().addToFront(wnd)

            return true
        }
    }
}
