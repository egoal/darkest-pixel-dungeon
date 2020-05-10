package com.egoal.darkestpixeldungeon.scenes

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.Badges
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.effects.BadgeBanner
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.ui.*
import com.watabou.noosa.Camera
import com.watabou.noosa.Game
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Music
import com.watabou.noosa.ui.Button
import com.watabou.noosa.ui.Component
import com.watabou.utils.Callback
import com.watabou.utils.Random

class KBadgesScene : PixelScene() {
    private val archs: Archs by lazy { Archs() }
    override fun create() {
        super.create()

        Music.INSTANCE.play(Assets.TRACK_MAIN_THEME, true)
        Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f)

        uiCamera.visible = false

        val w = Camera.main.width.toFloat()
        val h = Camera.main.height.toFloat()

        archs.setSize(w, h)
        add(archs)

        Badges.loadGlobal()

        val allBadges = Badges.allVisiableBadges().sortedBy { if (it.meta) (10000 + it.ordinal) else it.ordinal }
        val gained = Badges.filtered(true)

        val title = renderText(M.L(BadgesScene::class.java, "title", gained.size, allBadges.size), 9)
        title.hardlight(Window.TITLE_COLOR)

        title.x = (w - title.width()) / 2f
        title.y = GAP
        align(title)
        add(title)


        val rowWidth = w - GAP * 2f
        val list = ScrollPane(Component())
        add(list)
        val content = list.content()
        for (pr in allBadges.withIndex()) {
            val row = BadgeButton(pr.value, !gained.contains(pr.value) && pr.value.meta)
            if (!gained.contains(pr.value))
                row.deepen()

            row.setRect(INNER_MARGIN, pr.index * ROW_HEIGHT, rowWidth - INNER_MARGIN * 2f, ROW_HEIGHT)
            content.add(row)
        }
        content.setSize(rowWidth, ROW_HEIGHT * allBadges.size)

        list.setRect(GAP, title.y + title.height() + GAP, rowWidth, h - GAP - (title.y + title.height()))
        list.scrollTo(0f, 0f)

        val btnExit = ExitButton()
        btnExit.setPos(w - btnExit.width(), 0f)
        add(btnExit)

        fadeIn()

        Badges.loadingListener = Callback {
            if (Game.scene() === this) DarkestPixelDungeon.switchNoFade(KBadgesScene::class.java)
        }
    }

    override fun onBackPressed() {
        DarkestPixelDungeon.switchNoFade(TitleScene::class.java)
    }

    override fun destroy() {
        Badges.saveGlobal()
        Badges.loadingListener = null
        super.destroy()
    }

    companion object {
        private const val GAP = 4f
        private const val INNER_MARGIN = 6f
        private const val ROW_HEIGHT = 20f
    }

    private class BadgeButton(private val badge: Badges.Badge, hide: Boolean) : Component() {
        private val icon: Image
        private val line: RenderedTextMultiline

        init {
            icon = if (hide) Image(Assets.LOCKED) else BadgeBanner.image(badge.image)
            add(icon)

            line = renderMultiline(7)
            add(line)

            line.text(if (hide) M.L(Badges::class.java, "hidden") else badge.desc())
        }

        override fun layout() {
            super.layout()

            icon.x = x
            icon.y = y + (height - icon.height) / 2f
            align(icon)

            line.maxWidth((width - (icon.width + GAP)).toInt())
            line.setPos(icon.x + icon.width + GAP, y + (height - line.height()) / 2f)
            align(line)
        }

        fun deepen() {
            icon.brightness(0.3f)
            line.hardlight(0xaaaaaa)
        }

        override fun update() {
            super.update()

            if (Random.Float() < Game.elapsed * 0.1f)
                BadgeBanner.highlight(icon, badge.image)
        }
    }
}