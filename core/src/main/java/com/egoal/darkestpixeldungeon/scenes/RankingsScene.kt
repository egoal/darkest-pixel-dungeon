package com.egoal.darkestpixeldungeon.scenes

import com.egoal.darkestpixeldungeon.Assets
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon
import com.egoal.darkestpixeldungeon.Rankings
import com.egoal.darkestpixeldungeon.effects.Flare
import com.egoal.darkestpixeldungeon.messages.M
import com.egoal.darkestpixeldungeon.messages.Messages
import com.egoal.darkestpixeldungeon.sprites.ItemSprite
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet
import com.egoal.darkestpixeldungeon.ui.*
import com.egoal.darkestpixeldungeon.windows.WndError
import com.egoal.darkestpixeldungeon.windows.WndRanking
import com.watabou.noosa.BitmapText
import com.watabou.noosa.Camera
import com.watabou.noosa.Image
import com.watabou.noosa.audio.Music
import com.watabou.noosa.ui.Button
import com.watabou.noosa.ui.Component

class RankingsScene : PixelScene() {
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

        Rankings.Load()

        val title = renderText(M.L(this, "title"), 9)
        title.hardlight(Window.SHPX_COLOR)
        title.x = (w - title.width()) / 2f
        title.y = GAP
        align(title)
        add(title)

        if (Rankings.records.isEmpty()) {
            val noRec = renderText(M.L(this, "no_games"), 8)
            noRec.hardlight(0xcccccc)
            noRec.x = (w - noRec.width()) / 2f
            noRec.y = (h - noRec.height()) / 2f
            align(noRec)
            add(noRec)
        } else {
            val rowWidth = w - GAP * 2f
            val offsetHor = 0f

            val list = ScrollPane(Component())
            add(list)
            val content = list.content()

            for (pr in Rankings.records.withIndex()) {
                val row = Record(pr.index, pr.index == Rankings.lastRecord, pr.value)
                val offset = if (pr.index % 2 == 1) offsetHor else 0f
                row.setRect(offset, pr.index * ROW_HEIGHT, rowWidth - offsetHor, ROW_HEIGHT)
                content.add(row)
            }

            content.setSize(rowWidth, ROW_HEIGHT * Rankings.records.size)

            // game count
            val label = renderText(M.L(this, "total") + " ", 8).apply { hardlight(0xcccccc) }
            add(label)

            val won = renderText(Rankings.wonNumber.toString(), 8).apply { hardlight(Window.SHPX_COLOR) }
            add(won)

            val total = renderText("/" + Rankings.totalNumber.toString(), 8).apply { hardlight(0xcccccc) }
            total.x = (w - total.width()) / 2f
            total.y = h - label.height() - GAP
            add(total)

            val tw = label.width() + won.width() + total.width()
            label.x = (w - tw) / 2f
            label.y = total.y
            won.x = label.x + label.width()
            won.y = total.y
            total.x = won.x + won.width()

            align(label)
            align(total)
            align(won)

            //
            list.setRect((w - rowWidth) / 2f, title.y + title.height() + GAP,
                    rowWidth, (label.y - GAP) - (title.y + title.height() + GAP))
            list.scrollTo(0f, 0f)
        }

        val btnExit = ExitButton()
        btnExit.setPos(w - btnExit.width(), 0f)
        add(btnExit)

        fadeIn()
    }

    override fun onBackPressed() {
        DarkestPixelDungeon.switchNoFade(TitleScene::class.java)
    }


    companion object {
        private const val ROW_HEIGHT = 20f
        private const val GAP = 4f

        private val TEXT_WIN = intArrayOf(0xFFFF88, 0xB2B25F)
        private val TEXT_LOSE = intArrayOf(0xDDDDDD, 0x888888)
        private const val FLARE_WIN = 0x888866
        private const val FLARE_LOSE = 0x666666
    }

    class Record(pos: Int, latest: Boolean, private val rec: Rankings.Record) : Button() {

        private lateinit var shield: ItemSprite
        private var flare: Flare? = null
        private lateinit var position: BitmapText
        private lateinit var desc: RenderedTextMultiline
        private lateinit var steps: Image
        private lateinit var depth: BitmapText
        private lateinit var classIcon: Image
        private lateinit var level: BitmapText

        init {
            if (latest) {
                flare = Flare(6, 24f).apply {
                    angularSpeed = 90f
                    color(if (rec.win) FLARE_WIN else FLARE_LOSE)
                }
                addToBack(flare)
            }

            if (pos != Rankings.CAPACITY - 1) position.text(Integer.toString(pos + 1))
            else position.text(" ")
            position.measure()

            desc.text(Messages.titleCase(rec.desc()))

            //desc.measure();
            val odd = pos % 2

            if (rec.win) {
                shield.view(ItemSpriteSheet.AMULET, null)
                position.hardlight(TEXT_WIN[odd])
                desc.hardlight(TEXT_WIN[odd])
                depth.hardlight(TEXT_WIN[odd])
                level.hardlight(TEXT_WIN[odd])
            } else {
                position.hardlight(TEXT_LOSE[odd])
                desc.hardlight(TEXT_LOSE[odd])
                depth.hardlight(TEXT_LOSE[odd])
                level.hardlight(TEXT_LOSE[odd])

                if (rec.depth != 0) {
                    depth.text(Integer.toString(rec.depth))
                    depth.measure()
                    steps.copy(Icons.DEPTH_LG.get())

                    add(steps)
                    add(depth)
                }

            }

            if (rec.heroLevel != 0) {
                level.text(Integer.toString(rec.heroLevel))
                level.measure()
                add(level)
            }

            classIcon.copy(Icons[rec.heroClass])
        }

        override fun createChildren() {

            super.createChildren()

            shield = ItemSprite(ItemSpriteSheet.TOMB, null)
            add(shield)

            position = BitmapText(pixelFont).apply { alpha(0.8f) }
            add(position)

            desc = renderMultiline(7)
            add(desc)

            depth = BitmapText(pixelFont).apply { alpha(0.8f) }

            steps = Image()

            classIcon = Image()
            add(classIcon)

            level = BitmapText(pixelFont).apply { alpha(0.8f) }
        }

        override fun layout() {
            super.layout()

            shield.x = x
            shield.y = y + (height - shield.height) / 2f
            align(shield)

            position.x = shield.x + (shield.width - position.width()) / 2f
            position.y = shield.y + (shield.height - position.height()) / 2f + 1f
            align(position)

            flare?.point(shield.center())

            classIcon.x = x + width - classIcon.width
            classIcon.y = shield.y

            level.x = classIcon.x + (classIcon.width - level.width()) / 2f
            level.y = classIcon.y + (classIcon.height - level.height()) / 2f + 1f
            align(level)

            steps.x = x + width - steps.width - classIcon.width
            steps.y = shield.y

            depth.x = steps.x + (steps.width - depth.width()) / 2f
            depth.y = steps.y + (steps.height - depth.height()) / 2f + 1f
            align(depth)

            desc.maxWidth((steps.x - (shield.x + shield.width + GAP)).toInt())
            desc.setPos(shield.x + shield.width + GAP, shield.y + (shield.height - desc.height()) / 2f + 1f)
            align(desc)

            //todo: simply modify hot area here, fragile but works for now
            hotArea.x = depth.x
            hotArea.width = classIcon.x + classIcon.width() - depth.x
        }

        override fun onClick() {
            //fixme: if can be null when i break the outdated savings.
            if (rec.gameData != null) {
                parent.add(WndRanking(rec))
            } else {
                parent.add(WndError(Messages.get(RankingsScene::class.java, "no_info")))
            }
        }
    }
}